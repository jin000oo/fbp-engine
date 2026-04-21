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

import com.fbp.engine.core.Connection;
import com.fbp.engine.message.Message;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TemperatureSensorNodeTest {

    TemperatureSensorNode sensor;

    Connection connection;

    @BeforeEach
    void setUp() {
        sensor = new TemperatureSensorNode("sensor", 15, 45);
        connection = new Connection("connection");
        sensor.getOutputPort("out").connect(connection);
    }

    @Test
    @DisplayName("온도 범위 확인")
    void test1() {
        // 생성된 온도가 min~max 범위 이내
        sensor.process(new Message(Map.of("trigger", true)));

        Message message = connection.poll();
        Assertions.assertNotNull(message);

        double temperature = message.get("temperature");
        Assertions.assertTrue(temperature >= 15 && temperature <= 45);
    }

    @Test
    @DisplayName("필수 키 포함")
    void test2() {
        // 출력 메시지에 "sensorId", "temperature", "unit", "timestamp" 키가 모두 존재
        sensor.process(new Message(Map.of("trigger", true)));

        Message message = connection.poll();
        Assertions.assertNotNull(message);

        Assertions.assertAll(
                () -> Assertions.assertTrue(message.hasKey("sensorId")),
                () -> Assertions.assertTrue(message.hasKey("temperature")),
                () -> Assertions.assertTrue(message.hasKey("unit")),
                () -> Assertions.assertTrue(message.hasKey("timestamp"))
        );
    }

    @Test
    @DisplayName("sensorId 일치")
    void test3() {
        // 메시지의 "sensorId"가 노드 ID와 일치
        sensor.process(new Message(Map.of("trigger", true)));

        Message message = connection.poll();
        Assertions.assertNotNull(message);

        Assertions.assertEquals("sensor", message.get("sensorId"));
    }

    @Test
    @DisplayName("트리거마다 생성")
    void test4() {
        // 트리거 메시지를 3번 보내면 3개의 출력 메시지 생성
        sensor.process(new Message(Map.of("trigger", 1)));
        sensor.process(new Message(Map.of("trigger", 2)));
        sensor.process(new Message(Map.of("trigger", 3)));

        Assertions.assertEquals(3, connection.getBufferSize());
    }

}