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

import com.fbp.engine.core.Flow;
import java.util.List;
import java.util.Map;

public class FlowMetrics {

    public static Map<String, Object> collect(Flow flow) {
        List<Map<String, Object>> nodeMetrics = flow.getNodes().values().stream()
                .map(node -> {
                    NodeMetrics metrics = MetricsCollector.getInstance().getNodeMetrics(node.getId());

                    if (metrics != null) {
                        return Map.<String, Object>of(
                                "id", node.getId(),
                                "processed", metrics.getProcessed().get(),
                                "errors", metrics.getErrors().get(),
                                "avgTimeMs", metrics.getAverageTimeMs()
                        );
                    } else {
                        return Map.<String, Object>of(
                                "id", node.getId(),
                                "processed", 0L,
                                "errors", 0L,
                                "avgTimeMs", 0.0
                        );
                    }
                }).toList();

        return Map.of("nodes", nodeMetrics);
    }
}
