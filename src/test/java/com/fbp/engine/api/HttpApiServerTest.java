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
import java.io.IOException;
import java.net.BindException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class HttpApiServerTest {

    HttpApiServer server;

    FlowManager flowManager;

    HttpClient client;

    @BeforeEach
    void setUp() throws IOException {
        flowManager = Mockito.mock(FlowManager.class);
        server = new HttpApiServer(18080, flowManager);
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    @DisplayName("서버 시작/정지")
    void test1() {
        // start() → 포트 바인딩 확인, stop() → 정상 종료
        Assertions.assertDoesNotThrow(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:18080/health"))
                    .GET().build();

            client.send(request, HttpResponse.BodyHandlers.ofString());
        });

        server.stop();

        Assertions.assertThrows(Exception.class, () -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:18080/health"))
                    .GET().build();

            client.send(request, HttpResponse.BodyHandlers.ofString());
        });
    }

    @Test
    @DisplayName("GET /health")
    void test2() throws IOException, InterruptedException {
        // 200 OK, status 필드 포함
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:18080/health"))
                .GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertAll(
                () -> Assertions.assertEquals(200, response.statusCode()),
                () -> Assertions.assertTrue(response.body().contains("\"status\":\"UP\""))
        );
    }

    @Test
    @DisplayName("GET /flows")
    void test3() throws IOException, InterruptedException {
        // 200 OK, 배포된 플로우 목록 반환
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:18080/flows"))
                .GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(200, response.statusCode());
    }

    @Test
    @DisplayName("POST /flows")
    void test4() throws IOException, InterruptedException {
        // 유효한 JSON → 201 Created, 플로우 배포 확인
        String validJson =
                "{\"id\":\"f1\", \"nodes\":[{\"id\":\"n1\",\"type\":\"t1\",\"config\":{}}], \"connections\":[]}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:18080/flows"))
                .POST(HttpRequest.BodyPublishers.ofString(validJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(201, response.statusCode());
    }

    @Test
    @DisplayName("POST /flows 잘못된 JSON")
    void test5() throws IOException, InterruptedException {
        // 400 Bad Request
        String invalidJson = "{ invalid json ";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:18080/flows"))
                .POST(HttpRequest.BodyPublishers.ofString(invalidJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    @DisplayName("DELETE /flows/{id}")
    void test6() throws IOException, InterruptedException {
        // 존재하는 플로우 삭제 → 200 OK
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:18080/flows/f1"))
                .DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(200, response.statusCode());
        Mockito.verify(flowManager).remove("f1");
    }

    @Test
    @DisplayName("DELETE /flows/{id} 없는 id")
    void test7() throws IOException, InterruptedException {
        // 404 Not Found
        Mockito.doThrow(new IllegalArgumentException("Not found")).when(flowManager).remove("missing");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:18080/flows/missing"))
                .DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    @DisplayName("GET /flows/{id}/metrics")
    void test8() throws IOException, InterruptedException {
        // 배포된 플로우의 메트릭 JSON 반환
        Flow mockFlow = new Flow("flow-1");
        Mockito.when(flowManager.list()).thenReturn(List.of(mockFlow));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:18080/flows/flow-1/metrics"))
                .GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertAll(
                () -> Assertions.assertEquals(200, response.statusCode()),
                () -> Assertions.assertTrue(response.body().contains("\"nodes\":"))
        );
    }

    @Test
    @DisplayName("존재하지 않는 경로")
    void test9() throws IOException, InterruptedException {
        // 404 Not Found
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:18080/unknown"))
                .GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    @DisplayName("잘못된 HTTP 메서드")
    void test10() throws IOException, InterruptedException {
        // 405 Method Not Allowed
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:18080/health"))
                .POST(HttpRequest.BodyPublishers.noBody()).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(405, response.statusCode());
    }

    @Test
    @DisplayName("포트 충돌")
    void test11() {
        // 이미 사용 중인 포트로 시작 시 예외
        Assertions.assertThrows(BindException.class, () -> {
            new HttpApiServer(18080, flowManager).start();
        });
    }

    @Test
    @DisplayName("Content-Type")
    void test12() throws IOException, InterruptedException {
        // 응답 헤더에 application/json 포함
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:18080/health"))
                .GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertTrue(response.headers().firstValue("Content-Type").get().contains("application/json"));
    }

}