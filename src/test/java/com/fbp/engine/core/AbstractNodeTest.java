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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AbstractNodeTest {

    static class TestNode extends AbstractNode {

        boolean isCalled = false;

        public TestNode(String id) {
            super(id);
            addInputPort("in");
            addOutputPort("out");
        }

        @Override
        public void onProcess(Message message) {
            isCalled = true;
        }

        @Override
        public void initialize() {
        }

        @Override
        public void shutdown() {
        }

        public void testSend(String portName, Message message) {
            send(portName, message);
        }

    }

    TestNode node;

    @BeforeEach
    void setUp() {
        node = new TestNode("test-node");
    }

    @Test
    @DisplayName("getId 반환")
    void test1() {
        // 생성 시 지정한 ID가 반환됨
        Assertions.assertEquals("test-node", node.getId());
    }

    @Test
    @DisplayName("addInputPort 등록")
    void test2() {
        // addInputPort("in") 후 getInputPort("in")이 null이 아님
        Assertions.assertNotNull(node.getInputPort("in"));
    }

    @Test
    @DisplayName("addOutputPort 등록")
    void test3() {
        // addOutputPort("out") 후 getOutputPort("out")이 null이 아님
        Assertions.assertNotNull(node.getOutputPort("out"));
    }

    @Test
    @DisplayName("미등록 포트 조회")
    void test4() {
        // getInputPort("없는포트")가 null 반환
        Assertions.assertNull(node.getInputPort("없는포트"));
    }

    @Test
    @DisplayName("process → onProcess 호출")
    void test5() {
        // process() 호출 시 하위 클래스의 onProcess()가 실행됨 (간단한 테스트용 하위 클래스 작성)
        node.process(new Message(Map.of("test", 1)));
        Assertions.assertTrue(node.isCalled);
    }

    @Test
    @DisplayName("send로 메시지 전달")
    void test6() {
        // OutputPort에 Connection을 연결 후 send()하면 상대측에서 수신
        Connection connection = new Connection("connection-1");
        node.getOutputPort("out").connect(connection);

        Message message = new Message(Map.of("test", 1));
        node.testSend("out", message);

        Assertions.assertSame(message, connection.poll());
    }

}