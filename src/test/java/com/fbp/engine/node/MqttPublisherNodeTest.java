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

package com.fbp.engine.node;

import com.fbp.engine.message.Message;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class MqttPublisherNodeTest {

    MqttPublisherNode publisher;

    @BeforeEach
    void setUp() {
        publisher = new MqttPublisherNode("mqtt-publisher", Map.of(
                "brokerUrl", "tcp://localhost:1883",
                "clientId", "publisher-test-client",
                "topic", "test/pub/default",
                "qos", 1
        ));
    }

    @AfterEach
    void tearDown() {
        publisher.shutdown();
    }

    @Test
    @DisplayName("포트 구성")
    void test1() {
        // getInputPort("in")이 null이 아님
        Assertions.assertNotNull(publisher.getInputPort("in"));
    }

    @Test
    @DisplayName("초기 상태")
    void test2() {
        // 생성 직후 isConnected()가 false
        Assertions.assertFalse(publisher.isConnected());
    }

    @Test
    @DisplayName("config 기본 토픽 조회")
    void test3() {
        // getConfig("topic")가 설정 값과 일치
        Assertions.assertEquals("test/pub/default", publisher.getConfig("topic"));
    }

    @Test
    @DisplayName("Broker 연결 성공")
    @Tag("integration")
    void test4() {
        // initialize() 후 isConnected()가 true
        publisher.initialize();

        Assertions.assertTrue(publisher.isConnected());
    }

    @Test
    @DisplayName("메시지 발행")
    @Tag("integration")
    void test5() throws MqttException, InterruptedException {
        // FBP Message를 process()로 보내면 Broker에서 수신됨 (별도 subscriber로 확인)
        publisher.initialize();

        MqttClient client = new MqttClient("tcp://localhost:1883", "test-sub-1");
        client.connect();

        CountDownLatch latch = new CountDownLatch(1);
        client.subscribe("test/pub/default", 1, (topic, msg) -> {
            latch.countDown();
        });

        publisher.process(new Message(Map.of("data", "test")));

        boolean messageReceived = latch.await(3, TimeUnit.SECONDS);

        Assertions.assertTrue(messageReceived);

        client.disconnect();
    }

    @Test
    @DisplayName("동적 토픽")
    @Tag("integration")
    void test6() throws MqttException, InterruptedException {
        // 메시지에 "topic" 키가 있으면 해당 토픽으로 발행됨
        MqttPublisherNode testPublisher = new MqttPublisherNode("mqtt-test-publisher", Map.of(
                "brokerUrl", "tcp://localhost:1883",
                "clientId", "publisher-client",
                "qos", 1
        ));

        testPublisher.initialize();

        MqttClient client = new MqttClient("tcp://localhost:1883", "test-sub-2");
        client.connect();

        CountDownLatch latch = new CountDownLatch(1);
        client.subscribe("test/pub/dynamic", 1, (topic, msg) -> {
            latch.countDown();
        });

        testPublisher.process(new Message(Map.of("data", "test", "topic", "test/pub/dynamic")));

        boolean messageReceived = latch.await(3, TimeUnit.SECONDS);

        Assertions.assertTrue(messageReceived);

        client.disconnect();
    }

    @Test
    @DisplayName("shutdown 후 연결 해제")
    @Tag("integration")
    void test7() {
        // shutdown() 후 isConnected()가 false
        publisher.initialize();
        publisher.shutdown();

        Assertions.assertFalse(publisher.isConnected());
    }

}