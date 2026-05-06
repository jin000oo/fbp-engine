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
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Predicate;

public class TimeWindowRuleNode extends AbstractNode {

    private final Predicate<Message> condition;

    private final long windowMs;

    private final int threshold;

    private final Queue<Long> events = new LinkedList<>();

    public TimeWindowRuleNode(String id, Predicate<Message> condition, long windowMs, int threshold) {
        super(id);
        this.condition = condition;
        this.windowMs = windowMs;
        this.threshold = threshold;
        addInputPort("in");
        addOutputPort("alert");
        addOutputPort("pass");
    }

    @Override
    public synchronized void onProcess(Message message) {
        long now = System.currentTimeMillis();

        if (condition.test(message)) {
            events.add(now);
        }

        while (!events.isEmpty() && (now - events.peek() > windowMs)) {
            events.poll();
        }

        if (events.size() >= threshold) {
            send("alert", message);
        } else {
            send("pass", message);
        }
    }

    @Override
    public void initialize() {
    }

    @Override
    public void shutdown() {
    }

}
