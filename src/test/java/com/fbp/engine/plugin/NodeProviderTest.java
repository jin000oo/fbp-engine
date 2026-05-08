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

package com.fbp.engine.plugin;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NodeProviderTest {

    @Test
    @DisplayName("getNodeDescriptors")
    void test1() {
        // 구현체가 올바른 NodeDescriptor 목록 반환
        NodeProvider provider = () -> List.of(new NodeDescriptor(
                "node-a", "test-node", null, (id, config) -> null));

        List<NodeDescriptor> descriptors = provider.getNodeDescriptors();

        Assertions.assertAll(
                () -> Assertions.assertEquals(1, descriptors.size()),
                () -> Assertions.assertEquals("node-a", descriptors.getFirst().typeName()),
                () -> Assertions.assertEquals("test-node", descriptors.getFirst().description())
        );
    }

    @Test
    @DisplayName("빈 목록")
    void test2() {
        // 노드를 제공하지 않는 Provider → 빈 리스트 반환
        NodeProvider nodeProvider = List::of;

        Assertions.assertTrue(nodeProvider.getNodeDescriptors().isEmpty());
    }

    @Test
    @DisplayName("descriptor 정합성")
    void test3() {
        // 반환된 descriptor의 typeName, factory가 null이 아님
        NodeProvider provider = () -> List.of(new NodeDescriptor(
                "node-b", "test-node", null, (id, config) -> null));

        NodeDescriptor descriptor = provider.getNodeDescriptors().getFirst();

        Assertions.assertAll(
                () -> Assertions.assertNotNull(descriptor.typeName()),
                () -> Assertions.assertNotNull(descriptor.factory())
        );
    }

}