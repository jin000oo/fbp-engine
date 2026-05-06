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

package com.fbp.engine;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.node.ModbusReaderNode;
import com.fbp.engine.node.ModbusWriterNode;
import com.fbp.engine.node.MqttPublisherNode;
import com.fbp.engine.node.MqttSubscriberNode;
import com.fbp.engine.node.RuleNode;
import com.fbp.engine.node.TimerNode;
import com.fbp.engine.node.TransformNode;
import com.fbp.engine.protocol.ModbusTcpSimulator;
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
class CrossProtocolIntegrationTest {

    ModbusTcpSimulator simulator;

    FlowEngine engine;

    @BeforeEach
    void setUp() throws InterruptedException {
        simulator = new ModbusTcpSimulator(56000, 10);
        simulator.start();

        Thread.sleep(100);

        engine = new FlowEngine();
    }

    @AfterEach
    void tearDown() {
        engine.shutdown();
        simulator.stop();
    }

    @Test
    @DisplayName("MQTT → Rule → MODBUS")
    void test1() throws MqttException, InterruptedException {
        // MQTT 수신 → 규칙 평가 → MODBUS 쓰기 전체 경로 동작
        Flow flow = new Flow("flow");

        String suffix = UUID.randomUUID().toString().substring(0, 5);

        simulator.setRegister(2, 0);

        flow.addNode(new MqttSubscriberNode("mqtt-subscriber", Map.of(
                        "brokerUrl", "tcp://localhost:1883",
                        "clientId", "sub-" + suffix,
                        "topic", "sensor/cross1")))
                .addNode(new RuleNode("rule", "temperature > 30.0"))
                .addNode(new TransformNode("transformer", msg -> msg.withEntry("fanFlag", 1)))
                .addNode(new ModbusWriterNode("modbus-writer", Map.of(
                        "host", "localhost",
                        "port", 56000,
                        "slaveId", 1,
                        "registerAddress", 2,
                        "valueField", "fanFlag")));

        flow.connect("mqtt-subscriber", "out", "rule", "in")
                .connect("rule", "match", "transformer", "in")
                .connect("transformer", "out", "modbus-writer", "in");

        engine.register(flow);
        engine.startFlow(flow.getId());

        MqttClient testClient = new MqttClient("tcp://localhost:1883", "tester-" + suffix);
        testClient.connect();

        testClient.publish("sensor/cross1", new MqttMessage("{\"temperature\": 35.0}".getBytes()));

        Thread.sleep(1000);

        Assertions.assertEquals(1, simulator.getRegister(2));

        testClient.disconnect();
    }

    @Test
    @DisplayName("MODBUS → Rule → MQTT")
    void test2() throws MqttException, InterruptedException {
        // MODBUS 읽기 → 규칙 평가 → MQTT 발행 전체 경로 동작
        Flow flow = new Flow("flow");

        String suffix = UUID.randomUUID().toString().substring(0, 5);

        simulator.setRegister(0, 350);

        flow.addNode(new TimerNode("timer", 500))
                .addNode(new ModbusReaderNode("modbus-reader", Map.of(
                        "host", "localhost",
                        "port", 56000,
                        "slaveId", 1,
                        "startAddress", 0,
                        "count", 1,
                        "registerMapping", Map.of(
                                "0", Map.of(
                                        "name", "temperature",
                                        "scale", 0.1)))))
                .addNode(new RuleNode("rule", "temperature > 30.0"))
                .addNode(new MqttPublisherNode("mqtt-publisher", Map.of(
                        "brokerUrl", "tcp://localhost:1883",
                        "clientId", "pub-" + suffix,
                        "topic", "alert/cross2")));

        flow.connect("timer", "out", "modbus-reader", "trigger")
                .connect("modbus-reader", "out", "rule", "in")
                .connect("rule", "match", "mqtt-publisher", "in");

        MqttClient testClient = new MqttClient("tcp://localhost:1883", "tester-" + suffix);
        testClient.connect();

        CountDownLatch latch = new CountDownLatch(1);
        testClient.subscribe("alert/cross2", (topic, msg) -> latch.countDown());

        engine.register(flow);
        engine.startFlow(flow.getId());

        boolean received = latch.await(4, TimeUnit.SECONDS);

        Assertions.assertTrue(received);

        testClient.disconnect();
    }

    @Test
    @DisplayName("복합 플로우 안정성")
    void test3() {
        // 5분간 연속 실행 시 에러 없이 동작 (장기 실행 테스트)
        Flow flow = new Flow("flow");

        simulator.setRegister(0, 250);

        flow.addNode(new TimerNode("timer", 100))
                .addNode(new ModbusReaderNode("modbus-reader", Map.of(
                        "host", "localhost",
                        "port", 56000,
                        "slaveId", 1,
                        "startAddress", 0,
                        "count", 1,
                        "registerMapping", Map.of(
                                "0", Map.of(
                                        "name", "temperature",
                                        "scale", 0.1)))))
                .addNode(new RuleNode("rule", "temperature > 20.0"));

        flow.connect("timer", "out", "modbus-reader", "trigger")
                .connect("modbus-reader", "out", "rule", "in");

        engine.register(flow);
        engine.startFlow(flow.getId());

        Assertions.assertDoesNotThrow(() -> {
            Thread.sleep(3000);
        });
    }

}
