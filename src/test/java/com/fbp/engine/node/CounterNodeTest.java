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

class CounterNodeTest {

    @Test
    @DisplayName("count 키 추가")
    void test1() {
        // 첫 번째 메시지 전달 후 "count" 키 값이 1
        CounterNode counter = new CounterNode("counter-1");

        Connection connection = new Connection("out");
        counter.getOutputPort("out").connect(connection);

        counter.process(new Message(Map.of("name", "Alice")));

        Message message = connection.poll();

        Assertions.assertEquals(1, (Integer) message.get("count"));
    }

    @Test
    @DisplayName("count 누적")
    void test2() {
        // 3개 메시지 전달 후 마지막 메시지의 "count" 값이 3
        CounterNode counter = new CounterNode("counter-1");

        Connection connection = new Connection("out");
        counter.getOutputPort("out").connect(connection);

        counter.process(new Message(Map.of("name", "Alice")));
        counter.process(new Message(Map.of("name", "Bob")));
        counter.process(new Message(Map.of("name", "Charlie")));

        Message message1 = connection.poll();
        Message message2 = connection.poll();
        Message message3 = connection.poll();

        Assertions.assertEquals(1, (Integer) message1.get("count"));
        Assertions.assertEquals(2, (Integer) message2.get("count"));
        Assertions.assertEquals(3, (Integer) message3.get("count"));
    }

    @Test
    @DisplayName("원본 키 유지")
    void test3() {
        // 원본 메시지의 다른 키-값이 변환 후에도 유지됨
        CounterNode counter = new CounterNode("counter-1");

        Connection connection = new Connection("out");
        counter.getOutputPort("out").connect(connection);

        counter.process(new Message(Map.of("name", "Alice")));

        Message message = connection.poll();

        Assertions.assertEquals("Alice", message.get("name"));
    }

}