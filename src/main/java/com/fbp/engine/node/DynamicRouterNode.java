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
import java.util.ArrayList;
import java.util.List;

public class DynamicRouterNode extends AbstractNode {

    private final List<RoutingRule> rules = new ArrayList<>();

    public DynamicRouterNode(String id) {
        super(id);
        addInputPort("in");
        addOutputPort("default");
    }

    public DynamicRouterNode addRule(String expression, String portName) {
        rules.add(new RoutingRule(expression, portName));

        addOutputPort(portName);

        return this;
    }

    @Override
    public void onProcess(Message message) {
        for (RoutingRule rule : rules) {
            if (rule.matches(message)) {
                send(rule.getTargetPort(), message);
                return;
            }
        }

        send("default", message);
    }

    @Override
    public void initialize() {
    }

    @Override
    public void shutdown() {
    }

}
