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

class TransformNodeTest {

    @Test
    @DisplayName("변환 정상 동작")
    void test1() {
        // 입력 메시지가 transformer 함수에 의해 변환되어 OutputPort로 전달됨
        TransformNode transformer = new TransformNode("transformer-1", message -> {
            int value = message.get("value");
            return message.withEntry("result", value * 2);
        });

        Connection connection = new Connection("out");
        transformer.getOutputPort("out").connect(connection);

        Message original = new Message(Map.of("value", 10));
        transformer.process(original);

        Message result = connection.poll();
        Assertions.assertEquals(20, (Integer) result.get("result"));
    }

    @Test
    @DisplayName("null 반환 시 미전달")
    void test2() {
        // transformer가 null을 반환하면 OutputPort로 전달되지 않음
        TransformNode transformer = new TransformNode("transformer-1", message -> null);

        Connection connection = new Connection("out");
        transformer.getOutputPort("out").connect(connection);

        transformer.process(new Message(Map.of("key", "value")));
        Assertions.assertEquals(0, connection.getBufferSize());
    }

    @Test
    @DisplayName("원본 메시지 불변")
    void test3() {
        // 변환 후에도 원본 메시지의 내용은 변하지 않음
        TransformNode transformer = new TransformNode("transformer-1", message -> {
            int value = message.get("value");
            return message.withEntry("result", value * 2);
        });

        Connection connection = new Connection("out");
        transformer.getOutputPort("out").connect(connection);

        Message original = new Message(Map.of("value", 10));
        transformer.process(original);

        connection.poll();
        Assertions.assertFalse(original.hasKey("result"));
    }

}