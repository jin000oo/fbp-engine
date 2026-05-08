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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NodeMetricsTest {

    @Test
    @DisplayName("초기값")
    void test1() {
        // 생성 직후 모든 카운터가 0
        NodeMetrics nodeMetrics = new NodeMetrics();

        Assertions.assertAll(
                () -> Assertions.assertEquals(0, nodeMetrics.getProcessed().get()),
                () -> Assertions.assertEquals(0, nodeMetrics.getErrors().get()),
                () -> Assertions.assertEquals(0, nodeMetrics.getTotalTimeNs().get())
        );
    }

    @Test
    @DisplayName("increment")
    void test2() {
        // 처리 건수, 에러 건수 증가
        NodeMetrics nodeMetrics = new NodeMetrics();

        nodeMetrics.record(1_000_000L, true);
        nodeMetrics.record(2_000_000L, false);

        Assertions.assertAll(
                () -> Assertions.assertEquals(2, nodeMetrics.getProcessed().get()),
                () -> Assertions.assertEquals(1, nodeMetrics.getErrors().get())
        );
    }

    @Test
    @DisplayName("평균 계산")
    void test3() {
        // 처리 시간 합계 / 처리 건수 = 평균
        NodeMetrics nodeMetrics = new NodeMetrics();

        nodeMetrics.record(10_000_000L, true);
        nodeMetrics.record(20_000_000L, true);

        Assertions.assertEquals(15.0, nodeMetrics.getAverageTimeMs());
    }

    @Test
    @DisplayName("스냅샷")
    void test4() {
        // 현재 메트릭의 불변 스냅샷 반환
        NodeMetrics nodeMetrics = new NodeMetrics();

        nodeMetrics.record(15_000_000L, true);
        nodeMetrics.record(15_000_000L, false);

        Assertions.assertAll(
                () -> Assertions.assertEquals(2, nodeMetrics.getProcessed().get()),
                () -> Assertions.assertEquals(1, nodeMetrics.getErrors().get()),
                () -> Assertions.assertEquals(15.0, nodeMetrics.getAverageTimeMs())
        );
    }

}