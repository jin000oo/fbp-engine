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
import com.fbp.engine.parser.JsonFlowParser;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpApiServer {

    private final HttpServer server;

    private final FlowManager flowManager;

    private final JsonFlowParser parser = new JsonFlowParser();

    private final long startTime = System.currentTimeMillis();

    public HttpApiServer(int port, FlowManager flowManager) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.flowManager = flowManager;
        setupRoutes();
        this.server.setExecutor(null);
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    private void setupRoutes() {
        server.createContext("/flows", new FlowHandler(flowManager, parser));
        server.createContext("/nodes", new MetricsHandler());
        server.createContext("/health", new HealthHandler(flowManager, startTime));
    }

}
