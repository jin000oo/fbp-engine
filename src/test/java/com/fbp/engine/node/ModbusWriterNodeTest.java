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
import com.fbp.engine.protocol.ModbusTcpSimulator;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ModbusWriterNodeTest {

    ModbusWriterNode writer;

    ModbusTcpSimulator simulator;

    Connection connection;

    @BeforeEach
    void setUp() throws InterruptedException {
        simulator = new ModbusTcpSimulator(5024, 10);
        simulator.start();

        writer = new ModbusWriterNode("writer", Map.of(
                "host", "localhost",
                "port", 5024,
                "slaveId", 1,
                "registerAddress", 2,
                "valueField", "targetTemperature",
                "scale", 10.0
        ));

        connection = new Connection("result");
        writer.getOutputPort("result").connect(connection);

        Thread.sleep(100);
    }

    @AfterEach
    void tearDown() {
        writer.shutdown();
        simulator.stop();
    }

    @Test
    @DisplayName("포트 구성")
    void test1() {
        // getInputPort("in")이 null이 아님
        Assertions.assertNotNull(writer.getInputPort("in"));
    }

    @Test
    @DisplayName("초기 상태")
    void test2() {
        // 생성 직후 isConnected()가 false
        Assertions.assertFalse(writer.isConnected());
    }

    @Test
    @DisplayName("config 확인")
    void test3() {
        // getConfig("registerAddress") 등이 설정 값과 일치
        Assertions.assertAll(
                () -> Assertions.assertEquals("localhost", writer.getConfig("host")),
                () -> Assertions.assertEquals(5024, writer.getConfig("port")),
                () -> Assertions.assertEquals(1, writer.getConfig("slaveId")),
                () -> Assertions.assertEquals(2, writer.getConfig("registerAddress")),
                () -> Assertions.assertEquals("targetTemperature", writer.getConfig("valueField")),
                () -> Assertions.assertEquals(10.0, writer.getConfig("scale"))
        );
    }

    @Test
    @DisplayName("연결 성공")
    void test4() {
        // initialize() 후 isConnected()가 true
        writer.initialize();

        Assertions.assertTrue(writer.isConnected());
    }

    @Test
    @DisplayName("레지스터 쓰기")
    void test5() {
        // FBP Message를 process()로 보낸 후, 시뮬레이터의 getRegister()로 값 변경 확인
        writer.initialize();

        writer.process(new Message(Map.of("targetTemperature", 25.5)));

        Assertions.assertEquals(255, simulator.getRegister(2));
    }

    @Test
    @DisplayName("스케일 변환")
    void test6() {
        // scale=10.0 설정 시 25.5 → 255로 변환되어 기록됨
        writer.initialize();

        writer.process(new Message(Map.of("targetTemperature", 25.5)));

        Message result = connection.poll();

        Assertions.assertAll(
                () -> Assertions.assertNotNull(result),
                () -> Assertions.assertEquals("success", result.get("status")),
                () -> Assertions.assertEquals(255, (int) result.get("writtenValue"))
        );
    }

    @Test
    @DisplayName("shutdown 후 연결 해제")
    void test7() {
        // shutdown() 후 isConnected()가 false
        writer.initialize();
        writer.shutdown();

        Assertions.assertFalse(writer.isConnected());
    }

}