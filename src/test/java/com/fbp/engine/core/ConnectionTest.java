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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

//@ExtendWith(MockitoExtension.class)
class ConnectionTest {

//    @Mock
//    InputPort inputPort;

    static volatile boolean isBlocked = true;

//    @Test
//    @DisplayName("deliver 후 target 수신")
//    void test1() {
//        // deliver()한 메시지가 target InputPort의 receive()를 통해 노드에 전달됨
//        Connection connection = new Connection("connection-1");
//        connection.setTarget(inputPort);
//
//        Message message = new Message(Map.of("key", "value"));
//        connection.deliver(message);
//
//        Mockito.verify(inputPort, Mockito.times(1)).receive(message);
//    }

    @Test
    @DisplayName("target 미설정 시 동작")
    void test2() {
        // target이 null인 상태에서 deliver()해도 예외가 발생하지 않음
        Connection connection = new Connection("connection-1");

        Message message = new Message(Map.of("key", "value"));

        Assertions.assertDoesNotThrow(() -> connection.deliver(message));
    }

    @Test
    @DisplayName("버퍼 크기 확인")
    void test3() {
        // deliver() 후 getBufferSize()가 예상값과 일치
        Connection connection = new Connection("connection-1");

        Message message = new Message(Map.of("key", "value"));
        connection.deliver(message);

        Assertions.assertEquals(1, connection.getBufferSize());
    }

//    @Test
//    @DisplayName("다수 메시지 순서 보장")
//    void test4() {
//        // 여러 메시지를 deliver()하면 전달 순서가 보장됨
//        Connection connection = new Connection("connection-1");
//
//        Message message1 = new Message(Map.of("test", 1));
//        Message message2 = new Message(Map.of("test", 2));
//
//        List<Message> messages = new ArrayList<>();
//
//        connection.setTarget(new InputPort() {
//            @Override
//            public String getName() {
//                return "in";
//            }
//
//            @Override
//            public void receive(Message message) {
//                messages.add(message);
//            }
//        });
//
//        connection.deliver(message1);
//        connection.deliver(message2);
//
//        Assertions.assertAll(
//                () -> Assertions.assertEquals(2, messages.size()),
//                () -> Assertions.assertSame(message1, messages.get(0)),
//                () -> Assertions.assertSame(message2, messages.get(1))
//        );
//    }

    @Test
    @DisplayName("deliver-poll 기본 동작")
    void test5() {
        // deliver()한 메시지를 poll()로 꺼낼 수 있음
        Connection connection = new Connection("connection-1");

        Message message = new Message(Map.of("test", 1));
        connection.deliver(message);

        Assertions.assertSame(message, connection.poll());
    }

    @Test
    @DisplayName("메시지 순서 보장")
    void test6() {
        // 3개 메시지를 deliver()하면 poll() 순서가 FIFO
        Connection connection = new Connection("connection-1");

        Message message1 = new Message(Map.of("test", 1));
        Message message2 = new Message(Map.of("test", 2));
        Message message3 = new Message(Map.of("test", 3));
        connection.deliver(message1);
        connection.deliver(message2);
        connection.deliver(message3);

        Assertions.assertAll(
                () -> Assertions.assertSame(message1, connection.poll()),
                () -> Assertions.assertSame(message2, connection.poll()),
                () -> Assertions.assertSame(message3, connection.poll())
        );
    }

    @Test
    @DisplayName("멀티스레드 deliver-poll")
    void test7() {
        // 별도 스레드에서 deliver하고, 다른 스레드에서 poll하여 수신 성공 (CountDownLatch 활용)
        Connection connection = new Connection("connection-1");

        Message message = new Message(Map.of("test", 1));

        CountDownLatch countDownLatch = new CountDownLatch(1);
        final Message[] received = {null};

        Thread consumer = new Thread(() -> {
            received[0] = connection.take();
            countDownLatch.countDown();
        });
        consumer.start();

        Thread producer = new Thread(() -> {
            connection.deliver(message);
        });
        producer.start();

        Assertions.assertAll(
                () -> Assertions.assertTrue(countDownLatch.await(2, TimeUnit.SECONDS)),
                () -> Assertions.assertSame(message, received[0])
        );
    }

    @Test
    @DisplayName("poll 대기 동작")
    void test8() throws InterruptedException {
        // deliver 전에 poll()을 호출한 스레드가 메시지 도착까지 블로킹됨 (타임아웃 내 수신 확인)
        Connection connection = new Connection("connection-1");

        Thread consumer = new Thread(() -> {
            connection.take();
            isBlocked = false;
        });
        consumer.start();

        Thread.sleep(100);
        Assertions.assertTrue(isBlocked);

        connection.deliver(new Message(Map.of("key", "value")));
        consumer.join(1000);
        Assertions.assertFalse(isBlocked);
    }

    @Test
    @DisplayName("버퍼 크기 제한")
    void test9() throws InterruptedException {
        // 버퍼 크기 2로 생성한 Connection에 3개 deliver 시도 → 3번째가 블로킹됨 (별도 스레드에서 확인)
        Connection connection = new Connection("connection-1", 2);

        connection.deliver(new Message(Map.of("test", 1)));
        connection.deliver(new Message(Map.of("test", 2)));

        Thread producer = new Thread(() -> {
            connection.deliver(new Message(Map.of("test", 3)));
        });
        producer.start();

        Thread.sleep(100);
        Assertions.assertEquals(Thread.State.WAITING, producer.getState());

        connection.poll();
        producer.join(1000);
        Assertions.assertEquals(Thread.State.TERMINATED, producer.getState());
    }

    @Test
    @DisplayName("버퍼 크기 조회")
    void test10() {
        // deliver() 후 getBufferSize()가 예상 값과 일치
        Connection connection = new Connection("connection-1");
        Assertions.assertEquals(0, connection.getBufferSize());

        connection.deliver(new Message(Map.of("key", "value")));
        Assertions.assertEquals(1, connection.getBufferSize());
    }

}