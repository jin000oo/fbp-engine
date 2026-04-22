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
import com.fbp.engine.node.MqttPublisherNode;
import com.fbp.engine.node.MqttSubscriberNode;
import com.fbp.engine.node.PrintNode;
import java.util.Map;

public class Main2 {

    public static void main(String[] args) throws InterruptedException {
//        System.out.println("===== 과제 2-5 =====");
//        test1();

//        System.out.println("===== 과제 2-6 =====");
//        test2();

        System.out.println("===== 과제 2-7 =====");
        test3();
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

}
