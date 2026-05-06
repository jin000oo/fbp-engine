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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CompositeRuleNodeTest {

    @Test
    @DisplayName("AND — 모두 만족")
    void test1() {
        // 두 조건 모두 true일 때 match
        CompositeRuleNode composite = new CompositeRuleNode("composite", CompositeRuleNode.Operator.AND);
        composite.addCondition("temperature > 30.0");
        composite.addCondition("humidity < 50.0");

        Assertions.assertTrue(isMatched(composite, new Message(Map.of(
                "temperature", 35.0,
                "humidity", 40.0
        )), true));
    }

    @Test
    @DisplayName("AND — 하나 불만족")
    void test2() {
        // 하나라도 false이면 mismatch
        CompositeRuleNode composite = new CompositeRuleNode("composite", CompositeRuleNode.Operator.AND);
        composite.addCondition("temperature > 30.0");
        composite.addCondition("humidity < 50.0");

        Assertions.assertFalse(isMatched(composite, new Message(Map.of(
                "temperature", 35.0,
                "humidity", 60.0
        )), false));
    }

    @Test
    @DisplayName("OR — 하나 만족")
    void test3() {
        // 하나만 true여도 match
        CompositeRuleNode composite = new CompositeRuleNode("composite", CompositeRuleNode.Operator.OR);
        composite.addCondition("temperature > 30.0");
        composite.addCondition("humidity < 50.0");

        Assertions.assertTrue(isMatched(composite, new Message(Map.of(
                "temperature", 25.0,
                "humidity", 40.0
        )), true));
    }

    @Test
    @DisplayName("OR — 모두 불만족")
    void test4() {
        // 모두 false이면 mismatch
        CompositeRuleNode composite = new CompositeRuleNode("composite", CompositeRuleNode.Operator.OR);
        composite.addCondition("temperature > 30.0");
        composite.addCondition("humidity < 50.0");

        Assertions.assertFalse(isMatched(composite, new Message(Map.of(
                "temperature", 25.0,
                "humidity", 60.0
        )), false));
    }

    @Test
    @DisplayName("빈 조건")
    void test5() {
        // 조건이 없을 때의 기본 동작 (AND: match, OR: mismatch)
        CompositeRuleNode andComposite = new CompositeRuleNode("and-composite", CompositeRuleNode.Operator.AND);
        CompositeRuleNode orComposite = new CompositeRuleNode("or-composite", CompositeRuleNode.Operator.OR);

        Message message = new Message(Map.of("value", 1));

        Assertions.assertAll(
                () -> Assertions.assertTrue(isMatched(andComposite, message, true)),
                () -> Assertions.assertFalse(isMatched(orComposite, message, false))
        );
    }

    private boolean isMatched(CompositeRuleNode composite, Message message, boolean expect) {
        Connection matchConnection = new Connection("match");
        Connection mismatchConnection = new Connection("mismatch");

        composite.getOutputPort("match").connect(matchConnection);
        composite.getOutputPort("mismatch").connect(mismatchConnection);

        composite.process(message);

        if (expect) {
            return matchConnection.poll() != null;
        } else {
            return mismatchConnection.poll() == null;
        }
    }

}