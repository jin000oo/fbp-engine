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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TimerNodeTest {

    TimerNode timer;

    Connection connection;

    @AfterEach
    void tearDown() {
        if (timer != null) {
            timer.shutdown();
        }
    }

    @Test
    @DisplayName("initialize 후 메시지 생성")
    void test1() throws InterruptedException {
        // initialize() 호출 후 일정 시간 대기하면 OutputPort로 메시지가 전송됨 (CollectorNode 또는 Connection의 poll로 확인)
        timer = new TimerNode("timer-1", 100);

        connection = new Connection("connection-1");
        timer.getOutputPort("out").connect(connection);

        timer.initialize();
        Thread.sleep(150);

        Assertions.assertNotNull(connection.poll());
    }

    @Test
    @DisplayName("tick 증가")
    void test2() throws InterruptedException {
        // 수신한 메시지들의 tick 값이 0, 1, 2, ... 순서로 증가
        timer = new TimerNode("timer-1", 100);

        connection = new Connection("connection-1");
        timer.getOutputPort("out").connect(connection);

        timer.initialize();
        Thread.sleep(350);

        Message message1 = connection.poll();
        Message message2 = connection.poll();
        Message message3 = connection.poll();

        Assertions.assertAll(
                () -> Assertions.assertEquals(0, (Integer) message1.get("tick")),
                () -> Assertions.assertEquals(1, (Integer) message2.get("tick")),
                () -> Assertions.assertEquals(2, (Integer) message3.get("tick"))
        );
    }

    @Test
    @DisplayName("shutdown 후 정지")
    void test3() throws InterruptedException {
        // shutdown() 호출 후에는 더 이상 메시지가 생성되지 않음
        timer = new TimerNode("timer-1", 100);

        connection = new Connection("connection-1");
        timer.getOutputPort("out").connect(connection);

        timer.initialize();
        Thread.sleep(150);
        timer.shutdown();

        while (connection.getBufferSize() > 0) {
            connection.poll();
        }

        Thread.sleep(200);

        Assertions.assertEquals(0, connection.getBufferSize());
    }

    @Test
    @DisplayName("주기 확인")
    void test4() throws InterruptedException {
        // 500ms 주기로 설정 시 2초간 대략 4개 메시지가 생성됨 (오차 허용)
        timer = new TimerNode("timer-1", 500);

        connection = new Connection("connection-1");
        timer.getOutputPort("out").connect(connection);

        timer.initialize();
        Thread.sleep(2100);

        timer.shutdown();

        int count = connection.getBufferSize();

        Assertions.assertTrue(count >= 4 && count <= 5);
    }

}
