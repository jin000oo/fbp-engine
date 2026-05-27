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
import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.TemperatureSensorNode;
import com.fbp.engine.node.ThresholdFilterNode;
import com.fbp.engine.node.TimerNode;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TemperatureMonitoringIntegrationFlowTest {

    static class CollectorNode extends AbstractNode {

        final List<Message> collected = new ArrayList<>();

        public CollectorNode(String id) {
            super(id);
            addInputPort("in");
        }

        @Override
        public void onProcess(Message message) {
            collected.add(message);
        }

        @Override
        public void initialize() {
        }

        @Override
        public void shutdown() {
        }

    }

    @Test
    @DisplayName("alert 경로 검증")
    void test1() throws InterruptedException {
        // alert 경로의 CollectorNode에 수집된 메시지의 temperature가 모두 임계값 초과
        FlowEngine engine = new FlowEngine();

        Flow flow = new Flow("flow");

        CollectorNode alertCollector = new CollectorNode("alert");
        CollectorNode normalCollector = new CollectorNode("normal");

        flow.addNode(new TimerNode("timer", 100))
                .addNode(new TemperatureSensorNode("temperature-sensor", 15, 45))
                .addNode(new ThresholdFilterNode("threshold-filter", "temperature", 30))
                .addNode(alertCollector)
                .addNode(normalCollector);

        flow.connect("timer", "out", "temperature-sensor", "trigger")
                .connect("temperature-sensor", "out", "threshold-filter", "in")
                .connect("threshold-filter", "alert", "alert", "in")
                .connect("threshold-filter", "normal", "normal", "in");

        engine.register(flow);

        engine.startFlow(flow.getId());
        Thread.sleep(2000);
        engine.stopFlow(flow.getId());

        engine.shutdown();

        for (Message message : alertCollector.collected) {
            Assertions.assertTrue(((Number) message.get("temperature")).doubleValue() > 30);
        }
    }

    @Test
    @DisplayName("normal 경로 검증")
    void test2() throws InterruptedException {
        // normal 경로의 CollectorNode에 수집된 메시지의 temperature가 모두 임계값 이하
        FlowEngine engine = new FlowEngine();

        Flow flow = new Flow("flow");

        CollectorNode alertCollector = new CollectorNode("alert");
        CollectorNode normalCollector = new CollectorNode("normal");

        flow.addNode(new TimerNode("timer", 100))
                .addNode(new TemperatureSensorNode("temperature-sensor", 15, 45))
                .addNode(new ThresholdFilterNode("threshold-filter", "temperature", 30))
                .addNode(alertCollector)
                .addNode(normalCollector);

        flow.connect("timer", "out", "temperature-sensor", "trigger")
                .connect("temperature-sensor", "out", "threshold-filter", "in")
                .connect("threshold-filter", "alert", "alert", "in")
                .connect("threshold-filter", "normal", "normal", "in");

        engine.register(flow);

        engine.startFlow(flow.getId());
        Thread.sleep(2000);
        engine.stopFlow(flow.getId());

        engine.shutdown();

        for (Message message : normalCollector.collected) {
            Assertions.assertTrue(((Number) message.get("temperature")).doubleValue() <= 30);

        }
    }

    @Test
    @DisplayName("전체 메시지 수")
    void test3() throws InterruptedException {
        // alert + normal 수집 수의 합이 TimerNode의 tick 수와 일치
        FlowEngine engine = new FlowEngine();

        Flow flow = new Flow("flow");

        CollectorNode alertCollector = new CollectorNode("alert");
        CollectorNode normalCollector = new CollectorNode("normal");

        flow.addNode(new TimerNode("timer", 100))
                .addNode(new TemperatureSensorNode("temperature-sensor", 15, 45))
                .addNode(new ThresholdFilterNode("threshold-filter", "temperature", 30))
                .addNode(alertCollector)
                .addNode(normalCollector);

        flow.connect("timer", "out", "temperature-sensor", "trigger")
                .connect("temperature-sensor", "out", "threshold-filter", "in")
                .connect("threshold-filter", "alert", "alert", "in")
                .connect("threshold-filter", "normal", "normal", "in");

        engine.register(flow);

        engine.startFlow(flow.getId());
        Thread.sleep(2000);
        engine.stopFlow(flow.getId());

        engine.shutdown();

        Assertions.assertTrue((alertCollector.collected.size() + normalCollector.collected.size()) >= 9);
    }

}
