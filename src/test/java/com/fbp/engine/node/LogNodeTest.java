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

class LogNodeTest {

    @Test
    @DisplayName("메시지 통과 전달")
    void test1() {
        // 수신한 메시지가 그대로 OutputPort로 전달됨 (내용 동일)
        LogNode logger = new LogNode("logger-1");
        Connection connection = new Connection("connection-1");
        logger.getOutputPort("out").connect(connection);

        Message message = new Message(Map.of("test", 1));
        logger.process(message);

        Assertions.assertSame(message, connection.poll());
    }

    @Test
    @DisplayName("중간 삽입 가능")
    void test2() {
        // A → LogNode → B 연결에서 A가 보낸 메시지를 B가 수신
        GeneratorNode generator = new GeneratorNode("generator-1");
        LogNode logger = new LogNode("logger-1");

        Connection connection1 = new Connection("connection-1");
        Connection connection2 = new Connection("connection-2");
        connection1.setTarget(logger.getInputPort("in"));

        generator.getOutputPort("out").connect(connection1);
        logger.getOutputPort("out").connect(connection2);

        generator.generate("key", "value");
        Message message = connection1.poll();

        logger.getInputPort("in").receive(message);

        Message finalMessage = connection2.poll();
        Assertions.assertAll(
                () -> Assertions.assertNotNull(finalMessage),
                () -> Assertions.assertEquals("value", finalMessage.get("key"))

        );
    }

}