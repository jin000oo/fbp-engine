/*
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * + Copyright 2026. NHN Academy Corp. All rights reserved.
 * + * While every precaution has been taken in the preparation of this resource,  assumes no
 * + responsibility for errors or omissions, or for damages resulting from the use of the information
 * + contained herein
 * + No part of this resource may be reproduced, stored in a retrieval system, or transmitted, in any
 * + form or by any means, electronic, mechanical, photocopying, recording, or otherwise, without the
 * + prior written permission.
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */

package com.fbp.engine.integration;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.node.ModbusWriterNode;
import com.fbp.engine.node.MqttPublisherNode;
import com.fbp.engine.node.MqttSubscriberNode;
import com.fbp.engine.node.TransformNode;
import com.fbp.engine.protocol.ModbusTcpSimulator;
import com.fbp.engine.rule.RuleNode;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
class MqttModbusIntegrationTest {
    // MQTT↔MODBUS 통합 플로우

    ModbusTcpSimulator simulator;

    FlowEngine engine;

    @BeforeEach
    void setUp() {
        simulator = new ModbusTcpSimulator(5099, 10);
        simulator.start();

        simulator.setRegister(2, 0);

        engine = new FlowEngine();

        Flow flow = new Flow("flow");

        flow.addNode(new MqttSubscriberNode("mqtt-subscriber", Map.of(
                        "brokerUrl", "tcp://localhost:1883",
                        "clientId", "test-sub",
                        "topic", "sensor/test")))
                .addNode(new RuleNode("rule", "temperature > 30.0"))
                .addNode(new TransformNode("transformer", msg -> msg.withEntry("fanFlag", 1)))
                .addNode(new ModbusWriterNode("modbus-writer", Map.of(
                        "host", "localhost",
                        "port", 5099,
                        "slaveId", 1,
                        "registerAddress", 2,
                        "valueField", "fanFlag")))
                .addNode(new MqttPublisherNode("mqtt-publisher", Map.of(
                        "brokerUrl", "tcp://localhost:1883",
                        "clientId", "test-pub",
                        "topic", "alert/test")));

        flow.connect("mqtt-subscriber", "out", "rule", "in")
                .connect("rule", "match", "transformer", "in")
                .connect("transformer", "out", "modbus-writer", "in")
                .connect("rule", "match", "mqtt-publisher", "in");

        engine.register(flow);
        engine.startFlow(flow.getId());
    }

    @AfterEach
    void tearDown() {
        engine.shutdown();
        simulator.stop();
    }

    @Test
    @DisplayName("MQTT 수신 → Rule 분기")
    void test1() throws MqttException, InterruptedException {
        // MQTT로 발행한 메시지가 RuleNode를 통해 올바르게 분기됨
        MqttClient testSender = new MqttClient("tcp://localhost:1883", "sender-" + UUID.randomUUID());
        testSender.connect();

        testSender.publish("sensor/test", new MqttMessage("{\"temperature\": 25.0}".getBytes()));
        Thread.sleep(1000);
        Assertions.assertEquals(0, simulator.getRegister(2));

        testSender.publish("sensor/test", new MqttMessage("{\"temperature\": 35.0}".getBytes()));
        Thread.sleep(1000);
        Assertions.assertEquals(1, simulator.getRegister(2));

        testSender.disconnect();
    }

    @Test
    @DisplayName("Rule match → MODBUS 쓰기")
    void test2() throws MqttException, InterruptedException {
        // 규칙 만족 시 MODBUS 레지스터에 값이 기록됨
        MqttClient testSender = new MqttClient("tcp://localhost:1883", "sender-" + UUID.randomUUID());
        testSender.connect();

        testSender.publish("sensor/test", new MqttMessage("{\"temperature\": 40.0}".getBytes()));
        Thread.sleep(1000);
        Assertions.assertEquals(1, simulator.getRegister(2));

        testSender.disconnect();
    }

    @Test
    @DisplayName("Rule match → MQTT 알림")
    void test3() throws MqttException, InterruptedException {
        // 규칙 만족 시 알림 토픽에 메시지가 발행됨
        MqttClient testClient = new MqttClient("tcp://localhost:1883", "tester-" + UUID.randomUUID());
        testClient.connect();

        CountDownLatch latch = new CountDownLatch(1);
        testClient.subscribe("alert/test", (topic, msg) -> latch.countDown());

        testClient.publish("sensor/test", new MqttMessage("{\"temperature\": 45.0}".getBytes()));

        boolean received = latch.await(3, TimeUnit.SECONDS);
        Assertions.assertTrue(received);

        testClient.disconnect();
    }

    @Test
    @DisplayName("End-to-End 흐름")
    void test4() throws MqttException, InterruptedException {
        // 전체 파이프라인이 중단 없이 동작
        MqttClient e2eClient = new MqttClient("tcp://localhost:1883", "e2e-" + UUID.randomUUID());
        e2eClient.connect();

        CountDownLatch latch = new CountDownLatch(1);
        e2eClient.subscribe("alert/test", (topic, msg) -> latch.countDown());

        e2eClient.publish("sensor/test", new MqttMessage("{\"temperature\": 50.0}".getBytes()));

        boolean receivedAlert = latch.await(3, TimeUnit.SECONDS);

        Assertions.assertAll(
                () -> Assertions.assertTrue(receivedAlert),
                () -> Assertions.assertEquals(1, simulator.getRegister(2))
        );

        e2eClient.disconnect();
    }

}
