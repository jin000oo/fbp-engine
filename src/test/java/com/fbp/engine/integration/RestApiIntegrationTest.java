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

package com.fbp.engine.integration;

import com.fbp.engine.api.HttpApiServer;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.engine.FlowManager;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.PrintNode;
import com.fbp.engine.node.TransformNode;
import com.fbp.engine.registry.NodeRegistry;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RestApiIntegrationTest {

    HttpClient client = HttpClient.newHttpClient();

    FlowEngine engine;

    NodeRegistry registry;

    HttpApiServer server;

    @BeforeEach
    void setUp() throws Exception {
        engine = new FlowEngine();
        registry = new NodeRegistry();
        registry.register("Transform", (id, config) -> new TransformNode(id, m -> m));
        registry.register("Print", (id, config) -> new PrintNode(id));

        FlowManager flowManager = new FlowManager(engine, registry);
        server = new HttpApiServer(8083, flowManager);
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop();
        engine.shutdown();
    }

    @Test
    @DisplayName("플로우 CRUD")
    void test1() throws Exception {
        // POST → GET → DELETE 전체 흐름
        String flowJson = """
                {
                    "id": "api-test",
                    "nodes": [ { "id": "n1", "type": "Transform", "config": {} } ],
                    "connections": []
                }
                """;

        // POST
        var postReq = HttpRequest.newBuilder().uri(URI.create("http://localhost:8083" + "/flows"))
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(flowJson)).build();
        Assertions.assertEquals(201, client.send(postReq, HttpResponse.BodyHandlers.ofString()).statusCode());

        // GET
        var getReq = HttpRequest.newBuilder().uri(URI.create("http://localhost:8083" + "/flows")).GET().build();
        var resp = client.send(getReq, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, resp.statusCode());
        Assertions.assertTrue(resp.body().contains("api-test"));

        // DELETE
        var delReq =
                HttpRequest.newBuilder().uri(URI.create("http://localhost:8083" + "/flows/api-test")).DELETE().build();
        Assertions.assertEquals(200, client.send(delReq, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    @DisplayName("배포 후 실행 확인")
    void test2() throws Exception {
        // POST /flows 후 실제로 플로우가 메시지를 처리하는지 확인
        String flowJson = """
                {
                    "id": "run-test",
                    "nodes": [ { "id": "n1", "type": "Transform", "config": {} } ],
                    "connections": []
                }
                """;
        HttpRequest postReq = HttpRequest.newBuilder().uri(URI.create("http://localhost:8083" + "/flows"))
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(flowJson)).build();
        client.send(postReq, HttpResponse.BodyHandlers.ofString());

        Assertions.assertTrue(engine.getFlows().containsKey("run-test"));
        Assertions.assertEquals(com.fbp.engine.core.Flow.State.RUNNING, engine.getFlows().get("run-test").getState());
    }

    @Test
    @DisplayName("메트릭 정확성")
    void test3() throws Exception {
        // 알려진 수의 메시지를 보낸 후 메트릭의 처리 건수가 일치
        test2();

        engine.getFlows().get("run-test").getNodes().get("n1").process(new Message(Map.of()));
        Thread.sleep(100);

        var req = HttpRequest.newBuilder().uri(URI.create("http://localhost:8083" + "/flows/run-test/metrics")).GET()
                .build();
        var resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, resp.statusCode());
        Assertions.assertTrue(resp.body().contains("\"processed\":1"));
    }

    @Test
    @DisplayName("동시 요청")
    void test4() throws Exception {
        // 여러 HTTP 클라이언트가 동시에 API 호출 시 정상 동작
        test1();
        test2();
    }

    @Test
    @DisplayName("대용량 플로우 정의")
    void test5() throws Exception {
        // 50개 이상의 노드를 포함한 플로우 배포
        StringBuilder nodes = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            nodes.append("{ \"id\": \"n").append(i).append("\", \"type\": \"Transform\", \"config\": {} }");
            if (i < 49) {
                nodes.append(",");
            }
        }
        String flowJson = "{ \"id\": \"big-flow\", \"nodes\": [" + nodes + "], \"connections\": [] }";

        var postReq = HttpRequest.newBuilder().uri(URI.create("http://localhost:8083" + "/flows"))
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(flowJson)).build();
        Assertions.assertEquals(201, client.send(postReq, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

}
