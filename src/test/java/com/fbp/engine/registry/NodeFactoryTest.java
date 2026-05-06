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
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NodeFactoryTest {

    @Test
    @DisplayName("정상 생성")
    void test1() {
        // config를 받아 올바른 노드 인스턴스 반환
        NodeFactory nodeFactory = EchoProtocolNode::new;

        Map<String, Object> config = Map.of("threshold", 10.0);

        Node node = nodeFactory.create("filter", config);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(node),
                () -> Assertions.assertEquals("filter", node.getId()),
                () -> Assertions.assertEquals(10.0, ((EchoProtocolNode) node).getConfig("threshold"))
        );
    }

    @Test
    @DisplayName("잘못된 config")
    void test2() {
        // 필수 설정이 누락된 config 전달 시 예외
        NodeFactory nodeFactory = (id, config) -> {
            if (!config.containsKey("test")) {
                throw new IllegalArgumentException("키 누락");
            }

            return new EchoProtocolNode(id, config);
        };

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            nodeFactory.create("node-a", new HashMap<>());
        });
    }

    @Test
    @DisplayName("람다 구현")
    void test3() {
        // 함수형 인터페이스로 람다 기반 팩토리 등록 가능
        NodeFactory nodeFactory = (id, config) -> {
            return new EchoProtocolNode(id, config);
        };

        Map<String, Object> config = Map.of("threshold", 10.0);

        Node node = nodeFactory.create("filter", config);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(node),
                () -> Assertions.assertEquals("filter", node.getId()),
                () -> Assertions.assertEquals(10.0, ((EchoProtocolNode) node).getConfig("threshold"))
        );
    }

}