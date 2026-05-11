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

import com.fbp.engine.message.Message;
import com.fbp.engine.rule.RuleExpression;
import lombok.Getter;

public class RoutingRule {

    private final RuleExpression expression;

    @Getter
    private final String targetPort;

    public RoutingRule(String expressionStr, String targetPort) {
        this.expression = RuleExpression.parse(expressionStr);
        this.targetPort = targetPort;
    }

    public boolean matches(Message message) {
        return expression.test(message);
    }

}
