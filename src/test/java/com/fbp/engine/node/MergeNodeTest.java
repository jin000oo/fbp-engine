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

class MergeNodeTest {

    MergeNode mergeNode;

    Connection connection;

    @BeforeEach
    void setUp() {
        mergeNode = new MergeNode("merge");
        connection = new Connection("connection");

        mergeNode.getOutputPort("out").connect(connection);
    }

    @Test
    @DisplayName("양쪽 입력 수신")
    void test1() {
        // in-1과 in-2 양쪽에서 메시지를 수신할 수 있음
        mergeNode.process(new Message(Map.of("__source__", "in-1", "temperature", 25.5)));
        mergeNode.process(new Message(Map.of("__source__", "in-2", "humidity", 60.0)));

        Message result = connection.poll();

        Assertions.assertNotNull(result);
    }

    @Test
    @DisplayName("합쳐진 메시지 출력")
    void test2() {
        // 두 입력의 데이터가 하나의 메시지에 합쳐져 OutputPort로 전달됨
        mergeNode.process(new Message(Map.of("__source__", "in-1", "temperature", 25.5)));
        mergeNode.process(new Message(Map.of("__source__", "in-2", "humidity", 60.0)));

        Message result = connection.poll();

        Assertions.assertAll(
                () -> Assertions.assertTrue(result.hasKey("temperature")),
                () -> Assertions.assertTrue(result.hasKey("humidity"))
        );
    }

//    @Test
//    @DisplayName("한쪽만 도착 시 대기")
//    void test3() {
//        // 한쪽 입력만 도착하면 출력이 즉시 발생하지 않음 (매칭 대기)
//        mergeNode.process(new Message(Map.of("__source__", "in-1", "temperature", 25.5)));
//
//        Assertions.assertNull(connection.poll());
//    }

    @Test
    @DisplayName("포트 구성 확인")
    void test4() {
        // getInputPort("in-1"), getInputPort("in-2"), getOutputPort("out")이 모두 null이 아님
        Assertions.assertAll(
                () -> Assertions.assertNotNull(mergeNode.getInputPort("in-1")),
                () -> Assertions.assertNotNull(mergeNode.getInputPort("in-2")),
                () -> Assertions.assertNotNull(mergeNode.getOutputPort("out"))
        );
    }

}