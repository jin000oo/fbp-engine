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
import com.fbp.engine.core.Node;
import com.fbp.engine.message.Message;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PrintNodeTest {

    @Test
    @DisplayName("getId 반환")
    void test1() {
        // 생성 시 지정한 ID가 getId()로 반환됨
        PrintNode printNode = new PrintNode("printer-1");
        Assertions.assertEquals("printer-1", printNode.getId());
    }

    @Test
    @DisplayName("process 정상 동작")
    void test2() {
        // process() 호출 시 예외가 발생하지 않음
        PrintNode printNode = new PrintNode("printer-1");
        Message message = new Message(Map.of("data", "test"));
        Assertions.assertDoesNotThrow(() -> printNode.process(message));
    }

    @Test
    @DisplayName("Node 인터페이스 구현")
    void test3() {
        // PrintNode 인스턴스를 Node 타입 변수에 대입 가능
        PrintNode printNode = new PrintNode("printer-1");
        Assertions.assertInstanceOf(Node.class, printNode);

        Node node = printNode;
        Assertions.assertNotNull(node);
    }

    @Test
    @DisplayName("InputPort 조회")
    void test4() {
        // getInputPort()가 null이 아님
        PrintNode printNode = new PrintNode("printer-1");
        Assertions.assertNotNull(printNode.getInputPort("in"));
    }

    @Test
    @DisplayName("InputPort를 통한 수신")
    void test5() {
        // InputPort의 receive()를 호출하면 process()가 실행됨
        PrintNode printNode = new PrintNode("printer-1");
        Message message = new Message(Map.of("test", 1));
        Assertions.assertDoesNotThrow(() -> printNode.getInputPort("in").receive(message));
    }

    @Test
    @DisplayName("포트 구성 확인")
    void test6() {
        // getInputPort("in")이 null이 아님
        PrintNode printer = new PrintNode("printer-1");
        Assertions.assertNotNull(printer.getInputPort("in"));
    }

    @Test
    @DisplayName("process 정상 동작")
    void test7() {
        // 메시지를 process()하면 예외 없이 처리됨
        PrintNode printer = new PrintNode("printer-1");
        Message message = new Message(Map.of("test", 1));
        Assertions.assertDoesNotThrow(() -> printer.process(message));
    }

    @Test
    @DisplayName("AbstractNode 상속 확인")
    void test8() {
        // PrintNode가 AbstractNode의 인스턴스임 (instanceof)
        PrintNode printer = new PrintNode("printer-1");
        Assertions.assertTrue(printer instanceof AbstractNode);
    }

}