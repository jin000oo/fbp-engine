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

package com.fbp.engine.metrics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MetricsCollector {

    private static final MetricsCollector INSTANCE = new MetricsCollector();

    private final Map<String, NodeMetrics> nodeMetricsMap = new ConcurrentHashMap<>();

    public static MetricsCollector getInstance() {
        return INSTANCE;
    }

    public void recordProcessing(String nodeId, long durationNs, boolean success) {
        nodeMetricsMap.computeIfAbsent(nodeId, n -> new NodeMetrics()).record(durationNs, success);
    }

    public NodeMetrics getNodeMetrics(String nodeId) {
        return nodeMetricsMap.get(nodeId);
    }

    public Map<String, NodeMetrics> getAllMetrics() {
        return nodeMetricsMap;
    }

    public void reset() {
        nodeMetricsMap.clear();
    }

}
