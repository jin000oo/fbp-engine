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

package com.fbp.engine.protocol;

import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ModbusTcpSimulatorTest {

    ModbusTcpSimulator simulator;

    @BeforeEach
    void setUp() {
        simulator = new ModbusTcpSimulator(5020, 10);
        simulator.start();
    }

    @AfterEach
    void tearDown() {
        simulator.stop();
    }

    @Test
    @DisplayName("시작/종료")
    void test1() {
        // start() 후 포트가 열리고, stop() 후 닫힘
        Assertions.assertDoesNotThrow(() -> simulator.setRegister(0, 100));
    }

    @Test
    @DisplayName("레지스터 초기값")
    void test2() {
        // setRegister() 후 getRegister()로 설정 값 확인
        simulator.setRegister(0, 100);

        Assertions.assertAll(
                () -> Assertions.assertEquals(100, simulator.getRegister(0)),
                () -> Assertions.assertEquals(0, simulator.getRegister(1))
        );
    }

    @Test
    @DisplayName("FC 03 응답")
    void test3() throws IOException, ModbusException {
        // ModbusTcpClient로 읽기 요청 시 설정된 레지스터 값이 응답됨
        simulator.setRegister(2, 255);

        ModbusTcpClient client = new ModbusTcpClient("localhost", 5020);
        client.connect();

        int[] result = client.readHoldingRegisters(1, 2, 1);

        Assertions.assertEquals(255, result[0]);

        client.disconnect();
    }

    @Test
    @DisplayName("FC 06 응답")
    void test4() throws IOException, ModbusException {
        // ModbusTcpClient로 쓰기 요청 시 레지스터 값이 변경되고 에코백 응답
        ModbusTcpClient client = new ModbusTcpClient("localhost", 5020);
        client.connect();

        client.writeSingleRegister(1, 5, 999);

        Assertions.assertEquals(999, simulator.getRegister(5));

        client.disconnect();
    }

    @Test
    @DisplayName("잘못된 주소 에러")
    void test5() throws IOException {
        // 범위를 벗어난 주소 요청 시 Exception Code 0x02 에러 응답
        ModbusTcpClient client = new ModbusTcpClient("localhost", 5020);
        client.connect();

        ModbusException exception = Assertions.assertThrows(ModbusException.class, () -> {
            client.readHoldingRegisters(1, 20, 1);
        });

        Assertions.assertEquals(0x02, exception.getExceptionCode());

        client.disconnect();
    }

    @Test
    @DisplayName("다중 클라이언트")
    void test6() throws IOException, ModbusException {
        // 2개 클라이언트가 동시 접속하여 독립적으로 요청/응답 가능
        ModbusTcpClient client1 = new ModbusTcpClient("localhost", 5020);
        ModbusTcpClient client2 = new ModbusTcpClient("localhost", 5020);
        client1.connect();
        client2.connect();

        client1.writeSingleRegister(1, 0, 100);
        client1.writeSingleRegister(1, 1, 200);

        Assertions.assertAll(
                () -> Assertions.assertEquals(100, simulator.getRegister(0)),
                () -> Assertions.assertEquals(200, simulator.getRegister(1))
        );

        client1.disconnect();
        client2.disconnect();
    }

}