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

package com.fbp.engine.metrics;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MetricsCollectorTest {

    MetricsCollector metricsCollector = MetricsCollector.getInstance();

    @BeforeEach
    void setUp() {
        metricsCollector.reset();
    }

    @Test
    @DisplayName("처리 건수 기록")
    void test1() {
        // recordProcessing 호출 후 처리 건수 증가
        metricsCollector.recordProcessing("node-1", 1000L, true);
        metricsCollector.recordProcessing("node-1", 1000L, true);

        Assertions.assertEquals(2, metricsCollector.getNodeMetrics("node-1").getProcessed().get());
    }

    @Test
    @DisplayName("에러 건수 기록")
    void test2() {
        // 실패로 기록 시 에러 카운트 증가
        metricsCollector.recordProcessing("node-1", 1000L, true);
        metricsCollector.recordProcessing("node-1", 1000L, false);

        Assertions.assertEquals(1, metricsCollector.getNodeMetrics("node-1").getErrors().get());
    }

    @Test
    @DisplayName("평균 처리 시간")
    void test3() {
        // 여러 번 기록 후 평균 처리 시간 계산이 정확함
        metricsCollector.recordProcessing("node-1", 10_000_000L, true);
        metricsCollector.recordProcessing("node-1", 30_000_000L, true);

        Assertions.assertEquals(20.0, metricsCollector.getNodeMetrics("node-1").getAverageTimeMs());
    }

    @Test
    @DisplayName("멀티스레드 안전성")
    void test4() throws InterruptedException {
        // 10개 스레드에서 동시에 기록해도 카운트가 정확함
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                for (int j = 0; j < 1000; j++) {
                    metricsCollector.recordProcessing("node", 1_000_000L, true);
                }

                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();

        Assertions.assertEquals(10_000, metricsCollector.getNodeMetrics("node").getProcessed().get());
    }

    @Test
    @DisplayName("노드별 분리")
    void test5() {
        // 서로 다른 노드의 메트릭이 독립적으로 관리됨
        metricsCollector.recordProcessing("node-1", 1000L, true);
        metricsCollector.recordProcessing("node-2", 1000L, true);
        metricsCollector.recordProcessing("node-2", 1000L, true);

        Assertions.assertAll(
                () -> Assertions.assertEquals(
                        1, metricsCollector.getNodeMetrics("node-1").getProcessed().get()),
                () -> Assertions.assertEquals(
                        2, metricsCollector.getNodeMetrics("node-2").getProcessed().get())
        );
    }

    @Test
    @DisplayName("리셋")
    void test6() {
        // 메트릭 초기화 후 카운트가 0
        metricsCollector.recordProcessing("node-1", 1000L, true);

        metricsCollector.reset();

        Assertions.assertTrue(metricsCollector.getAllMetrics().isEmpty());
    }

    @Test
    @DisplayName("존재하지 않는 노드")
    void test7() {
        // 미등록 노드 id로 조회 시 빈 메트릭 또는 null
        Assertions.assertNull(metricsCollector.getNodeMetrics("unknown"));
    }

}