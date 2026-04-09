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

package com.fbp.engine.core;

import com.fbp.engine.message.Message;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConnectionTest {

    @Mock
    InputPort inputPort;

    @Test
    @DisplayName("deliver 후 target 수신")
    void test1() {
        // deliver()한 메시지가 target InputPort의 receive()를 통해 노드에 전달됨
        Connection connection = new Connection("connection-1", 10);
        connection.setTarget(inputPort);

        Message message = new Message(Map.of("key", "value"));
        connection.deliver(message);

        Mockito.verify(inputPort, Mockito.times(1)).receive(message);
    }

    @Test
    @DisplayName("target 미설정 시 동작")
    void test2() {
        // target이 null인 상태에서 deliver()해도 예외가 발생하지 않음
        Connection connection = new Connection("connection-1", 10);

        Message message = new Message(Map.of("key", "value"));

        Assertions.assertDoesNotThrow(() -> connection.deliver(message));
    }

    @Test
    @DisplayName("버퍼 크기 확인")
    void test3() {
        // deliver() 후 getBufferSize()가 예상값과 일치
        Connection connection = new Connection("connection-1", 10);

        Message message = new Message(Map.of("key", "value"));
        connection.deliver(message);

        Assertions.assertEquals(1, connection.getBufferSize());
    }

    @Test
    @DisplayName("다수 메시지 순서 보장")
    void test4() {
        // 여러 메시지를 deliver()하면 전달 순서가 보장됨
        Connection connection = new Connection("connection-1", 10);

        Message message1 = new Message(Map.of("test", 1));
        Message message2 = new Message(Map.of("test", 2));

        List<Message> messages = new ArrayList<>();

        connection.setTarget(new InputPort() {
            @Override
            public String getName() {
                return "in";
            }

            @Override
            public void receive(Message message) {
                messages.add(message);
            }
        });

        connection.deliver(message1);
        connection.deliver(message2);

        Assertions.assertAll(
                () -> Assertions.assertEquals(2, messages.size()),
                () -> Assertions.assertSame(message1, messages.get(0)),
                () -> Assertions.assertSame(message2, messages.get(1))
        );
    }

}