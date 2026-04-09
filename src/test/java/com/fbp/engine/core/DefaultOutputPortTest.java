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
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultOutputPortTest {

    @Mock
    Connection connection1;

    @Mock
    Connection connection2;

    @Test
    @DisplayName("단일 Connection 전달")
    void test1() {
        // send()하면 연결된 Connection에 메시지가 전달됨
        DefaultOutputPort port = new DefaultOutputPort("out");
        port.connect(connection1);

        Message message = new Message(Map.of("test", 1));
        port.send(message);

        Mockito.verify(connection1, Mockito.times(1)).deliver(message);
    }

    @Test
    @DisplayName("다중 Connection 전달 (1:N)")
    void test2() {
        // 2개의 Connection을 연결하고 send()하면 양쪽 모두 메시지를 수신
        DefaultOutputPort port = new DefaultOutputPort("out");
        port.connect(connection1);
        port.connect(connection2);

        Message message = new Message(Map.of("test", 1));
        port.send(message);

        Mockito.verify(connection1, Mockito.times(1)).deliver(message);
        Mockito.verify(connection2, Mockito.times(1)).deliver(message);
    }

    @Test
    @DisplayName("Connection 미연결 시")
    void test3() {
        // connect()하지 않고 send()해도 예외가 발생하지 않음
        DefaultOutputPort port = new DefaultOutputPort("out");

        Message message = new Message(Map.of("test", 1));

        Assertions.assertDoesNotThrow(() -> port.send(message));
    }

}