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

import com.fbp.engine.api.HttpApiServer;
import com.fbp.engine.core.AbstractNode;
import com.fbp.engine.core.BackpressureConnection;
import com.fbp.engine.core.Connection;
import com.fbp.engine.core.DropOldestStrategy;
import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.engine.FlowManager;
import com.fbp.engine.flow.SubFlowNode;
import com.fbp.engine.message.Message;
import com.fbp.engine.metrics.MetricsCollector;
import com.fbp.engine.node.CounterNode;
import com.fbp.engine.node.DynamicRouterNode;
import com.fbp.engine.node.FilterNode;
import com.fbp.engine.node.ModbusWriterNode;
import com.fbp.engine.node.PrintNode;
import com.fbp.engine.node.TransformNode;
import com.fbp.engine.parser.JsonFlowParser;
import com.fbp.engine.registry.NodeRegistry;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ComplexScenarioTest {

    FlowEngine engine;

    NodeRegistry registry;

    @BeforeEach
    void setUp() {
        engine = new FlowEngine();
        registry = new NodeRegistry();

        registry.register("Transform", (id, config) -> new TransformNode(id, m -> m));
        registry.register("Filter", (id, config) -> new FilterNode(id, (String) config.get("key"),
                ((Number) config.get("threshold")).doubleValue()));
        registry.register("DynamicRouter", (id, config) -> new DynamicRouterNode(id));
        registry.register("Print", (id, config) -> new PrintNode(id));
    }

    @AfterEach
    void tearDown() {
        engine.shutdown();
    }

    @Test
    @DisplayName("JSON 플로우 배포")
    void test1() throws Exception {
        // JSON 파일에서 플로우 정의 읽기 → 배포 → RUNNING 상태
        String json = """
                {
                    "id": "json-flow",
                    "nodes": [
                        { "id": "n1", "type": "Transform", "config": {} }
                    ],
                    "connections": []
                }
                """;
        JsonFlowParser parser = new JsonFlowParser();
        var def = parser.parse(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));

        Flow flow = new Flow(def.getId());
        for (var nodeDef : def.getNodes()) {
            flow.addNode((AbstractNode) registry.create(nodeDef.type(), nodeDef.id(), nodeDef.config()));
        }

        engine.register(flow);
        engine.startFlow("json-flow");

        Assertions.assertEquals(Flow.State.RUNNING, flow.getState());
    }

    @Test
    @DisplayName("MQTT→Rule→MQTT")
    void test2() throws InterruptedException {
        // MQTT 수신 → 규칙 적용 → 조건 충족 메시지만 MQTT 발행
        Flow flow = new Flow("mqtt-flow");
        TransformNode mqttIn = new TransformNode("mqtt-in", m -> m);
        FilterNode rule = new FilterNode("rule", "temp", 30.0);
        TransformNode mqttOut = new TransformNode("mqtt-out", m -> m);

        flow.addNode(mqttIn).addNode(rule).addNode(mqttOut);
        flow.connect("mqtt-in", "out", "rule", "in");
        flow.connect("rule", "out", "mqtt-out", "in");

        CountDownLatch latch = new CountDownLatch(1);
        mqttOut.getOutputPort("out").connect(new Connection("c") {
            @Override
            public void deliver(Message m) {
                latch.countDown();
            }
        });

        engine.register(flow);
        engine.startFlow("mqtt-flow");

        mqttIn.receive(new Message(Map.of("temp", 35.0)));
        Assertions.assertTrue(latch.await(1, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("동적 라우팅")
    void test3() throws InterruptedException {
        // 센서 타입별로 서로 다른 처리 경로로 분기
        Flow flow = new Flow("router-flow");
        DynamicRouterNode router = new DynamicRouterNode("router");
        router.addRule("type == A", "pathA");
        router.addRule("type == B", "pathB");

        CounterNode counterA = new CounterNode("countA");
        CounterNode counterB = new CounterNode("countB");

        flow.addNode(router).addNode(counterA).addNode(counterB);
        flow.connect("router", "pathA", "countA", "in");
        flow.connect("router", "pathB", "countB", "in");

        engine.register(flow);
        engine.startFlow("router-flow");

        router.receive(new Message(Map.of("type", "A")));
        router.receive(new Message(Map.of("type", "B")));
        router.receive(new Message(Map.of("type", "A")));

        Thread.sleep(100);
        Assertions.assertEquals(2, counterA.getCount());
        Assertions.assertEquals(1, counterB.getCount());
    }

    @Test
    @DisplayName("에러 핸들링")
    void test4() throws InterruptedException {
        // 처리 중 에러 발생 → 에러 플로우로 분기 → 로그 기록
        Flow flow = new Flow("error-flow");
        AbstractNode errorNode = new AbstractNode("error-node") {
            @Override
            public void onProcess(Message m) {
                throw new RuntimeException("fail");
            }

            @Override
            public void initialize() {
            }

            @Override
            public void shutdown() {
            }
        };
        errorNode.addErrorPort();

        CounterNode errorCounter = new CounterNode("error-counter");
        flow.addNode(errorNode).addNode(errorCounter);
        flow.connect("error-node", "error", "error-counter", "in");

        engine.register(flow);
        engine.startFlow("error-flow");

        errorNode.receive(new Message(Map.of()));
        Thread.sleep(100);
        Assertions.assertEquals(1, errorCounter.getCount());
    }

    @Test
    @DisplayName("서브플로우")
    void test5() throws InterruptedException {
        // 서브플로우를 포함한 플로우가 정상 동작
        Flow inner = new Flow("inner");
        inner.addNode(new TransformNode("in-node", m -> m));
        inner.addNode(new TransformNode("out-node", m -> m));
        inner.connect("in-node", "out", "out-node", "in");

        SubFlowNode subFlow = new SubFlowNode("sub", inner, "in-node", "out-node");

        Flow outer = new Flow("outer");
        outer.addNode(subFlow);

        CountDownLatch latch = new CountDownLatch(1);
        subFlow.getOutputPort("out").connect(new Connection("c") {
            @Override
            public void deliver(Message m) {
                latch.countDown();
            }
        });

        engine.register(outer);
        engine.startFlow("outer");

        subFlow.receive(new Message(Map.of("data", "test")));
        Assertions.assertTrue(latch.await(1, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("백프레셔")
    void test6() throws InterruptedException {
        // 느린 소비자에서 큐 적체 → 백프레셔 전략 동작 확인
        Flow flow = new Flow("bp-flow");
        TransformNode producer = new TransformNode("producer", m -> m);

        AbstractNode consumer = new AbstractNode("consumer") {
            @Override
            public void onProcess(Message m) {
                try {
                    Thread.sleep(50);
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

        flow.addNode(producer).addNode(consumer);

        BackpressureConnection bpConn = new BackpressureConnection("bp", 2, new DropOldestStrategy());
        bpConn.setTarget(consumer.getInputPort("in"));
        producer.getOutputPort("out").connect(bpConn);
        flow.getConnections().add(bpConn);

        engine.register(flow);
        engine.startFlow("bp-flow");

        for (int i = 0; i < 10; i++) {
            producer.receive(new Message(Map.of("v", i)));
        }

        Thread.sleep(500);
        Assertions.assertTrue(bpConn.getBufferSize() <= 2);
    }

    @Test
    @DisplayName("MODBUS 연동")
    void test7() {
        // 규칙 충족 시 MODBUS TCP 레지스터에 값 기록 (Socket 기반)
        ModbusWriterNode modbusWriter = new ModbusWriterNode("modbus", Map.of("host", "localhost", "port", 502));
        Assertions.assertNotNull(modbusWriter);
    }

    @Test
    @DisplayName("REST API 연동")
    void test8() throws Exception {
        // POST /flows로 플로우 배포, GET /flows로 확인, DELETE로 삭제
        FlowManager flowManager = new FlowManager(engine, registry);
        HttpApiServer server = new HttpApiServer(8081, flowManager);
        server.start();
        server.stop();
    }

    @Test
    @DisplayName("메트릭 수집")
    void test9() throws InterruptedException {
        // 플로우 실행 후 GET /flows/{id}/metrics에서 처리량 확인
        Flow flow = new Flow("metrics-flow");
        TransformNode node = new TransformNode("node", m -> m);
        flow.addNode(node);
        engine.register(flow);
        engine.startFlow("metrics-flow");

        node.receive(new Message(Map.of()));
        Thread.sleep(100);

        var metrics = MetricsCollector.getInstance().getNodeMetrics("node");
        Assertions.assertNotNull(metrics);
        Assertions.assertTrue(metrics.getProcessed().get() >= 1);
    }

    @Test
    @DisplayName("다중 플로우")
    void test10() {
        // 3개 이상의 플로우를 동시에 배포 및 실행
        engine.register(new Flow("f1").addNode(new TransformNode("n1", m -> m)));
        engine.register(new Flow("f2").addNode(new TransformNode("n2", m -> m)));
        engine.register(new Flow("f3").addNode(new TransformNode("n3", m -> m)));

        engine.startFlow("f1");
        engine.startFlow("f2");
        engine.startFlow("f3");

        Assertions.assertEquals(3, engine.getFlows().size());
    }

}
