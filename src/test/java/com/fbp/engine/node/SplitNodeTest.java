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

class SplitNodeTest {

    SplitNode split;

    Connection matchConnection;

    Connection mismatchConnection;

    @BeforeEach
    void setUp() {
        split = new SplitNode("split-1", "score", 80.0);

        matchConnection = new Connection("match");
        mismatchConnection = new Connection("mismatch");

        split.getOutputPort("match").connect(matchConnection);
        split.getOutputPort("mismatch").connect(mismatchConnection);
    }

    @Test
    @DisplayName("조건 만족 → match 포트")
    void test1() {
        // threshold 이상인 메시지가 "match" OutputPort로 전달됨
        split.process(new Message(Map.of("score", 90.0)));

        Assertions.assertAll(
                () -> Assertions.assertEquals(1, matchConnection.getBufferSize()),
                () -> Assertions.assertEquals(0, mismatchConnection.getBufferSize())
        );
    }

    @Test
    @DisplayName("조건 미달 → mismatch 포트")
    void test2() {
        // threshold 미만인 메시지가 "mismatch" OutputPort로 전달됨
        split.process(new Message(Map.of("score", 79.9)));

        Assertions.assertAll(
                () -> Assertions.assertEquals(0, matchConnection.getBufferSize()),
                () -> Assertions.assertEquals(1, mismatchConnection.getBufferSize())
        );
    }

    @Test
    @DisplayName("양쪽 동시 확인")
    void test3() {
        // 만족/미달 메시지를 각각 보내면 양쪽 포트에서 각각 수신됨
        split.process(new Message(Map.of("score", 90.0)));
        split.process(new Message(Map.of("score", 50.0)));

        Assertions.assertAll(
                () -> Assertions.assertEquals(1, matchConnection.getBufferSize()),
                () -> Assertions.assertEquals(1, mismatchConnection.getBufferSize())
        );
    }

    @Test
    @DisplayName("경계값 처리")
    void test4() {
        // threshold와 같은 값의 분기 방향 확인
        split.process(new Message(Map.of("score", 80.0)));

        Assertions.assertAll(
                () -> Assertions.assertEquals(1, matchConnection.getBufferSize()),
                () -> Assertions.assertEquals(0, mismatchConnection.getBufferSize())
        );
    }

}