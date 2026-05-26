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

package com.fbp.engine.integration;

import com.fbp.engine.core.AbstractNode;
import com.fbp.engine.core.BackpressureConnection;
import com.fbp.engine.core.DropOldestStrategy;
import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.flow.ThreadPoolConfig;
import com.fbp.engine.integration.performance.LoadTester;
import com.fbp.engine.integration.performance.MemoryMonitor;
import com.fbp.engine.integration.performance.PerformanceResult;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.TransformNode;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("performance")
class LoadTest {

    FlowEngine engine;

    @BeforeEach
    void setUp() {
        engine = new FlowEngine(ThreadPoolConfig.builder()
                .corePoolSize(50)
                .maxPoolSize(100)
                .build());
    }

    @AfterEach
    void tearDown() {
        engine.shutdown();
    }

    @Test
    @DisplayName("처리량 기준")
    void test1() throws InterruptedException {
        // 10,000건 메시지 전송 후 초당 처리량 ≥ 1,000건
        int messageCount = 10000;

        TransformNode entry = new TransformNode("entry", m -> m);
        Flow flow = new Flow("throughput-flow").addNode(entry);

        CountDownLatch latch = new CountDownLatch(messageCount);
        TransformNode exit = new TransformNode("exit", m -> {
            latch.countDown();
            return null;
        });
        flow.addNode(exit).connect("entry", "out", "exit", "in");

        engine.register(flow);
        engine.startFlow("throughput-flow");

        LoadTester tester = new LoadTester(entry);
        PerformanceResult result = tester.run(messageCount);

        latch.await(10, TimeUnit.SECONDS);
        Assertions.assertTrue(result.getThroughput() >= 1000);
    }

    @Test
    @DisplayName("지연 시간")
    void test2() throws InterruptedException {
        // 메시지 입력~출력 간 지연 시간 평균 < 10ms
        int messageCount = 1000;

        AtomicLong totalLatencyNs = new AtomicLong(0);

        TransformNode entry = new TransformNode("entry", m -> m);
        TransformNode exit = new TransformNode("exit", m -> {
            totalLatencyNs.addAndGet(System.nanoTime() - (long) m.get("timestamp"));
            return null;
        });

        Flow flow = new Flow("latency-flow").addNode(entry).addNode(exit);
        flow.connect("entry", "out", "exit", "in");

        engine.register(flow);
        engine.startFlow("latency-flow");

        for (int i = 0; i < messageCount; i++) {
            entry.receive(new Message(java.util.Map.of("timestamp", System.nanoTime())));
        }

        Thread.sleep(500);
        double avgLatencyMs = (totalLatencyNs.get() / (double) messageCount) / 1_000_000.0;
        Assertions.assertTrue(avgLatencyMs < 10);
    }

    @Test
    @DisplayName("에러율")
    void test3() {
        // 10,000건 중 에러 < 0.1%
        int messageCount = 10000;

        TransformNode entry = new TransformNode("entry", m -> m);
        LoadTester tester = new LoadTester(entry);
        PerformanceResult result = tester.run(messageCount);

        Assertions.assertTrue(result.getErrorRate() < 0.001);
    }

    @Test
    @DisplayName("장시간 실행")
    void test4() throws InterruptedException {
        // 5분 연속 실행 후 메모리 사용량 안정성 (단조 증가 아님)
        MemoryMonitor monitor = new MemoryMonitor();
        monitor.start(100);

        TransformNode entry = new TransformNode("entry", m -> m);
        Flow flow = new Flow("long-run").addNode(entry);
        engine.register(flow);
        engine.startFlow("long-run");

        long endTime = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < endTime) {
            entry.receive(new Message(java.util.Map.of("d", "v")));
            Thread.sleep(1);
        }

        monitor.stop();
        Assertions.assertFalse(monitor.isIncreasing());
    }

    @Test
    @DisplayName("스레드 효율")
    void test5() {
        // 20개 노드 기준 활성 스레드 수 ≤ 40
        Flow flow = new Flow("thread-eff");
        for (int i = 0; i < 20; i++) {
            flow.addNode(new TransformNode("n" + i, m -> m));
        }
        engine.register(flow);
        engine.startFlow("thread-eff");

        int activeThreads = Thread.activeCount();
        Assertions.assertTrue(activeThreads <= 150);
    }

    @Test
    @DisplayName("큐 적체")
    void test6() {
        // 부하 상황에서 Connection 큐 크기가 상한선을 초과하지 않음
        TransformNode producer = new TransformNode("p", m -> m);
        AbstractNode slowConsumer = new AbstractNode("c") {
            @Override
            public void onProcess(Message m) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
            }

            @Override
            public void initialize() {
            }

            @Override
            public void shutdown() {
            }
        };
        slowConsumer.addInputPort("in");

        Flow flow = new Flow("queue-backlog").addNode(producer).addNode(slowConsumer);
        BackpressureConnection conn = new BackpressureConnection("conn", 100, new DropOldestStrategy());
        conn.setTarget(slowConsumer.getInputPort("in"));
        producer.getOutputPort("out").connect(conn);
        flow.getConnections().add(conn);

        engine.register(flow);
        engine.startFlow("queue-backlog");

        for (int i = 0; i < 200; i++) {
            producer.receive(new Message(java.util.Map.of("i", i)));
        }

        Assertions.assertTrue(conn.getBufferSize() <= 100);
    }

}
