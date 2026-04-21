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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CollectorNodeTest {

    @Test
    @DisplayName("메시지 수집")
    void test1() {
        // 메시지를 전송하면 getCollected() 리스트에 저장됨
        CollectorNode collector = new CollectorNode("collector");
        collector.process(new Message(Map.of("data", 1)));

        Assertions.assertEquals(1, collector.getCollected().size());
    }

    @Test
    @DisplayName("수집 순서 보존")
    void test2() {
        // 여러 메시지를 순서대로 전송하면 리스트에 전송 순서대로 저장됨
        CollectorNode collector = new CollectorNode("collector");
        collector.process(new Message(Map.of("data", 1)));
        collector.process(new Message(Map.of("data", 2)));

        Assertions.assertAll(
                () -> Assertions.assertEquals(1, (Integer) collector.getCollected().getFirst().get("data")),
                () -> Assertions.assertEquals(2, (Integer) collector.getCollected().get(1).get("data"))
        );
    }

    @Test
    @DisplayName("초기 상태 빈 리스트")
    void test3() {
        // 생성 직후 getCollected()가 빈 리스트를 반환
        CollectorNode collector = new CollectorNode("collector");

        Assertions.assertTrue(collector.getCollected().isEmpty());
    }

    @Test
    @DisplayName("InputPort 존재")
    void test4() {
        // "in" 포트가 정상적으로 등록되어 있음
        CollectorNode collector = new CollectorNode("collector");

        Assertions.assertNotNull(collector.getInputPort("in"));
    }

    @Test
    @DisplayName("파이프라인 연결 검증")
    void test5() {
        // GeneratorNode → CollectorNode 연결 시, Generator가 보낸 모든 메시지가 Collector에 수집됨
        GeneratorNode generator = new GeneratorNode("generator");
        CollectorNode collector = new CollectorNode("collector");

        Connection connection = new Connection("connection");

        generator.getOutputPort("out").connect(connection);
        connection.setTarget(collector.getInputPort("in"));

        generator.generate("key", "value");
        collector.getInputPort("in").receive(connection.poll());

        Assertions.assertAll(
                () -> Assertions.assertEquals(1, collector.getCollected().size()),
                () -> Assertions.assertEquals("value", collector.getCollected().getFirst().get("key"))
        );
    }

}