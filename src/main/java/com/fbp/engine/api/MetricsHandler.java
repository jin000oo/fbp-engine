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

package com.fbp.engine.api;

import com.fbp.engine.metrics.MetricsCollector;
import com.fbp.engine.metrics.NodeMetrics;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.Map;

public class MetricsHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            ApiResponse.sendError(exchange, 405, "Method Not Allowed");
            return;
        }

        String path = exchange.getRequestURI().getPath();

        if (path.startsWith("/nodes/") && path.endsWith("/stats")) {
            String nodeId = path.substring("/nodes/".length(), path.length() - "/stats".length());

            NodeMetrics nodeMetrics = MetricsCollector.getInstance().getNodeMetrics(nodeId);

            if (nodeMetrics != null) {
                Map<String, Object> stats = Map.of(
                        "processed", nodeMetrics.getProcessed().get(),
                        "errors", nodeMetrics.getErrors().get(),
                        "avgTimeMs", nodeMetrics.getAverageTimeMs()
                );

                ApiResponse.send(exchange, 200, stats);
            } else {
                ApiResponse.sendError(exchange, 404, "Not Found");
            }
        } else {
            ApiResponse.sendError(exchange, 404, "Not Found");
        }
    }

}
