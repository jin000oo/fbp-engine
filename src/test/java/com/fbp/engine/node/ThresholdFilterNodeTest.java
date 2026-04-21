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

class ThresholdFilterNodeTest {

    ThresholdFilterNode filter;

    Connection connectionAlert;

    Connection connectionNormal;

    @BeforeEach
    void setUp() {
        filter = new ThresholdFilterNode("filter", "temperature", 30);

        connectionAlert = new Connection("alert");
        connectionNormal = new Connection("normal");

        filter.getOutputPort("alert").connect(connectionAlert);
        filter.getOutputPort("normal").connect(connectionNormal);
    }

    @Test
    @DisplayName("초과 → alert 포트")
    void test1() {
        // 임계값을 초과하는 메시지가 "alert" 포트로 전달됨
        filter.process(new Message(Map.of("temperature", 35)));

        Assertions.assertAll(
                () -> Assertions.assertEquals(1, connectionAlert.getBufferSize()),
                () -> Assertions.assertEquals(0, connectionNormal.getBufferSize())
        );
    }

    @Test
    @DisplayName("이하 → normal 포트")
    void test2() {
        // 임계값 이하인 메시지가 "normal" 포트로 전달됨
        filter.process(new Message(Map.of("temperature", 25)));

        Assertions.assertAll(
                () -> Assertions.assertEquals(0, connectionAlert.getBufferSize()),
                () -> Assertions.assertEquals(1, connectionNormal.getBufferSize())
        );
    }

    @Test
    @DisplayName("경계값 (정확히 같은 값)")
    void test3() {
        // 임계값과 같은 값의 분기 방향 확인 ("초과"이므로 normal)
        filter.process(new Message(Map.of("temperature", 30)));

        Assertions.assertAll(
                () -> Assertions.assertEquals(0, connectionAlert.getBufferSize()),
                () -> Assertions.assertEquals(1, connectionNormal.getBufferSize())
        );
    }

    @Test
    @DisplayName("키 없는 메시지")
    void test4() {
        // 대상 필드가 없는 메시지 수신 시 예외 없이 처리
        Assertions.assertAll(
                () -> Assertions.assertDoesNotThrow(() -> filter.process(new Message(Map.of("humidity", 50)))),
                () -> Assertions.assertEquals(0, connectionAlert.getBufferSize()),
                () -> Assertions.assertEquals(0, connectionNormal.getBufferSize())
        );
    }

    @Test
    @DisplayName("양쪽 동시 검증")
    void test5() {
        // alert와 normal 양쪽에 CollectorNode를 연결하여, 각각 올바른 메시지만 수신되는지 확인
        filter.process(new Message(Map.of("temperature", 35)));
        filter.process(new Message(Map.of("temperature", 25)));
        filter.process(new Message(Map.of("temperature", 40)));

        Assertions.assertAll(
                () -> Assertions.assertEquals(2, connectionAlert.getBufferSize()),
                () -> Assertions.assertEquals(1, connectionNormal.getBufferSize())
        );
    }

}