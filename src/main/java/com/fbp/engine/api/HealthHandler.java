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

import com.fbp.engine.engine.FlowManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.Map;

public class HealthHandler implements HttpHandler {

    private final FlowManager flowManager;

    private final long startTime;

    public HealthHandler(FlowManager flowManager, long startTime) {
        this.flowManager = flowManager;
        this.startTime = startTime;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            ApiResponse.sendError(exchange, 405, "Method Not Allowed");
            return;
        }

        Map<String, Object> health = Map.of(
                "status", "UP",
                "uptime", System.currentTimeMillis() - startTime,
                "flowCount", flowManager.list().size()
        );

        ApiResponse.send(exchange, 200, health);
    }

}
