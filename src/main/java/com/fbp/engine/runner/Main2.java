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

package com.fbp.engine.runner;

import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.node.FilterNode;
import com.fbp.engine.node.GeneratorNode;
import com.fbp.engine.node.ModbusReaderNode;
import com.fbp.engine.node.ModbusWriterNode;
import com.fbp.engine.node.MqttPublisherNode;
import com.fbp.engine.node.MqttSubscriberNode;
import com.fbp.engine.node.PrintNode;
import com.fbp.engine.node.ThresholdFilterNode;
import com.fbp.engine.node.TimerNode;
import com.fbp.engine.node.TransformNode;
import com.fbp.engine.protocol.ModbusException;
import com.fbp.engine.protocol.ModbusTcpClient;
import com.fbp.engine.protocol.ModbusTcpSimulator;
import com.fbp.engine.rule.RuleNode;
import java.io.IOException;
import java.util.Map;

public class Main2 {

    public static void main(String[] args) throws IOException, ModbusException, InterruptedException {
//        System.out.println("===== 과제 2-5 =====");
//        test1();

//        System.out.println("===== 과제 2-6 =====");
//        test2();

//        System.out.println("===== 과제 2-7 =====");
//        test3();

//        System.out.println("===== 과제 3-5 =====");
//        test4();

//        System.out.println("===== 과제 3-8 =====");
//        test5();

//        System.out.println("===== 과제 3-9 =====");
//        test6();

        System.out.println("===== 과제 4-4 =====");
        test7();
    }

    private static void test1() throws InterruptedException {
        FlowEngine engine = new FlowEngine();

        Flow flow = new Flow("flow");

        MqttSubscriberNode subscriber = new MqttSubscriberNode("mqtt-subscriber", Map.of(
                "brokerUrl", "tcp://localhost:1883",
                "clientId", "fbp-sub-client",
                "topic", "sensor/temp"
        ));
        PrintNode printer = new PrintNode("printer");

        flow.addNode(subscriber)
                .addNode(printer);

        flow.connect("mqtt-subscriber", "out", "printer", "in");

        engine.register(flow);
        engine.startFlow(flow.getId());

        Thread.sleep(60000);

        engine.shutdown();
    }

    private static void test2() throws InterruptedException {
        FlowEngine engine = new FlowEngine();

        Flow flow = new Flow("flow");

        MqttPublisherNode publisher = new MqttPublisherNode("mqtt-publisher", Map.of(
                "brokerUrl", "tcp://localhost:1883",
                "clientId", "fbp-pub-client",
                "topic", "test/temp"
        ));
        GeneratorNode generator = new GeneratorNode("generator");

        flow.addNode(publisher)
                .addNode(generator);

        flow.connect("generator", "out", "mqtt-publisher", "in");

        engine.register(flow);
        engine.startFlow(flow.getId());

        Thread.sleep(60000);

        engine.shutdown();
    }

    private static void test3() throws InterruptedException {
        FlowEngine engine = new FlowEngine();

        Flow flow = new Flow("flow");

        MqttSubscriberNode subscriber = new MqttSubscriberNode("mqtt-subscriber", Map.of(
                "brokerUrl", "tcp://localhost:1883",
                "clientId", "fbp-sub-client",
                "topic", "sensor/temp"
        ));
        FilterNode filter = new FilterNode("filter", "temperature", 30);
        MqttPublisherNode publisher = new MqttPublisherNode("mqtt-publisher", Map.of(
                "brokerUrl", "tcp://localhost:1883",
                "clientId", "fbp-pub-client",
                "topic", "alert/temp"
        ));

        flow.addNode(subscriber).addNode(filter).addNode(publisher);

        flow.connect("mqtt-subscriber", "out", "filter", "in")
                .connect("filter", "out", "mqtt-publisher", "in");

        engine.register(flow);
        engine.startFlow(flow.getId());

        Thread.sleep(60000);

        engine.shutdown();
    }

    private static void test4() throws IOException, ModbusException {
        ModbusTcpSimulator simulator = new ModbusTcpSimulator(5020, 10);
        simulator.start();

        simulator.setRegister(0, 250);
        simulator.setRegister(1, 600);
        simulator.setRegister(2, 1);
        simulator.setRegister(3, 0);
        simulator.setRegister(4, 0);
        simulator.setRegister(5, 0);
        simulator.setRegister(6, 0);
        simulator.setRegister(7, 0);
        simulator.setRegister(8, 0);
        simulator.setRegister(9, 0);

        ModbusTcpClient client = new ModbusTcpClient("localhost", 5020);
        client.connect();

        int[] readRegisters = client.readHoldingRegisters(1, 0, 3);
        System.out.printf("초기 읽기 결과: [%d, %d, %d]%n", readRegisters[0], readRegisters[1], readRegisters[2]);

        client.writeSingleRegister(1, 2, 100);

        int[] updateRegisters = client.readHoldingRegisters(1, 0, 3);
        System.out.printf("쓰기 후 읽기 결과: [%d, %d, %d]%n", updateRegisters[0], updateRegisters[1], updateRegisters[2]);

        client.disconnect();
        simulator.stop();
    }

    private static void test5() throws InterruptedException {
        ModbusTcpSimulator simulator = new ModbusTcpSimulator(5020, 10);
        simulator.start();

        simulator.setRegister(0, 255);

        FlowEngine engine = new FlowEngine();

        Flow flow = new Flow("flow");

        TimerNode timer = new TimerNode("timer", 1000);
        ModbusReaderNode modbusReader = new ModbusReaderNode("modbus-reader", Map.of(
                "host", "localhost",
                "port", 5020,
                "slaveId", 1,
                "startAddress", 0,
                "count", 1,
                "registerMapping", Map.of("0", Map.of("name", "temperature", "scale", 0.1))
        ));
        PrintNode printer = new PrintNode("printer");

        flow.addNode(timer)
                .addNode(modbusReader)
                .addNode(printer);

        flow.connect("timer", "out", "modbus-reader", "trigger")
                .connect("modbus-reader", "out", "printer", "in");

        engine.register(flow);
        engine.startFlow(flow.getId());

        Thread.sleep(3000);

        engine.shutdown();
        simulator.stop();
    }

    private static void test6() throws InterruptedException {
        ModbusTcpSimulator simulator = new ModbusTcpSimulator(5020, 10);
        simulator.start();

        simulator.setRegister(0, 355);
        simulator.setRegister(2, 0);

        FlowEngine engine = new FlowEngine();

        Flow flow = new Flow("flow");

        TimerNode timer = new TimerNode("timer", 1000);
        ModbusReaderNode modbusReader = new ModbusReaderNode("modbus-reader", Map.of(
                "host", "localhost",
                "port", 5020,
                "slaveId", 1,
                "startAddress", 0,
                "count", 1,
                "registerMapping", Map.of("0", Map.of("name", "temperature", "scale", 0.1))
        ));
        ThresholdFilterNode thresholdFilter = new ThresholdFilterNode("threshold-filter", "temperature", 30.0);
        TransformNode transformer = new TransformNode("transformer", msg -> msg.withEntry("alertFlag", 1));
        ModbusWriterNode modbusWriter = new ModbusWriterNode("modbus-writer", Map.of(
                "host", "localhost",
                "port", 5020,
                "slaveId", 1,
                "registerAddress", 2,
                "valueField", "alertFlag"
        ));

        flow.addNode(timer)
                .addNode(modbusReader)
                .addNode(thresholdFilter)
                .addNode(transformer)
                .addNode(modbusWriter);

        flow.connect("timer", "out", "modbus-reader", "trigger")
                .connect("modbus-reader", "out", "threshold-filter", "in")
                .connect("threshold-filter", "alert", "transformer", "in")
                .connect("transformer", "out", "modbus-writer", "in");

        engine.register(flow);
        engine.startFlow(flow.getId());

        Thread.sleep(3000);

        System.out.printf("주소 2번 최종 확인: %d%n", simulator.getRegister(2));

        engine.shutdown();
        simulator.stop();
    }

    private static void test7() throws InterruptedException {
        ModbusTcpSimulator simulator = new ModbusTcpSimulator(5020, 10);
        simulator.stop();

        simulator.setRegister(2, 0);

        FlowEngine engine = new FlowEngine();

        Flow flow = new Flow("flow");

        MqttSubscriberNode subscriber = new MqttSubscriberNode("mqtt-subscriber", Map.of(
                "brokerUrl", "tcp://localhost:1883",
                "clientId", "fbp-sub-client",
                "topic", "sensor/temp"
        ));
        RuleNode rule = new RuleNode("rule", "temperature > 30.0");
        MqttPublisherNode publisher = new MqttPublisherNode("mqtt-publisher", Map.of(
                "brokerUrl", "tcp://localhost:1883",
                "clientId", "fbp-pub-client",
                "topic", "alert/temp"
        ));
        ModbusWriterNode writer = new ModbusWriterNode("modbus-writer", Map.of(
                "host", "localhost",
                "port", 5020,
                "slaveId", 1,
                "registerAddress", 2,
                "valueField", "alertFlag"
        ));
        TransformNode transformer = new TransformNode("transformer", msg -> msg.withEntry("alertFlag", 1));
        PrintNode printer1 = new PrintNode("normal-printer");
        PrintNode printer2 = new PrintNode("alert-printer");

        flow.addNode(subscriber)
                .addNode(rule)
                .addNode(publisher)
                .addNode(writer)
                .addNode(transformer)
                .addNode(printer1)
                .addNode(printer2);

        flow.connect("mqtt-subscriber", "out", "rule", "in")
                .connect("rule", "match", "alert-printer", "in")
                .connect("rule", "match", "mqtt-publisher", "in")
                .connect("rule", "match", "transformer", "in")
                .connect("transformer", "out", "modbus-writer", "in")
                .connect("rule", "mismatch", "normal-printer", "in");

        engine.register(flow);
        engine.startFlow(flow.getId());

        Thread.sleep(60000);

        engine.shutdown();
        simulator.stop();
    }

}
