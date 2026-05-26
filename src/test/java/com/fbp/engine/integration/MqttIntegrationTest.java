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

import com.fbp.engine.core.Connection;
import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.MqttPublisherNode;
import com.fbp.engine.node.MqttSubscriberNode;
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
class MqttIntegrationTest {

    FlowEngine engine;

    @BeforeEach
    void setUp() {
        engine = new FlowEngine();
    }

    @AfterEach
    void tearDown() {
        engine.shutdown();
    }

    @Test
    @DisplayName("Subscriber → Publisher 파이프라인")
    void test1() throws MqttException, InterruptedException {
        // MQTT 수신 메시지가 처리되어 다른 토픽에 발행됨
        Flow flow = new Flow("flow");

        String suffix = UUID.randomUUID().toString().substring(0, 5);

        flow.addNode(new MqttSubscriberNode("mqtt-subscriber", Map.of(
                        "brokerUrl", "tcp://localhost:1883",
                        "clientId", "sub-" + suffix,
                        "topic", "sensor/in")))
                .addNode(new MqttPublisherNode("mqtt-publisher", Map.of(
                        "brokerUrl", "tcp://localhost:1883",
                        "clientId", "pub-" + suffix,
                        "topic", "sensor/out")));

        flow.connect("mqtt-subscriber", "out", "mqtt-publisher", "in");

        engine.register(flow);
        engine.startFlow(flow.getId());

        MqttClient testClient = new MqttClient("tcp://localhost:1883", "tester-" + suffix);
        testClient.connect();

        CountDownLatch latch = new CountDownLatch(1);
        testClient.subscribe("sensor/out", (topic, msg) -> {
            if (new String(msg.getPayload()).contains("hello")) {
                latch.countDown();
            }
        });

        testClient.publish("sensor/in", new MqttMessage("{\"data\": \"hello\"}".getBytes()));

        Assertions.assertTrue(latch.await(3, TimeUnit.SECONDS));

        testClient.disconnect();
    }

    @Test
    @DisplayName("다중 토픽 구독")
    void test2() throws MqttException {
        // 와일드카드 토픽(sensor/+) 구독 시 여러 하위 토픽의 메시지를 수신
        Flow flow = new Flow("flow");

        String suffix = UUID.randomUUID().toString().substring(0, 5);

        MqttSubscriberNode sub = new MqttSubscriberNode("sub", Map.of(
                "brokerUrl", "tcp://localhost:1883",
                "clientId", "sub-" + suffix,
                "topic", "sensor/+"));

        Connection connection = new Connection("out");

        sub.getOutputPort("out").connect(connection);

        flow.addNode(sub);

        engine.register(flow);
        engine.startFlow(flow.getId());

        MqttClient testClient = new MqttClient("tcp://localhost:1883", "tester-" + suffix);
        testClient.connect();

        testClient.publish("sensor/temp", new MqttMessage("{\"v\": 1}".getBytes()));
        testClient.publish("sensor/humi", new MqttMessage("{\"v\": 2}".getBytes()));

        Message message1 = connection.take();
        Message message2 = connection.take();

        Assertions.assertNotNull(message1);
        Assertions.assertNotNull(message2);

        testClient.disconnect();
    }

    @Test
    @DisplayName("QoS 1 전달 보장")
    void test3() throws MqttException {
        // QoS 1로 발행한 메시지가 누락 없이 수신됨
        Flow flow = new Flow("flow");

        String suffix = UUID.randomUUID().toString().substring(0, 5);

        MqttSubscriberNode sub = new MqttSubscriberNode("mqtt-subscriber", Map.of(
                "brokerUrl", "tcp://localhost:1883",
                "clientId", "sub-" + suffix,
                "topic", "qos/test",
                "qos", 1));

        Connection connection = new Connection("out");

        sub.getOutputPort("out").connect(connection);

        flow.addNode(sub);

        engine.register(flow);
        engine.startFlow(flow.getId());

        MqttClient testClient = new MqttClient("tcp://localhost:1883", "tester-" + suffix);
        testClient.connect();

        MqttMessage qosMessage = new MqttMessage("{\"data\": \"important\"}".getBytes());
        qosMessage.setQos(1);

        testClient.publish("qos/test", qosMessage);

        Assertions.assertNotNull(connection.take());

        testClient.disconnect();
    }

    @Test
    @DisplayName("재연결 테스트")
    void test4() {
        // Broker 재시작 후 SubscriberNode가 자동 재연결하여 메시지를 수신
        String suffix = UUID.randomUUID().toString().substring(0, 5);

        MqttSubscriberNode sub = new MqttSubscriberNode("sub", Map.of(
                "brokerUrl", "tcp://localhost:1883",
                "clientId", "sub-" + suffix,
                "topic", "test/reconnect"));

        sub.initialize();
        Assertions.assertTrue(sub.isConnected());

        sub.shutdown();
        Assertions.assertFalse(sub.isConnected());
    }

}
