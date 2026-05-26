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

package com.fbp.engine.integration.performance;

import com.fbp.engine.core.Node;
import com.fbp.engine.message.Message;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class LoadTester {

    private final Node targetNode;
    private final AtomicLong sentCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);

    public LoadTester(Node targetNode) {
        this.targetNode = targetNode;
    }

    public PerformanceResult run(int messageCount) {
        long startTime = System.nanoTime();

        for (int i = 0; i < messageCount; i++) {
            try {
                targetNode.receive(new Message(Map.of("data", i, "timestamp", System.nanoTime())));
                sentCount.incrementAndGet();
            } catch (Exception e) {
                errorCount.incrementAndGet();
            }
        }

        long endTime = System.nanoTime();
        long durationNs = endTime - startTime;
        long durationMs = durationNs / 1_000_000;

        double throughput = (double) sentCount.get() / (durationNs / 1_000_000_000.0);

        return PerformanceResult.builder()
                .totalMessages(sentCount.get())
                .durationMs(durationMs)
                .throughput(throughput)
                .errorCount(errorCount.get())
                .errorRate((double) errorCount.get() / messageCount)
                .build();
    }

}
