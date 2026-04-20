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

import com.fbp.engine.message.Message;
import com.fbp.engine.node.FilterNode;
import com.fbp.engine.node.PrintNode;
import com.fbp.engine.node.TimerNode;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class FlowTest {

    Flow flow;

    @BeforeEach
    void setUp() {
        flow = new Flow("test-flow");
    }

    @Test
    @DisplayName("노드 등록")
    void test1() {
        // addNode() 후 getNodes()에 해당 노드가 포함됨
        flow.addNode(new PrintNode("printer-1")).addNode(new PrintNode("printer-2"));

        Assertions.assertEquals(2, flow.getNodes().size());
    }

    @Test
    @DisplayName("메서드 체이닝")
    void test2() {
        // addNode().addNode().connect()가 예외 없이 동작
        flow.addNode(new PrintNode("printer-1")).addNode(new PrintNode("printer-2"));

        Assertions.assertTrue(flow.getNodes().containsKey("printer-1"));
    }

    @Test
    @DisplayName("정상 연결")
    void test3() {
        // connect() 후 getConnections()의 크기가 증가
        flow.addNode(new TimerNode("timer-1", 100)).addNode(new PrintNode("printer-1"))
                .connect("timer-1", "out", "printer-1", "in");

        Assertions.assertEquals(1, flow.getConnections().size());
        Assertions.assertEquals("timer-1:out->printer-1:in", flow.getConnections().getFirst().getId());
    }

    @Test
    @DisplayName("존재하지 않는 소스 노드 ID")
    void test4() {
        // connect()에 잘못된 sourceNodeId → IllegalArgumentException
        flow.addNode(new TimerNode("timer-1", 100)).addNode(new PrintNode("printer-1"));

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                flow.connect("wrong", "out", "printer-1", "in"));
    }

    @Test
    @DisplayName("존재하지 않는 대상 노드 ID")
    void test5() {
        // connect()에 잘못된 targetNodeId → IllegalArgumentException
        flow.addNode(new TimerNode("timer-1", 100)).addNode(new PrintNode("printer-1"));

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                flow.connect("timer-1", "out", "wrong", "in"));
    }

    @Test
    @DisplayName("존재하지 않는 소스 포트")
    void test6() {
        // connect()에 잘못된 sourcePort → IllegalArgumentException
        flow.addNode(new TimerNode("timer-1", 100)).addNode(new PrintNode("printer-1"));

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                flow.connect("timer-1", "wrong", "printer-1", "in"));
    }

    @Test
    @DisplayName("존재하지 않는 대상 포트")
    void test7() {
        // connect()에 잘못된 targetPort → IllegalArgumentException
        flow.addNode(new TimerNode("timer-1", 100)).addNode(new PrintNode("printer-1"));

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                flow.connect("timer-1", "out", "printer-1", "wrong"));
    }

    @Test
    @DisplayName("validate — 빈 Flow")
    void test8() {
        // 노드가 없는 Flow의 validate()가 에러 메시지 포함
        Assertions.assertFalse(flow.validate().isEmpty());
    }

    @Test
    @DisplayName("validate — 정상 Flow")
    void test9() {
        // 유효한 Flow의 validate()가 빈 리스트 반환
        flow.addNode(new TimerNode("timer-1", 100)).addNode(new PrintNode("printer-1"));

        Assertions.assertTrue(flow.validate().isEmpty());
    }

    @Test
    @DisplayName("initialize — 전체 호출")
    void test10() {
        // initialize() 시 모든 노드의 initialize()가 호출됨 (TimerNode 등의 동작으로 간접 확인)
        class DummyNode extends AbstractNode {

            boolean initCalled = false;

            public DummyNode(String id) {
                super(id);
            }

            @Override
            public void onProcess(Message message) {
            }

            @Override
            public void initialize() {
                initCalled = true;
            }

            @Override
            public void shutdown() {
            }

        }

        DummyNode dummy1 = new DummyNode("dummy-1");
        DummyNode dummy2 = new DummyNode("dummy-2");
        flow.addNode(dummy1).addNode(dummy2);

        flow.initialize();
        Assertions.assertTrue(dummy1.initCalled && dummy2.initCalled);
    }

    @Test
    @DisplayName("shutdown — 전체 호출")
    void test11() {
        // shutdown() 시 모든 노드의 shutdown()이 호출됨
        class DummyNode extends AbstractNode {

            boolean shutCalled = false;

            public DummyNode(String id) {
                super(id);
            }

            @Override
            public void onProcess(Message message) {
            }

            @Override
            public void initialize() {
            }

            @Override
            public void shutdown() {
                shutCalled = true;
            }

        }

        DummyNode dummy1 = new DummyNode("dummy-1");
        DummyNode dummy2 = new DummyNode("dummy-2");
        flow.addNode(dummy1).addNode(dummy2);

        flow.shutdown();
        Assertions.assertTrue(dummy1.shutCalled && dummy2.shutCalled);
    }

    @Test
    @DisplayName("순환 참조 탐지 (도전)")
    void test12() {
        // 순환 연결이 있는 Flow의 validate()가 에러 메시지 포함
        flow.addNode(new FilterNode("A", "key", 1))
                .addNode(new FilterNode("B", "key", 1))
                .addNode(new FilterNode("C", "key", 1));

        flow.connect("A", "out", "B", "in")
                .connect("B", "out", "C", "in")
                .connect("C", "out", "A", "in");

        List<String> errors = flow.validate();

        Assertions.assertAll(
                () -> Assertions.assertFalse(errors.isEmpty()),
                () -> Assertions.assertTrue(errors.getFirst().contains("순환 참조"))
        );
    }

}
