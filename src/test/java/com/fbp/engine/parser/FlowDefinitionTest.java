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

package com.fbp.engine.parser;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FlowDefinitionTest {

    @Test
    @DisplayName("불변성")
    void test1() {
        // 생성 후 노드/연결 목록 수정 불가 (unmodifiable)
        FlowDefinition flowDefinition =
                new FlowDefinition("id", "name", "desc", List.of(), List.of());

        Assertions.assertAll(
                () -> Assertions.assertThrows(UnsupportedOperationException.class, () -> {
                    flowDefinition.getNodes()
                            .add(new NodeDefinition("n", "t", Map.of()));
                }),
                () -> Assertions.assertThrows(UnsupportedOperationException.class, () -> {
                    flowDefinition.getConnections()
                            .add(new ConnectionDefinition("a", "out", "b", "in"));
                })
        );
    }

    @Test
    @DisplayName("노드 조회")
    void test2() {
        // getNode(id)로 특정 노드 정의 조회
        NodeDefinition node = new NodeDefinition("node-1", "type", Map.of());

        FlowDefinition flowDefinition =
                new FlowDefinition("id", "name", "desc", List.of(node), List.of());

        Assertions.assertAll(
                () -> Assertions.assertEquals(node, flowDefinition.getNode("node-1")),
                () -> Assertions.assertNull(flowDefinition.getNode("unknown"))
        );
    }

    @Test
    @DisplayName("연결 유효성")
    void test3() {
        // 모든 연결이 존재하는 노드를 참조하는지 검증
        NodeDefinition node1 = new NodeDefinition("n1", "type", Map.of());
        NodeDefinition node2 = new NodeDefinition("n2", "type", Map.of());

        ConnectionDefinition connectionDefinition =
                new ConnectionDefinition("n1", "out", "n2", "in");
        FlowDefinition flowDefinition = new FlowDefinition("id", "name", "desc",
                List.of(node1, node2), List.of(connectionDefinition));

        Assertions.assertDoesNotThrow(flowDefinition::validateConnections);
    }

}