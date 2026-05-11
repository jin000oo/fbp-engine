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

import com.fbp.engine.core.AbstractNode;
import com.fbp.engine.message.Message;
import java.util.function.Predicate;

public class RuleNode extends AbstractNode {

    private Predicate<Message> condition;

    public RuleNode(String id, Predicate<Message> condition) {
        super(id);
        this.condition = condition;
        addInputPort("in");
        addOutputPort("match");
        addOutputPort("mismatch");
    }

    public RuleNode(String id, String expression) {
        this(id, RuleExpression.parse(expression));
    }

    @Override
    public void onProcess(Message message) {
        if (condition != null) {
            if (condition.test(message)) {
                send("match", message);
            } else {
                send("mismatch", message);
            }
        }
    }

    @Override
    public void initialize() {
    }

    @Override
    public void shutdown() {
    }

}
