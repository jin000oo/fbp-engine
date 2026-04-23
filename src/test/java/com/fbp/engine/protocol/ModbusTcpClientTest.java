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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ModbusTcpClientTest {

    ModbusTcpSimulator simulator;

    @BeforeEach
    void setUp() {
        simulator = new ModbusTcpSimulator(5021, 10);
        simulator.start();
    }

    @AfterEach
    void tearDown() {
        simulator.stop();
    }
    
    @Test
    @DisplayName("FC 03 요청 프레임 조립")
    void test1() {
        // readHoldingRegisters()가 생성하는 바이트 배열이 MODBUS 사양과 일치 (unitId, startAddress, quantity가 올바른 위치에 올바른 값)
    }

    @Test
    @DisplayName("FC 06 요청 프레임 조립")
    void test2() {
        // writeSingleRegister()가 생성하는 바이트 배열이 MODBUS 사양과 일치
    }

    @Test
    @DisplayName("MBAP 헤더 구조")
    void test3() {
        // Transaction ID, Protocol ID(0x0000), Length, Unit ID가 올바르게 조립됨
    }

    @Test
    @DisplayName("Transaction ID 증가")
    void test4() {
        // 연속 요청 시 Transaction ID가 1씩 증가
    }

    @Test
    @DisplayName("초기 상태")
    void test5() {
        // 생성 직후 isConnected()가 false
    }

    @Test
    @DisplayName("연결/해제")
    void test6() {
        // connect() 후 isConnected() true, disconnect() 후 false
    }

    @Test
    @DisplayName("Holding Register 읽기")
    void test7() {
        // 시뮬레이터에 설정한 값과 readHoldingRegisters() 반환값이 일치
    }

    @Test
    @DisplayName("다수 레지스터 읽기")
    void test8() {
        // 5개 레지스터를 한 번에 읽어 배열 크기와 값이 모두 일치
    }

    @Test
    @DisplayName("Single Register 쓰기")
    void test9() {
        // writeSingleRegister() 후 시뮬레이터의 getRegister()로 값 변경 확인
    }

    @Test
    @DisplayName("쓰기 후 읽기")
    void test10() {
        // 쓰기 → 읽기 순서로 값이 일관되게 반영됨
    }

    @Test
    @DisplayName("에러 응답 처리")
    void test11() {
        // 존재하지 않는 주소를 읽으면 ModbusException 발생, exceptionCode가 ILLEGAL_DATA_ADDRESS
    }

    @Test
    @DisplayName("소켓 타임아웃")
    void test12() {
        // 시뮬레이터를 중지한 상태에서 요청 시 SocketTimeoutException 또는 IOException 발생
    }

}