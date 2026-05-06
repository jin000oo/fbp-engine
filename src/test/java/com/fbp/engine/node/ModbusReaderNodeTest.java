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

class ModbusReaderNodeTest {

    ModbusReaderNode reader;

    ModbusTcpSimulator simulator;

    Connection connection;

    Connection errorConnection;

    @BeforeEach
    void setUp() throws InterruptedException {
        simulator = new ModbusTcpSimulator(5023, 10);
        simulator.start();

        reader = new ModbusReaderNode("reader", Map.of(
                "host", "localhost",
                "port", 5023,
                "slaveId", 1,
                "startAddress", 0,
                "count", 2,
                "registerMapping", Map.of(
                        "0", Map.of(
                                "name", "temperature",
                                "scale", 0.1
                        ),
                        "1", Map.of(
                                "name", "humidity",
                                "scale", 1.0
                        )
                )
        ));

        connection = new Connection("out");
        reader.getOutputPort("out").connect(connection);

        errorConnection = new Connection("error");
        reader.getOutputPort("error").connect(errorConnection);

        Thread.sleep(100);
    }

    @AfterEach
    void tearDown() {
        reader.shutdown();
        simulator.stop();
    }

    @Test
    @DisplayName("포트 구성")
    void test1() {
        // getInputPort("trigger"), getOutputPort("out"), getOutputPort("error")가 null이 아님
        Assertions.assertAll(
                () -> Assertions.assertNotNull(reader.getInputPort("trigger")),
                () -> Assertions.assertNotNull(reader.getOutputPort("out")),
                () -> Assertions.assertNotNull(reader.getOutputPort("error"))
        );
    }

    @Test
    @DisplayName("초기 상태")
    void test2() {
        // 생성 직후 isConnected()가 false
        Assertions.assertFalse(reader.isConnected());
    }

    @Test
    @DisplayName("config 확인")
    void test3() {
        // getConfig("host"), getConfig("slaveId") 등이 설정 값과 일치
        Assertions.assertAll(
                () -> Assertions.assertEquals("localhost", reader.getConfig("host")),
                () -> Assertions.assertEquals(5023, reader.getConfig("port")),
                () -> Assertions.assertEquals(1, reader.getConfig("slaveId")),
                () -> Assertions.assertEquals(0, reader.getConfig("startAddress")),
                () -> Assertions.assertEquals(2, reader.getConfig("count"))
        );
    }

    @Test
    @DisplayName("연결 성공")
    void test4() {
        // initialize() 후 isConnected()가 true
        reader.initialize();

        Assertions.assertTrue(reader.isConnected());
    }

    @Test
    @DisplayName("레지스터 읽기")
    void test5() {
        // trigger 메시지 전송 후 CollectorNode에서 레지스터 값이 포함된 메시지 수신
        reader.initialize();

        simulator.setRegister(0, 255);
        simulator.setRegister(1, 60);

        reader.process(new Message(Map.of("trigger", true)));

        Message result = connection.poll();

        Assertions.assertNotNull(result);
    }

    @Test
    @DisplayName("registerMapping 적용")
    void test6() {
        // 매핑이 설정된 경우 의미 있는 키(temperature, humidity)로 변환됨
        reader.initialize();

        simulator.setRegister(0, 255);
        simulator.setRegister(1, 60);

        reader.process(new Message(Map.of("trigger", true)));

        Message result = connection.poll();

        Assertions.assertAll(
                () -> Assertions.assertEquals(25.5, (double) result.get("temperature")),
                () -> Assertions.assertEquals(60.0, (double) result.get("humidity"))
        );
    }

    @Test
    @DisplayName("읽기 실패 시 에러 포트")
    void test7() {
        // 존재하지 않는 주소를 읽으면 "error" 포트로 에러 메시지 전달
        reader.initialize();
        simulator.stop();

        reader.process(new Message(Map.of("trigger", true)));

        Message errorMessage = errorConnection.poll();

        Assertions.assertAll(
                () -> Assertions.assertNotNull(errorMessage),
                () -> Assertions.assertTrue(errorMessage.hasKey("error"))
        );
    }

    @Test
    @DisplayName("shutdown 후 연결 해제")
    void test8() {
        // shutdown() 후 isConnected()가 false
        reader.initialize();
        reader.shutdown();

        Assertions.assertFalse(reader.isConnected());
    }

}