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

import com.fbp.engine.message.Message;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RuleExpressionTest {

    @Test
    @DisplayName("파싱 — 숫자 비교")
    void test1() {
        // "temperature > 30.0" 파싱 후 evaluate()가 올바른 결과 반환
        RuleExpression expression = RuleExpression.parse("temperature > 30.0");

        Message message1 = new Message(Map.of("temperature", 35.5));
        Message message2 = new Message(Map.of("temperature", 25.0));

        Assertions.assertAll(
                () -> Assertions.assertTrue(expression.test(message1)),
                () -> Assertions.assertFalse(expression.test(message2))
        );
    }

    @Test
    @DisplayName("파싱 — 문자열 비교")
    void test2() {
        // "status == ON" 파싱 후 evaluate()가 올바른 결과 반환
        RuleExpression expression = RuleExpression.parse("status == ON");

        Message message1 = new Message(Map.of("status", "ON"));
        Message message2 = new Message(Map.of("status", "OFF"));

        Assertions.assertAll(
                () -> Assertions.assertTrue(expression.test(message1)),
                () -> Assertions.assertFalse(expression.test(message2))
        );
    }

    @Test
    @DisplayName("모든 연산자")
    void test3() {
        // >, >=, <, <=, ==, != 각각에 대해 올바른 비교
        Message message = new Message(Map.of("value", 10.0));

        Assertions.assertAll(
                () -> Assertions.assertFalse(RuleExpression.parse("value > 10.0").test(message)),
                () -> Assertions.assertTrue(RuleExpression.parse("value >= 10.0").test(message)),
                () -> Assertions.assertFalse(RuleExpression.parse("value < 10.0").test(message)),
                () -> Assertions.assertTrue(RuleExpression.parse("value <= 10.0").test(message)),
                () -> Assertions.assertTrue(RuleExpression.parse("value == 10.0").test(message)),
                () -> Assertions.assertFalse(RuleExpression.parse("value != 10.0").test(message))
        );
    }

    @Test
    @DisplayName("잘못된 표현식")
    void test4() {
        // 파싱 불가한 문자열에 대해 적절한 예외 발생
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            RuleExpression.parse("temperature >");
        });
    }

    @Test
    @DisplayName("필드 없음")
    void test5() {
        // 메시지에 해당 필드가 없을 때의 동작 확인
        RuleExpression expression = RuleExpression.parse("temperature > 30.0");

        Assertions.assertFalse(expression.test(new Message(Map.of("other", 50.0))));
    }

}