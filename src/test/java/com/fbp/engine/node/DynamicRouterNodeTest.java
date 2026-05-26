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

package com.fbp.engine.node;

import com.fbp.engine.core.Connection;
import com.fbp.engine.message.Message;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DynamicRouterNodeTest {

    DynamicRouterNode routerNode;

    Connection defaultConn;

    Connection highConn;

    Connection lowConn;

    @BeforeEach
    void setUp() {
        routerNode = new DynamicRouterNode("router");
        defaultConn = new Connection("default");
        highConn = new Connection("high");
        lowConn = new Connection("low");

        routerNode.getOutputPort("default").connect(defaultConn);
    }

    @Test
    @DisplayName("조건 매칭")
    void test1() {
        // 메시지 필드 값에 따라 올바른 출력 포트로 전달
        routerNode.addRule("temp > 30", "high");
        routerNode.getOutputPort("high").connect(highConn);

        Message msg = new Message(Map.of("temp", 35));
        routerNode.onProcess(msg);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(highConn.poll()),
                () -> Assertions.assertNull(defaultConn.poll())
        );
    }

    @Test
    @DisplayName("다중 규칙")
    void test2() {
        // 여러 RoutingRule 중 첫 매칭 규칙의 포트로 전달
        routerNode.addRule("temp > 30", "high");
        routerNode.addRule("temp < 10", "low");
        routerNode.getOutputPort("high").connect(highConn);
        routerNode.getOutputPort("low").connect(lowConn);

        Message msgLow = new Message(Map.of("temp", 5));
        routerNode.onProcess(msgLow);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(lowConn.poll()),
                () -> Assertions.assertNull(highConn.poll()),
                () -> Assertions.assertNull(defaultConn.poll())
        );
    }

    @Test
    @DisplayName("기본 포트")
    void test3() {
        // 어떤 규칙도 매칭되지 않으면 default 포트로 전달
        routerNode.addRule("temp > 30", "high");
        routerNode.getOutputPort("high").connect(highConn);

        Message msg = new Message(Map.of("temp", 20));
        routerNode.onProcess(msg);

        Assertions.assertAll(
                () -> Assertions.assertNull(highConn.poll()),
                () -> Assertions.assertNotNull(defaultConn.poll())
        );
    }

    @Test
    @DisplayName("규칙 없음")
    void test4() {
        // 규칙이 비어 있으면 모든 메시지가 default로 전달
        Message msg = new Message(Map.of("temp", 20));
        routerNode.onProcess(msg);

        Assertions.assertNotNull(defaultConn.poll());
    }

    @Test
    @DisplayName("null 필드")
    void test5() {
        // 라우팅 필드가 메시지에 없으면 default 포트
        routerNode.addRule("temp > 30", "high");
        routerNode.getOutputPort("high").connect(highConn);

        Message msg = new Message(Map.of("humidity", 50));
        routerNode.onProcess(msg);

        Assertions.assertAll(
                () -> Assertions.assertNull(highConn.poll()),
                () -> Assertions.assertNotNull(defaultConn.poll())
        );
    }

    @Test
    @DisplayName("런타임 규칙 변경")
    void test6() {
        // 실행 중 규칙 추가/제거 가능
        Message msg = new Message(Map.of("temp", 35));
        routerNode.onProcess(msg);
        Assertions.assertNotNull(defaultConn.poll());

        routerNode.addRule("temp > 30", "high");
        routerNode.getOutputPort("high").connect(highConn);

        routerNode.onProcess(msg);
        Assertions.assertNotNull(highConn.poll());
    }

    @Test
    @DisplayName("성능")
    void test7() {
        // 100개 규칙에서도 지연 시간이 허용 범위 내
        for (int i = 0; i < 100; i++) {
            routerNode.addRule("val == " + i, "port" + i);
        }

        long start = System.currentTimeMillis();
        routerNode.onProcess(new Message(Map.of("val", 99)));
        long end = System.currentTimeMillis();

        Assertions.assertTrue((end - start) < 100);
    }

}