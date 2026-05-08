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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FilterNodeTest {

    FilterNode filter;

    @Mock
    Connection connection;

    @BeforeEach
    void setUp() {
        filter = new FilterNode("filter-1", "temperature", 30.0);
        filter.getOutputPort("out").connect(connection);
    }

    @Test
    @DisplayName("조건 만족 시 통과")
    void test1() {
        // threshold 이상인 값을 가진 메시지가 OutputPort로 전달됨
        Message message = new Message(Map.of("temperature", 35.0));
        filter.process(message);

        Mockito.verify(connection, Mockito.times(1)).deliver(message);
    }

    @Test
    @DisplayName("조건 미달 시 차단")
    void test2() {
        // threshold 미만인 값을 가진 메시지가 OutputPort로 전달되지 않음
        Message message = new Message(Map.of("temperature", 25.0));
        filter.process(message);

        Mockito.verify(connection, Mockito.never()).deliver(Mockito.any());
    }

    @Test
    @DisplayName("경계값 처리")
    void test3() {
        // threshold와 정확히 같은 값의 동작 확인 (이상 조건이므로 통과)
        Message message = new Message(Map.of("temperature", 30.0));
        filter.process(message);

        Mockito.verify(connection, Mockito.times(1)).deliver(message);
    }

    @Test
    @DisplayName("키 없는 메시지")
    void test4() {
        // 필터링 대상 키가 없는 메시지가 들어왔을 때 예외 없이 처리됨
        Message message = new Message(Map.of("humidity", 60.0));
        Assertions.assertDoesNotThrow(() -> filter.process(message));
    }

    @Test
    @DisplayName("조건 만족 → send 호출")
    void test5() {
        // threshold 이상인 메시지가 OutputPort로 전달됨
        Message message = new Message(Map.of("temperature", 35.0));
        filter.process(message);

        Mockito.verify(connection, Mockito.times(1)).deliver(message);
    }

    @Test
    @DisplayName("조건 미달 → 차단")
    void test6() {
        // threshold 미만인 메시지가 OutputPort로 전달되지 않음
        Message message = new Message(Map.of("temperature", 25.0));
        filter.process(message);

        Assertions.assertEquals(0, connection.getBufferSize());
    }

    @Test
    @DisplayName("포트 구성 확인")
    void test7() {
        // getInputPort("in")과 getOutputPort("out")이 null이 아님
        Assertions.assertAll(
                () -> Assertions.assertNotNull(filter.getInputPort("in")),
                () -> Assertions.assertNotNull(filter.getOutputPort("out"))
        );
    }

}