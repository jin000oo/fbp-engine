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

import com.fbp.engine.node.PrintNode;
import com.fbp.engine.node.TimerNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FlowEngineTest {

    FlowEngine engine;

    Flow flow;

    @BeforeEach
    void setUp() {
        engine = new FlowEngine();
        flow = new Flow("flow");

        flow.addNode(new TimerNode("timer-1", 100))
                .addNode(new PrintNode("printer-1"))
                .connect("timer-1", "out", "printer-1", "in");
    }

    @Test
    @DisplayName("초기 상태")
    void test1() {
        // 생성 직후 getState()가 INITIALIZED
        Assertions.assertEquals(FlowEngine.State.INITIALIZED, engine.getState());
    }

    @Test
    @DisplayName("플로우 등록")
    void test2() {
        // register() 후 getFlows()에 해당 플로우가 포함됨
        engine.register(flow);

        Assertions.assertTrue(engine.getFlows().containsKey("flow"));
    }

    @Test
    @DisplayName("startFlow 정상")
    void test3() {
        // startFlow() 후 state가 RUNNING
        engine.register(flow);

        engine.startFlow("flow");

        Assertions.assertAll(
                () -> Assertions.assertEquals(FlowEngine.State.RUNNING, engine.getState()),
                () -> Assertions.assertEquals(Flow.State.RUNNING, flow.getState())
        );

        engine.shutdown();
    }

    @Test
    @DisplayName("startFlow — 없는 ID")
    void test4() {
        // 존재하지 않는 flowId → IllegalArgumentException
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            engine.startFlow("none-exist-id");
        });
    }

    @Test
    @DisplayName("startFlow — 유효성 실패")
    void test5() {
        // validate() 에러가 있는 Flow → IllegalStateException
        Flow errorFlow = new Flow("flow-error");
        engine.register(errorFlow);

        Assertions.assertThrows(IllegalStateException.class, () -> {
            engine.startFlow("flow-error");
        });
    }

    @Test
    @DisplayName("stopFlow 정상")
    void test6() {
        // startFlow() 후 stopFlow() → 해당 플로우가 정지됨
        engine.register(flow);

        engine.startFlow("flow");
        engine.stopFlow("flow");

        Assertions.assertEquals(Flow.State.STOPPED, flow.getState());

        engine.shutdown();
    }

    @Test
    @DisplayName("shutdown 전체")
    void test7() {
        // shutdown() 후 state가 STOPPED
        engine.register(flow);

        engine.startFlow("flow");

        engine.shutdown();

        Assertions.assertAll(
                () -> Assertions.assertEquals(FlowEngine.State.STOPPED, engine.getState()),
                () -> Assertions.assertEquals(Flow.State.STOPPED, flow.getState())
        );
    }

    @Test
    @DisplayName("다중 플로우 독립 동작")
    void test8() {
        // 2개 플로우 등록 후 하나만 stop해도 나머지는 영향 없음
        Flow flowA = new Flow("flow-A").addNode(new PrintNode("printer-1"));
        Flow flowB = new Flow("flow-B").addNode(new PrintNode("printer-2"));

        engine.register(flowA);
        engine.register(flowB);

        engine.startFlow("flow-A");
        engine.startFlow("flow-B");
        engine.stopFlow("flow-A");

        Assertions.assertAll(
                () -> Assertions.assertEquals(Flow.State.STOPPED, flowA.getState()),
                () -> Assertions.assertEquals(Flow.State.RUNNING, flowB.getState())
        );

        engine.shutdown();
    }

    @Test
    @DisplayName("listFlows 출력")
    void test9() {
        // 등록된 모든 플로우의 ID와 상태가 조회됨
        engine.register(flow);
        Assertions.assertDoesNotThrow(() -> engine.listFlows());
    }

}