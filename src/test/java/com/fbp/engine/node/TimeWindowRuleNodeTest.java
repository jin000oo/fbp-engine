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
import com.fbp.engine.core.RuleExpression;
import com.fbp.engine.message.Message;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TimeWindowRuleNodeTest {

    TimeWindowRuleNode timeWindowRule;

    Connection alertConnection;

    Connection passConnection;

    @BeforeEach
    void setUp() {
        timeWindowRule = new TimeWindowRuleNode(
                "time-window-rule", RuleExpression.parse("temperature > 30.0"), 500, 3);

        alertConnection = new Connection("alert");
        passConnection = new Connection("pass");

        timeWindowRule.getOutputPort("alert").connect(alertConnection);
        timeWindowRule.getOutputPort("pass").connect(passConnection);
    }

    @Test
    @DisplayName("기준 미달 → pass")
    void test1() {
        // 시간 창 내 조건 만족 횟수가 threshold 미만이면 pass
        Message message = new Message(Map.of("temperature", 35.0));

        timeWindowRule.process(message);
        timeWindowRule.process(message);

        Assertions.assertNotNull(passConnection.poll());
    }

    @Test
    @DisplayName("기준 도달 → alert")
    void test2() {
        // 시간 창 내 조건 만족 횟수가 threshold 이상이면 alert
        Message message = new Message(Map.of("temperature", 35.0));

        timeWindowRule.process(message);
        timeWindowRule.process(message);
        timeWindowRule.process(message);

        Assertions.assertNotNull(alertConnection.poll());
    }

    @Test
    @DisplayName("시간 창 만료")
    void test3() throws InterruptedException {
        // windowMs 이전의 이벤트는 카운트에서 제외됨
        Message message = new Message(Map.of("temperature", 35.0));

        timeWindowRule.process(message);
        timeWindowRule.process(message);

        Thread.sleep(1000);

        timeWindowRule.process(message);

        Assertions.assertNotNull(passConnection.poll());
    }

    @Test
    @DisplayName("조건 불만족 메시지")
    void test4() {
        // 조건 불만족 메시지는 이벤트로 기록되지 않음
        Message satisfyMessage = new Message(Map.of("temperature", 35.0));
        Message commonMessage = new Message(Map.of("temperature", 20.0));

        timeWindowRule.process(satisfyMessage);
        timeWindowRule.process(satisfyMessage);
        timeWindowRule.process(commonMessage);

        Assertions.assertNotNull(passConnection.poll());
    }

}