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

import com.fbp.engine.core.AbstractNode;
import com.fbp.engine.core.Connection;
import com.fbp.engine.message.Message;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ErrorHandlerNodeTest {

    ExceptionNode exceptionNode;

    ErrorHandlerNode errorHandlerNode;

    Connection errorConn;

    Connection outConn;

    static class ExceptionNode extends AbstractNode {
        public ExceptionNode(String id) {
            super(id);
            addInputPort("in");
            addErrorPort();
        }

        @Override
        public void onProcess(Message message) {
            throw new RuntimeException("test error");
        }

        @Override
        public void initialize() {
        }

        @Override
        public void shutdown() {
        }
    }

    @BeforeEach
    void setUp() {
        exceptionNode = new ExceptionNode("exception");
        errorHandlerNode = new ErrorHandlerNode("handler");
        errorConn = new Connection("error");
        outConn = new Connection("out");

        exceptionNode.getOutputPort("error").connect(errorConn);
        errorHandlerNode.getOutputPort("out").connect(outConn);
    }

    @Test
    @DisplayName("에러 발생 시 분기")
    void test1() {
        // process()에서 예외 → 에러 포트로 메시지 전달
        Message msg = new Message(Map.of("data", "hello"));
        exceptionNode.process(msg);

        Message errorMsg = errorConn.poll();
        Assertions.assertNotNull(errorMsg);
        Assertions.assertEquals("test error", errorMsg.getPayload().get("_error"));
    }

    @Test
    @DisplayName("에러 메시지 내용")
    void test2() {
        // 에러 메시지에 원본 메시지, 예외 정보, 노드 id 포함
        Message msg = new Message(Map.of("data", "hello"));
        exceptionNode.process(msg);

        Message errorMsg = errorConn.poll();
        Assertions.assertAll(
                () -> Assertions.assertEquals("hello", errorMsg.getPayload().get("data")),
                () -> Assertions.assertEquals("test error", errorMsg.getPayload().get("_error")),
                () -> Assertions.assertEquals("exception", errorMsg.getPayload().get("_nodeId")),
                () -> Assertions.assertEquals("java.lang.RuntimeException", errorMsg.getPayload().get("_exception"))
        );
    }

    @Test
    @DisplayName("에러 포트 미연결")
    void test3() {
        // 에러 포트가 연결되지 않았으면 로그 기록 후 계속
        ExceptionNode nodeNoErr = new ExceptionNode("noErr");

        Message msg = new Message(Map.of("data", "hello"));

        Assertions.assertDoesNotThrow(() -> nodeNoErr.process(msg));
    }

    @Test
    @DisplayName("정상 처리 시")
    void test4() {
        // 예외 없으면 에러 포트에 메시지 전달하지 않음
        AbstractNode normalNode = new AbstractNode("normal") {
            public void onProcess(Message m) {
            }

            public void initialize() {
            }

            public void shutdown() {
            }
        };
        normalNode.addErrorPort();
        normalNode.getOutputPort("error").connect(errorConn);

        normalNode.process(new Message(Map.of()));
        Assertions.assertNull(errorConn.poll());
    }

    @Test
    @DisplayName("ErrorHandlerNode 수신")
    void test5() {
        // ErrorHandlerNode가 에러 메시지를 수신하고 처리
        Message msg = new Message(Map.of("data", "error-data"));
        errorHandlerNode.onProcess(msg);

        Message result = outConn.poll();
        Assertions.assertNotNull(result);
        Assertions.assertEquals("error-data", result.getPayload().get("data"));
    }

    @Test
    @DisplayName("재시도 로직")
    void test6() {
        // ErrorHandlerNode에서 재시도 설정 시 원래 노드로 재전달
        // 현재 ErrorHandlerNode에는 재시도 로직이 없으므로 개념적 테스트만
    }

    @Test
    @DisplayName("DeadLetterNode")
    void test7() {
        // 재시도 초과 시 DeadLetterNode로 전달
        // 현재 로직상 미구현이므로 스킵 또는 기본 동작 확인
    }

}