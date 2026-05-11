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

package com.fbp.engine.rule;

import com.fbp.engine.core.Connection;
import com.fbp.engine.message.Message;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RuleNodeTest {

    RuleNode rule;

    Connection matchConnection;

    Connection mismatchConnection;

    @BeforeEach
    void setUp() throws InterruptedException {
        rule = new RuleNode("rule", "temperature > 30.0");

        matchConnection = new Connection("match");
        mismatchConnection = new Connection("mismatch");

        rule.getOutputPort("match").connect(matchConnection);
        rule.getOutputPort("mismatch").connect(mismatchConnection);
    }

    @Test
    @DisplayName("조건 만족 → match")
    void test1() {
        // 조건을 만족하는 메시지가 "match" 포트로 전달됨
        rule.process(new Message(Map.of("temperature", 35.0)));

        Assertions.assertNotNull(matchConnection.poll());
    }

    @Test
    @DisplayName("조건 불만족 → mismatch")
    void test2() {
        // 조건을 만족하지 않는 메시지가 "mismatch" 포트로 전달됨
        rule.process(new Message(Map.of("temperature", 25.0)));

        Assertions.assertNotNull(mismatchConnection.poll());
    }

    @Test
    @DisplayName("포트 구성")
    void test3() {
        // "in", "match", "mismatch" 포트가 모두 존재
        Assertions.assertAll(
                () -> Assertions.assertNotNull(rule.getInputPort("in")),
                () -> Assertions.assertNotNull(rule.getOutputPort("match")),
                () -> Assertions.assertNotNull(rule.getOutputPort("mismatch"))
        );
    }

    @Test
    @DisplayName("null 필드 처리")
    void test4() {
        // 조건에 사용되는 필드가 없는 메시지가 예외 없이 처리됨
        Assertions.assertAll(
                () -> Assertions.assertDoesNotThrow(() -> {
                    rule.process(new Message(Map.of("something", 50.0)));
                }),
                () -> Assertions.assertNotNull(mismatchConnection.poll())
        );
    }

    @Test
    @DisplayName("다수 메시지 분기")
    void test5() {
        // 혼합된 메시지를 연속 처리 시 각각 올바른 포트로 분기됨
        rule.process(new Message(Map.of("temperature", 35.0)));
        rule.process(new Message(Map.of("temperature", 35.0)));
        rule.process(new Message(Map.of("temperature", 20.0)));

        Assertions.assertAll(
                () -> Assertions.assertNotNull(matchConnection.poll()),
                () -> Assertions.assertNotNull(matchConnection.poll()),
                () -> Assertions.assertNotNull(mismatchConnection.poll())
        );
    }

}