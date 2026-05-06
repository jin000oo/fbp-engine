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

package com.fbp.engine.core;

import com.fbp.engine.message.Message;
import java.util.function.Predicate;

public class RuleExpression implements Predicate<Message> {

    private final String field;

    private final String operator;

    private final Object value;

    public RuleExpression(String field, String operator, Object value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    public static RuleExpression parse(String expression) {
        String[] parts = expression.trim().split("\\s+");
        if (parts.length != 3) {
            throw new IllegalArgumentException("올바르지 않은 형태의 조건식");
        }

        return new RuleExpression(parts[0], parts[1], parts[2]);
    }

    public boolean evaluate(Message message) {
        if (!message.hasKey(field)) {
            return false;
        }

        Object fieldValue = message.get(field);

        try {
            double actual = ((Number) fieldValue).doubleValue();
            double target = Double.parseDouble((String) value);

            return switch (operator) {
                case ">" -> actual > target;
                case ">=" -> actual >= target;
                case "<" -> actual < target;
                case "<=" -> actual <= target;
                case "==" -> actual == target;
                case "!=" -> actual != target;
                default -> false;
            };

        } catch (Exception e) {
            String actual = String.valueOf(fieldValue);
            String target = String.valueOf(value);

            return switch (operator) {
                case "==" -> actual.equals(target);
                case "!=" -> !actual.equals(target);
                default -> false;
            };
        }
    }

    @Override
    public boolean test(Message message) {
        return evaluate(message);
    }

}
