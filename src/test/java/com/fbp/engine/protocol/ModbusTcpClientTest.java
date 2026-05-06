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

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class ModbusTcpClientTest {

    ModbusTcpSimulator simulator;

    @BeforeEach
    void setUp() throws InterruptedException {
        simulator = new ModbusTcpSimulator(5022, 10);
        simulator.start();

        Thread.sleep(100);
    }

    @AfterEach
    void tearDown() {
        simulator.stop();
    }

    @Test
    @DisplayName("FC 03 요청 프레임 조립")
    void test1() throws IOException {
        // readHoldingRegisters()가 생성하는 바이트 배열이 MODBUS 사양과 일치 (unitId, startAddress, quantity가 올바른 위치에 올바른 값)
        try (ServerSocket serverSocket = new ServerSocket(5023)) {
            ModbusTcpClient client = new ModbusTcpClient("localhost", 5023);
            client.connect();

            Socket socket = serverSocket.accept();

            DataInputStream inputStream = new DataInputStream(socket.getInputStream());

            new Thread(() -> {
                try {
                    client.readHoldingRegisters(1, 10, 5);

                } catch (ModbusException | IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();

            int transactionId = inputStream.readUnsignedShort();
            int protocolId = inputStream.readUnsignedShort();
            int length = inputStream.readUnsignedShort();
            int unitId = inputStream.readUnsignedByte();
            int functionCode = inputStream.readUnsignedByte();
            int startAddress = inputStream.readUnsignedShort();
            int quantity = inputStream.readUnsignedShort();

            Assertions.assertAll(
                    () -> Assertions.assertEquals(1, unitId),
                    () -> Assertions.assertEquals(10, startAddress),
                    () -> Assertions.assertEquals(5, quantity)
            );

            client.disconnect();
        }
    }

    @Test
    @DisplayName("FC 06 요청 프레임 조립")
    void test2() throws IOException {
        // writeSingleRegister()가 생성하는 바이트 배열이 MODBUS 사양과 일치
        try (ServerSocket serverSocket = new ServerSocket(5024)) {
            ModbusTcpClient client = new ModbusTcpClient("localhost", 5024);
            client.connect();

            Socket socket = serverSocket.accept();

            DataInputStream inputStream = new DataInputStream(socket.getInputStream());

            new Thread(() -> {
                try {
                    client.writeSingleRegister(1, 10, 5);

                } catch (ModbusException | IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();

            int transactionId = inputStream.readUnsignedShort();
            int protocolId = inputStream.readUnsignedShort();
            int length = inputStream.readUnsignedShort();
            int unitId = inputStream.readUnsignedByte();
            int functionCode = inputStream.readUnsignedByte();
            int address = inputStream.readUnsignedShort();
            int value = inputStream.readUnsignedShort();

            Assertions.assertAll(
                    () -> Assertions.assertEquals(1, unitId),
                    () -> Assertions.assertEquals(10, address),
                    () -> Assertions.assertEquals(5, value)
            );

            client.disconnect();
        }
    }

    @Test
    @DisplayName("MBAP 헤더 구조")
    void test3() throws IOException {
        // Transaction ID, Protocol ID(0x0000), Length, Unit ID가 올바르게 조립됨
        try (ServerSocket serverSocket = new ServerSocket(5025)) {
            ModbusTcpClient client = new ModbusTcpClient("localhost", 5025);
            client.connect();

            Socket socket = serverSocket.accept();

            DataInputStream inputStream = new DataInputStream(socket.getInputStream());

            new Thread(() -> {
                try {
                    client.readHoldingRegisters(1, 10, 5);

                } catch (ModbusException | IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();

            int transactionId = inputStream.readUnsignedShort();
            int protocolId = inputStream.readUnsignedShort();
            int length = inputStream.readUnsignedShort();
            int unitId = inputStream.readUnsignedByte();

            Assertions.assertAll(
                    () -> Assertions.assertEquals(1, transactionId),
                    () -> Assertions.assertEquals(0, protocolId),
                    () -> Assertions.assertEquals(6, length),
                    () -> Assertions.assertEquals(1, unitId)
            );

            client.disconnect();
        }
    }

    @Test
    @DisplayName("Transaction ID 증가")
    void test4() throws IOException, ModbusException {
        // 연속 요청 시 Transaction ID가 1씩 증가
        ModbusTcpClient client = new ModbusTcpClient("localhost", 5022);
        client.connect();

        client.writeSingleRegister(1, 0, 10);
        int firstTransactionId = client.getTransactionId();

        client.writeSingleRegister(1, 0, 20);
        int secondTransactionId = client.getTransactionId();

        Assertions.assertEquals(firstTransactionId + 1, secondTransactionId);

        client.disconnect();
    }

    @Test
    @DisplayName("초기 상태")
    void test5() {
        // 생성 직후 isConnected()가 false
        ModbusTcpClient client = new ModbusTcpClient("localhost", 5022);
        Assertions.assertFalse(client.isConnected());
    }

    @Test
    @DisplayName("연결/해제")
    @Tag("integration")
    void test6() throws IOException {
        // connect() 후 isConnected() true, disconnect() 후 false
        ModbusTcpClient client = new ModbusTcpClient("localhost", 5022);

        client.connect();
        Assertions.assertTrue(client.isConnected());

        client.disconnect();
        Assertions.assertFalse(client.isConnected());
    }

    @Test
    @DisplayName("Holding Register 읽기")
    @Tag("integration")
    void test7() throws IOException, ModbusException {
        // 시뮬레이터에 설정한 값과 readHoldingRegisters() 반환값이 일치
        ModbusTcpClient client = new ModbusTcpClient("localhost", 5022);
        client.connect();

        client.writeSingleRegister(1, 0, 111);

        int[] results = client.readHoldingRegisters(1, 0, 1);

        Assertions.assertAll(
                () -> Assertions.assertEquals(1, results.length),
                () -> Assertions.assertEquals(111, results[0])
        );
    }

    @Test
    @DisplayName("다수 레지스터 읽기")
    @Tag("integration")
    void test8() throws IOException, ModbusException {
        // 5개 레지스터를 한 번에 읽어 배열 크기와 값이 모두 일치
        ModbusTcpClient client = new ModbusTcpClient("localhost", 5022);
        client.connect();

        client.writeSingleRegister(1, 0, 111);
        client.writeSingleRegister(1, 1, 222);
        client.writeSingleRegister(1, 2, 333);
        client.writeSingleRegister(1, 3, 444);
        client.writeSingleRegister(1, 4, 555);

        int[] results = client.readHoldingRegisters(1, 0, 5);

        Assertions.assertAll(
                () -> Assertions.assertEquals(5, results.length),
                () -> Assertions.assertEquals(111, results[0]),
                () -> Assertions.assertEquals(222, results[1]),
                () -> Assertions.assertEquals(333, results[2]),
                () -> Assertions.assertEquals(444, results[3]),
                () -> Assertions.assertEquals(555, results[4])
        );
    }

    @Test
    @DisplayName("Single Register 쓰기")
    @Tag("integration")
    void test9() throws IOException, ModbusException {
        // writeSingleRegister() 후 시뮬레이터의 getRegister()로 값 변경 확인
        int firstRegister = simulator.getRegister(0);

        ModbusTcpClient client = new ModbusTcpClient("localhost", 5022);
        client.connect();

        client.writeSingleRegister(1, 0, 111);

        Assertions.assertNotEquals(firstRegister, simulator.getRegister(0));
    }

    @Test
    @DisplayName("쓰기 후 읽기")
    @Tag("integration")
    void test10() throws IOException, ModbusException {
        // 쓰기 → 읽기 순서로 값이 일관되게 반영됨
        ModbusTcpClient client = new ModbusTcpClient("localhost", 5022);
        client.connect();

        client.writeSingleRegister(1, 0, 111);
        client.writeSingleRegister(1, 1, 222);
        client.writeSingleRegister(1, 2, 333);

        int[] results = client.readHoldingRegisters(1, 0, 3);

        Assertions.assertAll(
                () -> Assertions.assertEquals(3, results.length),
                () -> Assertions.assertEquals(111, results[0]),
                () -> Assertions.assertEquals(222, results[1]),
                () -> Assertions.assertEquals(333, results[2])
        );
    }

    @Test
    @DisplayName("에러 응답 처리")
    @Tag("integration")
    void test11() throws IOException {
        // 존재하지 않는 주소를 읽으면 ModbusException 발생, exceptionCode가 ILLEGAL_DATA_ADDRESS
        ModbusTcpClient client = new ModbusTcpClient("localhost", 5022);
        client.connect();

        ModbusException modbusException = Assertions.assertThrows(ModbusException.class, () -> {
            client.readHoldingRegisters(1, 999, 1);
        });

        Assertions.assertEquals(0x02, modbusException.getExceptionCode());

        client.disconnect();
    }

    @Test
    @DisplayName("소켓 타임아웃")
    @Tag("integration")
    void test12() {
        // 시뮬레이터를 중지한 상태에서 요청 시 SocketTimeoutException 또는 IOException 발생
        simulator.stop();

        ModbusTcpClient stopClient = new ModbusTcpClient("localhost", 5022);

        Assertions.assertThrows(ConnectException.class, stopClient::connect);
    }

}