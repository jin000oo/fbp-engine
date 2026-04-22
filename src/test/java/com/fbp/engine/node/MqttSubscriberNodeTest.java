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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fbp.engine.core.Connection;
import com.fbp.engine.message.Message;
import java.lang.reflect.Field;
import java.util.Map;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class MqttSubscriberNodeTest {

    MqttSubscriberNode subscriber;

    Connection connection;

    @BeforeEach
    void setUp() {
        subscriber = new MqttSubscriberNode("mqtt-subscriber", Map.of(
                "brokerUrl", "tcp://localhost:1883",
                "clientId", "subscriber-test-client",
                "topic", "test/sub/default",
                "qos", 1
        ));
        connection = new Connection("connection");

        subscriber.getOutputPort("out").connect(connection);
    }

    @AfterEach
    void tearDown() {
        subscriber.shutdown();
    }

    @Test
    @DisplayName("포트 구성")
    void test1() {
        // getOutputPort("out")이 null이 아님
        Assertions.assertNotNull(subscriber.getOutputPort("out"));
    }

    @Test
    @DisplayName("초기 상태")
    void test2() {
        // 생성 직후 isConnected()가 false
        Assertions.assertFalse(subscriber.isConnected());
    }

    @Test
    @DisplayName("config 조회")
    void test3() {
        // getConfig("brokerUrl")가 설정한 값과 일치
        Assertions.assertEquals("tcp://localhost:1883", subscriber.getConfig("brokerUrl"));
    }

    @Test
    @DisplayName("JSON → Message 변환")
    void test4() throws NoSuchFieldException, IllegalAccessException, JsonProcessingException {
        // JSON 문자열을 수동으로 변환하는 내부 메서드가 올바른 Map을 반환 (리플렉션 또는 protected 메서드 테스트)
        Field field = MqttSubscriberNode.class.getDeclaredField("objectMapper");
        field.setAccessible(true);

        ObjectMapper objectMapper = (ObjectMapper) field.get(subscriber);

        String json = "{\"temperature\": 25.5}";

        Map<String, Object> result = objectMapper.readValue(json, new TypeReference<>() {
        });

        Assertions.assertEquals(25.5, result.get("temperature"));
    }

    @Test
    @DisplayName("JSON 파싱 실패 처리")
    void test5() throws MqttException {
        // 잘못된 JSON이 들어왔을 때 "rawPayload" 키로 원본 문자열이 전달됨
        subscriber.initialize();

        MqttClient client = new MqttClient("tcp://localhost:1883", "test-pub-1");
        client.connect();

        client.publish("test/sub/default", new MqttMessage("Not Json".getBytes()));

        client.disconnect();

        Message message = connection.poll();

        Assertions.assertAll(
                () -> Assertions.assertNotNull(message),
                () -> Assertions.assertEquals("Not Json", message.get("rawPayload"))
        );
    }

    @Test
    @DisplayName("Broker 연결 성공")
    @Tag("integration")
    void test6() {
        // initialize() 후 isConnected()가 true
        subscriber.initialize();

        Assertions.assertTrue(subscriber.isConnected());
    }

    @Test
    @DisplayName("메시지 수신")
    @Tag("integration")
    void test7() throws MqttException {
        // Broker에 publish 후 CollectorNode에서 메시지가 수신됨
        subscriber.initialize();

        MqttClient client = new MqttClient("tcp://localhost:1883", "test-pub-1");
        client.connect();

        client.publish("test/sub/default", new MqttMessage("{\"sensorId\": \"test\", \"value\": 100}".getBytes()));

        client.disconnect();

        Message message = connection.poll();

        Assertions.assertAll(
                () -> Assertions.assertNotNull(message),
                () -> Assertions.assertEquals("test", message.get("sensorId")),
                () -> Assertions.assertEquals(100, (Integer) message.get("value"))
        );
    }

    @Test
    @DisplayName("토픽 정보 포함")
    @Tag("integration")
    void test8() throws MqttException {
        // 수신한 FBP Message에 "topic" 키가 포함됨
        subscriber.initialize();

        MqttClient client = new MqttClient("tcp://localhost:1883", "test-pub-1");
        client.connect();

        client.publish("test/sub/default", new MqttMessage("{\"sensorId\": \"test\", \"value\": 100}".getBytes()));

        client.disconnect();

        Assertions.assertEquals("test/sub/default", connection.poll().get("topic"));
    }

    @Test
    @DisplayName("shutdown 후 연결 해제")
    @Tag("integration")
    void test9() {
        // shutdown() 후 isConnected()가 false
        subscriber.initialize();
        subscriber.shutdown();

        Assertions.assertFalse(subscriber.isConnected());
    }

}