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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GeneratorNodeTest {

    @Test
    @DisplayName("generate 메시지 생성")
    void test1() {
        // generate("key", "value") 호출 시 OutputPort로 메시지가 전달됨
        GeneratorNode generator = new GeneratorNode("generator-1");

        Connection connection = new Connection("connection-1");
        generator.getOutputPort().connect(connection);

        generator.generate("key", "value");

        Assertions.assertEquals(1, connection.getBufferSize());
    }

    @Test
    @DisplayName("메시지 내용 확인")
    void test2() {
        // 전달된 메시지의 페이로드에 지정한 key-value가 포함됨
        GeneratorNode generator = new GeneratorNode("generator-1");

        Connection connection = new Connection("connection-1");
        generator.getOutputPort().connect(connection);

        generator.generate("key", "value");

        Assertions.assertEquals("value", connection.getBuffer().peek().get("key"));
    }

    @Test
    @DisplayName("OutputPort 조회")
    void test3() {
        // getOutputPort()가 null이 아님
        GeneratorNode generator = new GeneratorNode("generator-1");

        Assertions.assertNotNull(generator.getOutputPort());
    }

    @Test
    @DisplayName("다수 generate 호출")
    void test4() {
        // 3번 호출하면 3개의 메시지가 순서대로 전달됨
        GeneratorNode generator = new GeneratorNode("generator-1");

        Connection connection = new Connection("connection-1");
        generator.getOutputPort().connect(connection);

        generator.generate("key1", "value1");
        generator.generate("key2", "value2");
        generator.generate("key3", "value3");

        Assertions.assertEquals(3, connection.getBufferSize());
    }

}