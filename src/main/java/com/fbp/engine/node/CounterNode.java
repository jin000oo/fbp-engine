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
import java.util.concurrent.atomic.AtomicInteger;

public class CounterNode extends AbstractNode {

    private final AtomicInteger count = new AtomicInteger(0);

    public CounterNode(String id) {
        super(id);
        addInputPort("in");
        addOutputPort("out");
    }

    @Override
    public void onProcess(Message message) {
        int currentCount = count.incrementAndGet();

        Message newMessage = message.withEntry("count", currentCount);
        send("out", newMessage);
    }

    @Override
    public void initialize() {
    }

    @Override
    public void shutdown() {
        System.out.printf("[%s] 총 처리 메시지: %d건%n", getId(), count.get());
    }

}
