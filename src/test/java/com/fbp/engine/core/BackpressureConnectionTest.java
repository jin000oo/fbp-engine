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
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BackpressureConnectionTest {

    @Test
    @DisplayName("Block 전략")
    void test1() throws InterruptedException {
        // 큐 가득 참 → send()가 블로킹됨 (타임아웃으로 확인)
        BackpressureConnection connection = new BackpressureConnection("connection", 1, new BackpressureStrategy() {
            @Override
            public void handle(BlockingQueue<Message> queue, Message message) throws InterruptedException {
                BackpressureStrategy.super.handle(queue, message);
            }
        });
        connection.deliver(new Message(Map.of("key", 1)));

        CountDownLatch latch = new CountDownLatch(1);

        Thread thread = new Thread(() -> {
            connection.deliver(new Message(Map.of("key", 2)));
            latch.countDown();
        });

        thread.start();

        boolean finished = latch.await(100, TimeUnit.MILLISECONDS);

        Assertions.assertFalse(finished);

        thread.interrupt();
    }

    @Test
    @DisplayName("DropOldest 전략")
    void test2() {
        // 큐 가득 참 + 새 메시지 → 가장 오래된 메시지 제거 확인
        BackpressureConnection connection = new BackpressureConnection("connection", 2, new BackpressureStrategy() {
            @Override
            public void handle(BlockingQueue<Message> queue, Message message) throws InterruptedException {
                BackpressureStrategy.super.handle(queue, message);
            }
        });
        connection.deliver(new Message(Map.of("key", 1)));
        connection.deliver(new Message(Map.of("key", 2)));
        connection.deliver(new Message(Map.of("key", 3)));

        Assertions.assertAll(
                () -> Assertions.assertEquals(2, connection.poll().getPayload().get("key")),
                () -> Assertions.assertEquals(3, connection.poll().getPayload().get("key"))
        );
    }

    @Test
    @DisplayName("DropNewest 전략")
    void test3() {
        // 큐 가득 참 + 새 메시지 → 새 메시지가 버려짐
        BackpressureConnection connection = new BackpressureConnection("connection", 2, new BackpressureStrategy() {
            @Override
            public void handle(BlockingQueue<Message> queue, Message message) throws InterruptedException {
                BackpressureStrategy.super.handle(queue, message);
            }
        });
        connection.deliver(new Message(Map.of("key", 1)));
        connection.deliver(new Message(Map.of("key", 2)));
        connection.deliver(new Message(Map.of("key", 3)));

        Assertions.assertAll(
                () -> Assertions.assertEquals(1, connection.poll().getPayload().get("key")),
                () -> Assertions.assertEquals(2, connection.poll().getPayload().get("key")),
                () -> Assertions.assertNull(connection.poll())
        );
    }

    @Test
    @DisplayName("전략 변경")
    void test4() throws NoSuchFieldException, IllegalAccessException {
        // 런타임에 전략 변경 후 새 전략이 적용됨
        BackpressureConnection connection = new BackpressureConnection("connection", 1, new BackpressureStrategy() {
            @Override
            public void handle(BlockingQueue<Message> queue, Message message) throws InterruptedException {
                BackpressureStrategy.super.handle(queue, message);
            }
        });

        Field strategyField = BackpressureConnection.class.getDeclaredField("strategy");
        strategyField.setAccessible(true);
        strategyField.set(connection, new DropOldestStrategy());

        Assertions.assertEquals(2, connection.poll().getPayload().get("key"));
    }

    @Test
    @DisplayName("큐 크기 설정")
    void test5() {
        // 생성 시 지정한 큐 용량이 적용됨
        BackpressureConnection connection = new BackpressureConnection("connection", 3, new BackpressureStrategy() {
            @Override
            public void handle(BlockingQueue<Message> queue, Message message) throws InterruptedException {
                BackpressureStrategy.super.handle(queue, message);
            }
        });
        connection.deliver(new Message(Map.of("key", 1)));
        connection.deliver(new Message(Map.of("key", 2)));
        connection.deliver(new Message(Map.of("key", 3)));
        connection.deliver(new Message(Map.of("key", 4)));

        Assertions.assertEquals(3, connection.getBufferSize());
    }

    @Test
    @DisplayName("드롭 카운트")
    void test6() {
        // DropOldest/DropNewest 전략에서 드롭된 메시지 수 메트릭
    }

    @Test
    @DisplayName("멀티스레드")
    void test7() {
        // 여러 생산자 스레드에서 동시 전송 시 데이터 손실 없음
    }

}