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
class DefaultInputPortTest {

    @Mock
    Node node;

    @Test
    @DisplayName("receive 시 owner 호출")
    void test1() {
        // receive()하면 소속 노드의 process()가 호출됨
        DefaultInputPort port = new DefaultInputPort("in", node);

        Message message = new Message(Map.of("test", 1));
        port.receive(message);

        Mockito.verify(node, Mockito.times(1)).process(message);
    }

    @Test
    @DisplayName("포트 이름 확인")
    void test2() {
        // getName()이 생성 시 지정한 이름을 반환
        DefaultInputPort port = new DefaultInputPort("in", node);

        Assertions.assertEquals("in", port.getName());
    }

}