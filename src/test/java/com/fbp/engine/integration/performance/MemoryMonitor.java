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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MemoryMonitor {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final List<Long> heapUsageHistory = new ArrayList<>();

    public void start(long intervalMs) {
        scheduler.scheduleAtFixedRate(() -> {
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            heapUsageHistory.add(usedMemory);
            log.info("[MemoryMonitor] Used Heap: {} MB", usedMemory / (1024 * 1024));
        }, 0, intervalMs, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        scheduler.shutdownNow();
    }

    public boolean isIncreasing() {
        if (heapUsageHistory.size() < 2) {
            return false;
        }

        double firstHalfAvg = heapUsageHistory.stream()
                .limit(heapUsageHistory.size() / 2)
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);

        long lastValue = heapUsageHistory.get(heapUsageHistory.size() - 1);

        return lastValue > firstHalfAvg * 1.5;
    }

}
