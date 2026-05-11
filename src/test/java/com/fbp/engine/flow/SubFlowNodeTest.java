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

package com.fbp.engine.flow;

import com.fbp.engine.core.Connection;
import com.fbp.engine.core.Flow;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.EchoProtocolNode;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SubFlowNodeTest {

    @Test
    @DisplayName("메시지 전달메시지 전달")
    void test1() throws InterruptedException {
        // 외부 입력 → 서브플로우 내부 → 외부 출력 정상 전달
        Flow inner = createTestInnerFlow();

        SubFlowNode subFlowNode = new SubFlowNode("sub", inner, "entry", "exit");

        Connection connection = new Connection("connection");
        subFlowNode.getOutputPort("out").connect(connection);

        subFlowNode.initialize();
        subFlowNode.process(new Message(Map.of("data", "hello")));

        Thread.sleep(100);

        Message result = connection.poll();

        Assertions.assertAll(
                () -> Assertions.assertNotNull(result),
                () -> Assertions.assertEquals("hello", result.getPayload().get("data"))
        );

        subFlowNode.shutdown();
    }

    @Test
    @DisplayName("내부 플로우 실행")
    void test2() throws InterruptedException {
        // 서브플로우 내부 노드들이 올바른 순서로 처리
        Flow inner = createTestInnerFlow();

        SubFlowNode subFlowNode = new SubFlowNode("sub", inner, "entry", "exit");

        Connection connection = new Connection("connection");
        subFlowNode.getOutputPort("out").connect(connection);

        subFlowNode.initialize();
        subFlowNode.process(new Message(Map.of("data", "hello")));

        Thread.sleep(100);

        Assertions.assertNotNull(connection.poll());

        subFlowNode.shutdown();
    }

    @Test
    @DisplayName("수명주기 — 시작")
    void test3() {
        // SubFlowNode 시작 시 내부 플로우도 시작
        Flow inner = createTestInnerFlow();

        SubFlowNode subFlowNode = new SubFlowNode("sub", inner, "entry", "exit");

        subFlowNode.initialize();

        EchoProtocolNode echoProtocolNode = (EchoProtocolNode) inner.getNodes().get("entry");

        Assertions.assertNotNull(echoProtocolNode);

        subFlowNode.shutdown();
    }

    @Test
    @DisplayName("수명주기 — 정지")
    void test4() {
        // SubFlowNode 정지 시 내부 플로우도 정지
        Flow inner = createTestInnerFlow();

        SubFlowNode subFlowNode = new SubFlowNode("sub", inner, "entry", "exit");

        subFlowNode.initialize();
        subFlowNode.shutdown();

        EchoProtocolNode echoProtocolNode = (EchoProtocolNode) inner.getNodes().get("entry");

        Assertions.assertNull(echoProtocolNode);
    }

    @Test
    @DisplayName("재사용")
    void test5() {
        // 같은 서브플로우 정의를 여러 곳에서 인스턴스화
        Flow inner1 = createTestInnerFlow();
        Flow inner2 = createTestInnerFlow();

        SubFlowNode subFlowNode1 = new SubFlowNode("sub1", inner1, "entry", "exit");
        SubFlowNode subFlowNode2 = new SubFlowNode("sub2", inner2, "entry", "exit");

        Assertions.assertNotEquals(subFlowNode1.getId(), subFlowNode2.getId());
    }

    @Test
    @DisplayName("내부 에러 전파")
    void test6() {
        // 서브플로우 내부에서 에러 발생 시 외부 에러 포트로 전파
    }

    @Test
    @DisplayName("JSON 정의")
    void test7() {
        // 플로우 JSON에서 서브플로우를 정의하고 파싱 가능
    }

    private Flow createTestInnerFlow() {
        Flow flow = new Flow("flow");
        flow.addNode(new EchoProtocolNode("entry", Map.of()));
        flow.addNode(new EchoProtocolNode("exit", Map.of()));
        flow.connect("entry", "out", "exit", "in");
        return flow;
    }

}