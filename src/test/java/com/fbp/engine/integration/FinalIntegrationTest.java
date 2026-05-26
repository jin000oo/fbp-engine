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

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.AlertNode;
import com.fbp.engine.node.CollectorNode;
import com.fbp.engine.node.FileWriterNode;
import com.fbp.engine.node.LogNode;
import com.fbp.engine.node.TemperatureSensorNode;
import com.fbp.engine.node.ThresholdFilterNode;
import com.fbp.engine.node.TimerNode;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FinalIntegrationTest {

    String TEST_FILE_PATH = "test.log";

    FlowEngine engine;

    Flow flow;

    CollectorNode alertCollector;

    CollectorNode normalCollector;

    @BeforeEach
    void setUp() {
        engine = new FlowEngine();
        flow = new Flow("final-test-flow");

        flow.addNode(new TimerNode("timer", 100))
                .addNode(new TemperatureSensorNode("temperature-sensor", 15.0, 45.0))
                .addNode(new ThresholdFilterNode("threshold-filter", "temperature", 30.0))
                .addNode(new AlertNode("alerter"))
                .addNode(new LogNode("logger"))
                .addNode(new FileWriterNode("file-writer", TEST_FILE_PATH));

        alertCollector = new CollectorNode("collector-alert");
        normalCollector = new CollectorNode("collector-normal");
        flow.addNode(alertCollector).addNode(normalCollector);

        flow.connect("timer", "out", "temperature-sensor", "trigger")
                .connect("temperature-sensor", "out", "threshold-filter", "in")
                .connect("threshold-filter", "alert", "alerter", "in")
                .connect("threshold-filter", "alert", "collector-alert", "in")
                .connect("threshold-filter", "normal", "logger", "in")
                .connect("logger", "out", "file-writer", "in")
                .connect("threshold-filter", "normal", "collector-normal", "in");
    }

    @AfterEach
    void tearDown() {
        new File(TEST_FILE_PATH).delete();
    }

    @Test
    @DisplayName("엔진 시작/종료")
    void test1() throws InterruptedException {
        // FlowEngine이 플로우를 정상 시작하고, shutdown 후 STOPPED 상태
        engine.register(flow);
        engine.startFlow("final-test-flow");
        Assertions.assertEquals(FlowEngine.State.RUNNING, engine.getState());

        Thread.sleep(1000);

        flow.getNodes().get("timer").shutdown();
        engine.shutdown();
        Assertions.assertEquals(FlowEngine.State.STOPPED, engine.getState());
    }

    @Test
    @DisplayName("alert 경로 정확성")
    void test2() throws InterruptedException {
        // alert 경로의 CollectorNode에 수집된 메시지의 temperature가 모두 30도 초과
        engine.register(flow);
        engine.startFlow("final-test-flow");

        Thread.sleep(1000);

        flow.getNodes().get("timer").shutdown();
        engine.shutdown();

        List<Message> alertMessages = alertCollector.getCollected();

        for (Message message : alertMessages) {
            double temperature = ((Number) message.get("temperature")).doubleValue();
            Assertions.assertTrue(temperature > 30);
        }
    }

    @Test
    @DisplayName("normal 경로 정확성")
    void test3() throws InterruptedException {
        // normal 경로의 CollectorNode에 수집된 메시지의 temperature가 모두 30도 이하
        engine.register(flow);
        engine.startFlow("final-test-flow");

        Thread.sleep(1000);

        flow.getNodes().get("timer").shutdown();
        engine.shutdown();

        List<Message> normalMessages = normalCollector.getCollected();

        for (Message message : normalMessages) {
            double temperature = ((Number) message.get("temperature")).doubleValue();
            Assertions.assertTrue(temperature <= 30);
        }
    }

    @Test
    @DisplayName("전체 분기 완전성")
    void test4() throws InterruptedException {
        // alert 수집 수 + normal 수집 수 = 전체 센서 생성 수 (누락 없음)
        engine.register(flow);
        engine.startFlow("final-test-flow");

        Thread.sleep(1000);

        flow.getNodes().get("timer").shutdown();
        engine.shutdown();

        List<Message> alertMessages = alertCollector.getCollected();
        List<Message> normalMessages = normalCollector.getCollected();

        Assertions.assertTrue((alertMessages.size() + normalMessages.size()) >= 10);
    }

    @Test
    @DisplayName("파일 기록 검증")
    void test5() throws IOException, InterruptedException {
        // FileWriterNode가 기록한 파일의 줄 수 = normal 경로 메시지 수
        engine.register(flow);
        engine.startFlow("final-test-flow");

        Thread.sleep(1000);

        flow.getNodes().get("timer").shutdown();
        engine.shutdown();

        List<Message> normalMessages = normalCollector.getCollected();

        File file = new File(TEST_FILE_PATH);

        Assertions.assertAll(
                () -> Assertions.assertTrue(file.exists()),
                () -> Assertions.assertEquals(normalMessages.size(), Files.lines(file.toPath()).count())
        );
    }

    @Test
    @DisplayName("센서 데이터 형식")
    void test6() throws InterruptedException {
        // 모든 수집 메시지에 "sensorId", "temperature", "unit" 키가 존재
        engine.register(flow);
        engine.startFlow("final-test-flow");

        Thread.sleep(1000);

        flow.getNodes().get("timer").shutdown();
        engine.shutdown();

        List<Message> alertMessages = alertCollector.getCollected();
        List<Message> normalMessages = normalCollector.getCollected();

        List<Message> allMessages = new ArrayList<>();
        allMessages.addAll(alertMessages);
        allMessages.addAll(normalMessages);

        for (Message message : allMessages) {
            Assertions.assertAll(
                    () -> Assertions.assertTrue(message.hasKey("sensorId")),
                    () -> Assertions.assertTrue(message.hasKey("temperature")),
                    () -> Assertions.assertTrue(message.hasKey("unit"))
            );
        }
    }

    @Test
    @DisplayName("온도 범위")
    void test7() throws InterruptedException {
        // 모든 수집 메시지의 temperature가 15.0~45.0 범위 이내
        engine.register(flow);
        engine.startFlow("final-test-flow");

        Thread.sleep(1000);

        flow.getNodes().get("timer").shutdown();
        engine.shutdown();

        List<Message> alertMessages = alertCollector.getCollected();
        List<Message> normalMessages = normalCollector.getCollected();

        List<Message> allMessages = new ArrayList<>();
        allMessages.addAll(alertMessages);
        allMessages.addAll(normalMessages);

        for (Message message : allMessages) {
            double temperature = ((Number) message.get("temperature")).doubleValue();
            Assertions.assertTrue(temperature >= 15 && temperature <= 45);
        }
    }

}
