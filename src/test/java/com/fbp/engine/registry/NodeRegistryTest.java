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

package com.fbp.engine.registry;

import com.fbp.engine.core.Node;
import com.fbp.engine.node.EchoProtocolNode;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NodeRegistryTest {

    NodeRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new NodeRegistry();
    }

    @Test
    @DisplayName("register + create")
    void test1() {
        // 팩토리 등록 후 create()로 노드 인스턴스 생성
        registry.register("test", EchoProtocolNode::new);

        Node node = registry.create("test", "node-1", Map.of("key", "value"));

        Assertions.assertAll(
                () -> Assertions.assertNotNull(node),
                () -> Assertions.assertEquals("node-1", node.getId())
        );
    }

    @Test
    @DisplayName("미등록 타입 create")
    void test2() {
        // 등록되지 않은 타입으로 create() 호출 시 NodeRegistryException
        Assertions.assertThrows(NodeRegistryException.class, () -> {
            registry.create("unkown", "node-1004", null);
        });
    }

    @Test
    @DisplayName("중복 등록 처리")
    void test3() {
        // 동일 타입명으로 두 번 등록 시 정책에 맞게 동작 (덮어쓰기 또는 예외)
        registry.register("type", EchoProtocolNode::new);

        Assertions.assertDoesNotThrow(() -> {
            registry.register("type", EchoProtocolNode::new);
        });
    }

    @Test
    @DisplayName("getRegisteredTypes")
    void test4() {
        // 등록된 타입 목록이 정확히 반환됨
        registry.register("type-a", EchoProtocolNode::new);
        registry.register("type-b", EchoProtocolNode::new);

        Set<String> types = registry.getRegisteredTypes();

        Assertions.assertAll(
                () -> Assertions.assertTrue(types.contains("type-a")),
                () -> Assertions.assertTrue(types.contains("type-b")),
                () -> Assertions.assertEquals(2, types.size())
        );
    }

    @Test
    @DisplayName("config 전달")
    void test5() {
        // create 시 전달한 config가 노드에 올바르게 적용됨
        registry.register("test", EchoProtocolNode::new);

        Node node = registry.create("test", "node-1", Map.of("key", "value"));

        Assertions.assertEquals("value", ((EchoProtocolNode) node).getConfig("key"));
    }

    @Test
    @DisplayName("isRegistered")
    void test6() {
        // 등록된 타입 → true, 미등록 → false
        registry.register("test", EchoProtocolNode::new);

        Assertions.assertAll(
                () -> Assertions.assertTrue(registry.isRegistered("test")),
                () -> Assertions.assertFalse(registry.isRegistered("wrong"))
        );
    }

    @Test
    @DisplayName("null/빈 타입명")
    void test7() {
        // null 또는 빈 문자열로 등록/조회 시 적절한 예외
        Assertions.assertAll(
                () -> Assertions.assertThrows(NodeRegistryException.class, () -> {
                    registry.register(null, null);
                }),
                () -> Assertions.assertThrows(NodeRegistryException.class, () -> {
                    registry.register("", null);
                }),
                () -> Assertions.assertThrows(NodeRegistryException.class, () -> {
                    registry.create(null, null, null);
                })
        );
    }

}