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

package com.fbp.engine.engine;

import com.fbp.engine.core.AbstractNode;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.message.Message;
import com.fbp.engine.parser.FlowDefinition;
import com.fbp.engine.parser.NodeDefinition;
import com.fbp.engine.registry.NodeRegistry;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class FlowManagerTest {

    FlowEngine engine;

    NodeRegistry registry;

    FlowManager manager;

    static class DummyNode extends AbstractNode {

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
        }

    }

    @BeforeEach
    void setUp() {
        engine = new FlowEngine();
        registry = Mockito.mock(NodeRegistry.class);
        manager = new FlowManager(engine, registry);
    }

    @AfterEach
    void tearDown() {
        engine.shutdown();
    }

    @Test
    @DisplayName("deploy")
    void test1() {
        // FlowDefinition으로 플로우 배포 → 실행 상태 확인
        mockRegistrySuccess();

        manager.deploy(createSampleFlowDef("flow-1"));

        Assertions.assertEquals("RUNNING", manager.getStatus("flow-1"));
    }

    @Test
    @DisplayName("list")
    void test2() {
        // 배포된 플로우 목록 조회
        mockRegistrySuccess();

        manager.deploy(createSampleFlowDef("flow-1"));
        manager.deploy(createSampleFlowDef("flow-2"));

        Assertions.assertEquals(2, manager.list().size());
    }

    @Test
    @DisplayName("getStatus")
    void test3() {
        // 특정 플로우의 상태(RUNNING, STOPPED) 조회
        mockRegistrySuccess();

        manager.deploy(createSampleFlowDef("flow-1"));

        Assertions.assertEquals("RUNNING", manager.getStatus("flow-1"));

        manager.stop("flow-1");

        Assertions.assertEquals("STOPPED", manager.getStatus("flow-1"));
    }

    @Test
    @DisplayName("stop")
    void test4() {
        // 실행 중인 플로우 정지
        mockRegistrySuccess();

        manager.deploy(createSampleFlowDef("flow-1"));

        manager.stop("flow-1");

        Assertions.assertEquals("STOPPED", manager.getStatus("flow-1"));
    }

    @Test
    @DisplayName("restart")
    void test5() {
        // 정지된 플로우 재시작
        mockRegistrySuccess();

        manager.deploy(createSampleFlowDef("flow-1"));

        manager.stop("flow-1");
        manager.restart("flow-1");

        Assertions.assertEquals("RUNNING", manager.getStatus("flow-1"));
    }

    @Test
    @DisplayName("remove")
    void test6() {
        // 플로우 삭제 — 정지 후 제거
        mockRegistrySuccess();

        manager.deploy(createSampleFlowDef("flow-1"));

        manager.stop("flow-1");
        manager.remove("flow-1");

        Assertions.assertAll(
                () -> Assertions.assertEquals(0, manager.list().size()),
                () -> Assertions.assertThrows(IllegalArgumentException.class, () -> {
                    manager.getStatus("flow-1");
                })
        );
    }

    @Test
    @DisplayName("실행 중 삭제")
    void test7() {
        // RUNNING 상태의 플로우 삭제 시 자동 정지 후 삭제
        mockRegistrySuccess();

        manager.deploy(createSampleFlowDef("flow-1"));

        manager.remove("flow-1");

        Assertions.assertEquals(0, manager.list().size());
    }

    @Test
    @DisplayName("존재하지 않는 id 조작")
    void test8() {
        // 없는 id로 stop/restart/remove 시 예외
        Assertions.assertAll(
                () -> Assertions.assertThrows(IllegalArgumentException.class, () -> {
                    manager.stop("unknown");
                }),
                () -> Assertions.assertThrows(IllegalArgumentException.class, () -> {
                    manager.restart("unknown");
                }),
                () -> Assertions.assertThrows(IllegalArgumentException.class, () -> {
                    manager.remove("unknown");
                }),
                () -> Assertions.assertThrows(IllegalArgumentException.class, () -> {
                    manager.getStatus("unknown");
                })
        );
    }

    @Test
    @DisplayName("중복 id 배포")
    void test9() {
        // 이미 존재하는 id의 플로우 배포 시 정책에 맞게 동작
        mockRegistrySuccess();

        FlowDefinition flowDefinition = createSampleFlowDef("flow-1");

        manager.deploy(flowDefinition);

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            manager.deploy(flowDefinition);
        });
    }

    @Test
    @DisplayName("미등록 노드 타입")
    void test10() {
        // FlowDefinition에 NodeRegistry에 없는 타입이 있으면 배포 실패
        Mockito.when(registry.isRegistered("DummyType")).thenReturn(false);

        FlowDefinition def = createSampleFlowDef("flow-1");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            manager.deploy(def);
        });
    }

    FlowDefinition createSampleFlowDef(String flowId) {
        NodeDefinition nodeDefinition = new NodeDefinition("node-1", "DummyType", Map.of());
        return new FlowDefinition(flowId, "Test Flow", "Description",
                List.of(nodeDefinition), List.of());
    }

    void mockRegistrySuccess() {
        Mockito.when(registry.isRegistered("DummyType")).thenReturn(true);
        Mockito.when(registry.create(Mockito.eq("DummyType"), Mockito.anyString(), Mockito.anyMap()))
                .thenAnswer(invocation -> new DummyNode(invocation.getArgument(1)));
    }

}