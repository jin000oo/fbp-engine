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

import com.fbp.engine.core.Flow;
import com.fbp.engine.engine.FlowManager;
import com.fbp.engine.metrics.FlowMetrics;
import com.fbp.engine.parser.FlowDefinition;
import com.fbp.engine.parser.JsonFlowParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class FlowHandler implements HttpHandler {

    private final FlowManager flowManager;

    private final JsonFlowParser parser;

    public FlowHandler(FlowManager flowManager, JsonFlowParser parser) {
        this.flowManager = flowManager;
        this.parser = parser;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("GET".equals(method) && "/flows".equals(path)) {
                List<Map<String, String>> flows = flowManager.list().stream()
                        .map(f -> Map.of(
                                "id", f.getId(),
                                "status", f.getState().name())
                        ).toList();

                ApiResponse.send(exchange, 200, flows);
            } else if ("POST".equals(method) && "/flows".equals(path)) {
                FlowDefinition flowDefinition = parser.parse(exchange.getRequestBody());
                flowManager.deploy(flowDefinition);

                Map<String, Object> flows = Map.of(
                        "id", flowDefinition.getId(),
                        "status", "RUNNING"
                );

                ApiResponse.send(exchange, 201, flows);
            } else if ("DELETE".equals(method) && path.startsWith("/flows/")) {
                String flowId = path.substring("/flows/".length());

                if (!flowId.contains("/")) {
                    flowManager.remove(flowId);

                    Map<String, Object> flows = Map.of(
                            "message", "Deleted flow: " + flowId
                    );

                    ApiResponse.send(exchange, 200, flows);
                } else {
                    ApiResponse.sendError(exchange, 404, "Not Found");
                }
            } else if (path.startsWith("/flows/") && path.endsWith("/metrics")) {
                String flowId = path.substring("/flows/".length(), path.length() - "/metrics".length());

                Flow flow = flowManager.list().stream()
                        .filter(f -> f.getId().endsWith(flowId))
                        .findFirst()
                        .orElse(null);

                if (flow != null) {
                    ApiResponse.send(exchange, 200, FlowMetrics.collect(flow));
                } else {
                    ApiResponse.sendError(exchange, 404, "Not Found");
                }
            } else {
                ApiResponse.sendError(exchange, 404, "Not Found");
            }

        } catch (IllegalArgumentException e) {
            ApiResponse.sendError(exchange, 404, "Not Found");

        } catch (Exception e) {
            ApiResponse.sendError(exchange, 400, "Bad Request");
        }
    }

}
