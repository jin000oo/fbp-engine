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

package com.fbp.engine.core;

import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProtocolNodeTest {

    static class DummyProtocolNode extends ProtocolNode {

        boolean failConnect = false;

        int connectTryCount = 0;

        public DummyProtocolNode(String id, Map<String, Object> config) {
            super(id, config);
        }

        @Override
        protected void connect() throws IOException {
            connectTryCount++;

            if (failConnect) {
                throw new RuntimeException();
            }
        }

        @Override
        protected void disconnect() throws IOException {
        }
    }

    DummyProtocolNode node;

    @BeforeEach
    void setUp() {
        Map<String, Object> config = Map.of(
                "host", "localhost",
                "reconnectIntervalMs", 100L,
                "maxRetries", 3
        );

        node = new DummyProtocolNode("dummy", config);
    }

    @AfterEach
    void tearDown() {
        node.shutdown();
    }

    @Test
    @DisplayName("초기 상태")
    void test1() {
        // 생성 직후 getConnectionState()가 DISCONNECTED
        Assertions.assertEquals(ConnectionState.DISCONNECTED, node.getConnectionState());
    }

    @Test
    @DisplayName("config 조회")
    void test2() {
        // 생성 시 전달한 config의 값을 getConfig()로 조회 가능
        Assertions.assertAll(
                () -> Assertions.assertEquals("localhost", node.getConfig("host")),
                () -> Assertions.assertEquals(100L, node.getConfig("reconnectIntervalMs")),
                () -> Assertions.assertEquals(3, node.getConfig("maxRetries"))
        );
    }

    @Test
    @DisplayName("initialize → CONNECTED")
    void test3() {
        // connect()가 성공하면 상태가 CONNECTED로 변경됨
        node.initialize();

        Assertions.assertEquals(ConnectionState.CONNECTED, node.getConnectionState());
    }

    @Test
    @DisplayName("initialize → 연결 실패 시 상태")
    void test4() {
        // connect()에서 예외 발생 시 ERROR 또는 재연결 스케줄러가 시작됨
        node.failConnect = true;
        node.initialize();

        Assertions.assertAll(
                () -> Assertions.assertEquals(ConnectionState.ERROR, node.getConnectionState()),
                () -> Assertions.assertFalse(node.isConnected())
        );
    }

    @Test
    @DisplayName("shutdown → DISCONNECTED")
    void test5() {
        // shutdown() 후 상태가 DISCONNECTED
        node.initialize();
        node.shutdown();

        Assertions.assertEquals(ConnectionState.DISCONNECTED, node.getConnectionState());
    }

    @Test
    @DisplayName("isConnected 반환값")
    void test6() {
        // CONNECTED 상태에서 isConnected() → true, 그 외 → false
        node.initialize();

        Assertions.assertTrue(node.isConnected());
    }

    @Test
    @DisplayName("재연결 시도")
    void test7() throws InterruptedException {
        // connect() 실패 후 재연결 스케줄러가 지정 간격으로 재시도함 (간접 확인)
        node.failConnect = true;
        node.initialize();

        Thread.sleep(400);

        Assertions.assertTrue(node.connectTryCount >= 3);
    }

}