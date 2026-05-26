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

import com.fbp.engine.core.Connection;
import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.ModbusReaderNode;
import com.fbp.engine.node.ModbusWriterNode;
import com.fbp.engine.node.TimerNode;
import com.fbp.engine.node.TransformNode;
import com.fbp.engine.protocol.ModbusTcpSimulator;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
class ModbusIntegrationTest {

    ModbusTcpSimulator simulator;

    FlowEngine engine;

    @BeforeEach
    void setUp() throws InterruptedException {
        simulator = new ModbusTcpSimulator(55000, 10);
        simulator.start();

        Thread.sleep(100);

        engine = new FlowEngine();
    }

    @AfterEach
    void tearDown() {
        engine.shutdown();
        simulator.stop();
    }

    @Test
    @DisplayName("Reader → 레지스터 읽기")
    void test1() {
        // 시뮬레이터의 레지스터 값이 정확히 읽힘
        simulator.setRegister(0, 1234);

        Flow flow = new Flow("flow");

        TimerNode timer = new TimerNode("timer", 500);
        ModbusReaderNode reader = new ModbusReaderNode("modbus-reader", Map.of(
                "host", "localhost",
                "port", 55000,
                "slaveId", 1,
                "startAddress", 0,
                "count", 1,
                "registerMapping", Map.of(
                        "0", Map.of(
                                "name", "sensor",
                                "scale", 1.0)
                )
        ));

        Connection connection = new Connection("out");

        flow.addNode(timer)
                .addNode(reader);

        flow.connect("timer", "out", "modbus-reader", "trigger");

        reader.getOutputPort("out").connect(connection);

        engine.register(flow);
        engine.startFlow(flow.getId());

        Message msg = connection.take();

        Assertions.assertAll(
                () -> Assertions.assertNotNull(msg),
                () -> Assertions.assertEquals(1234.0, (double) msg.get("sensor"))
        );
    }

    @Test
    @DisplayName("Writer → 레지스터 쓰기")
    void test2() throws InterruptedException {
        // 기록한 값이 시뮬레이터에서 확인됨
        Flow flow = new Flow("flow");

        TimerNode timer = new TimerNode("timer", 500);
        TransformNode transformer = new TransformNode("transformer", msg -> msg.withEntry("target", 5678));

        ModbusWriterNode writer = new ModbusWriterNode("modbus-writer", Map.of(
                "host", "localhost",
                "port", 55000,
                "slaveId", 1,
                "registerAddress", 5,
                "valueField", "target"
        ));

        flow.addNode(timer)
                .addNode(transformer)
                .addNode(writer);

        flow.connect("timer", "out", "transformer", "in")
                .connect("transformer", "out", "modbus-writer", "in");

        engine.register(flow);
        engine.startFlow(flow.getId());

        Thread.sleep(1000);

        Assertions.assertEquals(5678, simulator.getRegister(5));
    }

    @Test
    @DisplayName("Reader → Writer 파이프라인")
    void test3() throws InterruptedException {
        // 읽은 값을 기반으로 다른 레지스터에 쓰기 성공
        simulator.setRegister(0, 999);

        Flow flow = new Flow("flow");

        flow.addNode(new TimerNode("timer", 500))
                .addNode(new ModbusReaderNode("modbus-reader", Map.of(
                        "host", "localhost",
                        "port", 55000,
                        "slaveId", 1,
                        "startAddress", 0,
                        "count", 1,
                        "registerMapping", Map.of(
                                "0", Map.of(
                                        "name", "val",
                                        "scale", 1.0)))))
                .addNode(new ModbusWriterNode("modbus-writer", Map.of(
                        "host", "localhost",
                        "port", 55000,
                        "slaveId", 1,
                        "registerAddress", 1,
                        "valueField", "val")));

        flow.connect("timer", "out", "modbus-reader", "trigger")
                .connect("modbus-reader", "out", "modbus-writer", "in");

        engine.register(flow);
        engine.startFlow(flow.getId());

        Thread.sleep(1000);

        Assertions.assertEquals(999, simulator.getRegister(1));
    }

//    @Test
//    @DisplayName("연결 끊김 처리")
//    void test4() throws InterruptedException {
//        // 시뮬레이터 중지 시 에러 포트로 에러 메시지 전달
//        Flow flow = new Flow("flow");
//
//        ModbusReaderNode reader = new ModbusReaderNode("modbus-reader", Map.of(
//                "host", "localhost",
//                "port", 55000,
//                "slaveId", 1,
//                "startAddress", 0,
//                "count", 1
//        ));
//
//        Connection connection = new Connection("error");
//
//        flow.addNode(new TimerNode("timer", 500))
//                .addNode(reader);
//
//        flow.connect("timer", "out", "modbus-reader", "trigger");
//
//        reader.getOutputPort("error").connect(connection);
//
//        engine.register(flow);
//        engine.startFlow(flow.getId());
//
//        Thread.sleep(800);
//        simulator.stop(); // 장비 다운
//        Thread.sleep(1000);
//
//        Message message = connection.poll();
//
//        Assertions.assertAll(
//                () -> Assertions.assertNotNull(message),
//                () -> Assertions.assertTrue(message.hasKey("error"))
//        );
//    }

}
