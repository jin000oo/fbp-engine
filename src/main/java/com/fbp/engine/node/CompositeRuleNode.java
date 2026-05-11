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

import com.fbp.engine.core.AbstractNode;
import com.fbp.engine.message.Message;
import com.fbp.engine.rule.RuleExpression;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class CompositeRuleNode extends AbstractNode {

    public enum Operator {
        AND, OR
    }

    private final Operator operator;

    private final List<Predicate<Message>> conditions = new ArrayList<>();

    public CompositeRuleNode(String id, Operator operator) {
        super(id);
        this.operator = operator;
        addInputPort("in");
        addOutputPort("match");
        addOutputPort("mismatch");
    }

    public CompositeRuleNode addCondition(Predicate<Message> condition) {
        conditions.add(condition);

        return this;
    }

    public CompositeRuleNode addCondition(String expression) {
        conditions.add(RuleExpression.parse(expression));

        return this;
    }

    public CompositeRuleNode addCondition(String field, String op, Object value) {
        conditions.add(new RuleExpression(field, op, value));

        return this;
    }

    @Override
    public void onProcess(Message message) {
        boolean isMatch = operator == Operator.AND;

        for (Predicate<Message> condition : conditions) {
            boolean tempResult = condition.test(message);

            if (Operator.AND.equals(operator) && !tempResult) {
                isMatch = false;
                break;
            }

            if (Operator.OR.equals(operator) && tempResult) {
                isMatch = true;
                break;
            }
        }

        if (isMatch) {
            send("match", message);
        } else {
            send("mismatch", message);
        }
    }

    @Override
    public void initialize() {
    }

    @Override
    public void shutdown() {
    }

}
