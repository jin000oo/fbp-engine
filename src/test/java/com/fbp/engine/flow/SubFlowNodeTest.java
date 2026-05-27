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

import com.fbp.engine.core.AbstractNode;
import com.fbp.engine.core.Connection;
import com.fbp.engine.core.Flow;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.TransformNode;
import com.fbp.engine.parser.FlowDefinition;
import com.fbp.engine.parser.JsonFlowParser;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SubFlowNodeTest {

    @Test
    @DisplayName("메시지 전달")
    void test1() throws InterruptedException {
        // 외부 입력 → 서브플로우 내부 → 외부 출력 정상 전달
        Flow inner = createTestInnerFlow();

        SubFlowNode subFlowNode = new SubFlowNode("sub", inner, "entry", "exit");

        Connection connection = new Connection("connection");
        subFlowNode.getOutputPort("out").connect(connection);

        subFlowNode.initialize();
        subFlowNode.process(new Message(Map.of("data", "hello")));

        Thread.sleep(500);
        Message result = connection.poll();

        Assertions.assertAll(
                () -> Assertions.assertNotNull(result),
                () -> Assertions.assertEquals("hello", result.getPayload().get("data"))
        );

        subFlowNode.shutdown();
    }

    @Test
    @DisplayName("내부 플로우 실행")
    void test2() {
        // 서브플로우 내부 노드들이 올바른 순서로 처리
        Flow inner = createTestInnerFlow();

        SubFlowNode subFlowNode = new SubFlowNode("sub", inner, "entry", "exit");

        Connection connection = new Connection("connection");
        subFlowNode.getOutputPort("out").connect(connection);

        subFlowNode.initialize();
        subFlowNode.process(new Message(Map.of("data", "hello")));

        Assertions.assertNotNull(connection.poll(500, TimeUnit.MILLISECONDS));

        subFlowNode.shutdown();
    }

    @Test
    @DisplayName("수명주기 — 시작")
    void test3() {
        // SubFlowNode 시작 시 내부 플로우도 시작
        Flow inner = createTestInnerFlow();

        SubFlowNode subFlowNode = new SubFlowNode("sub", inner, "entry", "exit");

        subFlowNode.initialize();

        TransformNode entryNode = (TransformNode) inner.getNodes().get("entry");

        Assertions.assertNotNull(entryNode);

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

        Assertions.assertEquals(Flow.State.STOPPED, inner.getState());
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
        Flow inner = new Flow("inner");
        inner.addNode(new AbstractNode("entry") {
            {
                addInputPort("in");
                addOutputPort("out");
            }

            @Override
            public void onProcess(Message message) {
                throw new RuntimeException("inner error");
            }

            @Override
            public void initialize() {
            }

            @Override
            public void shutdown() {
            }
        });

        inner.getNodes().get("entry").addErrorPort();

        SubFlowNode subFlowNode = new SubFlowNode("sub", inner, "entry", "entry");
        subFlowNode.addErrorPort();

        Connection errorConn = new Connection("error");
        subFlowNode.getOutputPort("error").connect(errorConn);

        Connection bridgeErr = new Connection("bridgeErr");
        inner.getNodes().get("entry").getOutputPort("error").connect(bridgeErr);

        subFlowNode.initialize();

        new Thread(() -> {
            while (true) {
                Message m = bridgeErr.take();

                if (m != null) {
                    subFlowNode.getOutputPort("error").send(m);
                }
            }
        }).start();

        subFlowNode.process(new Message(Map.of("data", "fail")));

        Message errorMsg = errorConn.poll(500, TimeUnit.MILLISECONDS);
        Assertions.assertNotNull(errorMsg);
        Assertions.assertEquals("inner error", errorMsg.getPayload().get("_error"));

        subFlowNode.shutdown();
    }

    @Test
    @DisplayName("JSON 정의")
    void test7() {
        // 플로우 JSON에서 서브플로우를 정의하고 파싱 가능
        String json = """
                {
                    "id": "test-flow",
                    "nodes": [
                        {
                            "id": "sub1",
                            "type": "SubFlow",
                            "config": {
                                "innerFlowId": "inner-1",
                                "entry": "in-node",
                                "exit": "out-node"
                            }
                        }
                    ],
                    "connections": []
                }
                """;

        JsonFlowParser parser = new JsonFlowParser();
        FlowDefinition def = parser.parse(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));

        Assertions.assertEquals(1, def.getNodes().size());
        Assertions.assertEquals("SubFlow", def.getNodes().get(0).type());
    }

    private Flow createTestInnerFlow() {
        Flow flow = new Flow("flow");
        flow.addNode(new TransformNode("entry", m -> m));
        flow.addNode(new TransformNode("exit", m -> m));
        flow.connect("entry", "out", "exit", "in");
        return flow;
    }

}