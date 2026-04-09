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

import com.fbp.engine.core.Connection;
import com.fbp.engine.core.Node;
import com.fbp.engine.message.Message;
import com.fbp.engine.node.FilterNode;
import com.fbp.engine.node.GeneratorNode;
import com.fbp.engine.node.PrintNode;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        System.out.println("===== 과제 2-7 =====");
        test1();

        System.out.println("===== 과제 3-7 =====");
        test2();

        System.out.println("===== 과제 3-8 =====");
        test3();

        System.out.println("===== 과제 3-10 =====");
        test4();
    }

    private static void test1() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("temperature", 25.5);

        Message message = new Message(payload);

        Node printer = new PrintNode("printer-1");
        printer.process(message);
    }

    private static void test2() {
        GeneratorNode generator = new GeneratorNode("generator-1");
        PrintNode printer = new PrintNode("printer-1");

        Connection connection = new Connection("connection-1", 10);
        connection.setTarget(printer.getInputPort());

        generator.getOutputPort().connect(connection);

        generator.generate("temperature", 25.5);
    }

    private static void test3() {
        GeneratorNode generator = new GeneratorNode("generator-1");
        PrintNode printer1 = new PrintNode("printer-1");
        PrintNode printer2 = new PrintNode("printer-2");

        Connection connection1 = new Connection("connection-1", 10);
        Connection connection2 = new Connection("connection-2", 10);
        connection1.setTarget(printer1.getInputPort());
        connection2.setTarget(printer2.getInputPort());

        generator.getOutputPort().connect(connection1);
        generator.getOutputPort().connect(connection2);

        generator.generate("test", "success");
    }

    private static void test4() {
        GeneratorNode generator = new GeneratorNode("generator-1");
        FilterNode filter = new FilterNode("filter-1", "temperature", 35.0);
        PrintNode printer = new PrintNode("printer-1");

        Connection connection1 = new Connection("connection-1", 10);
        Connection connection2 = new Connection("connection-2", 10);
        connection1.setTarget(filter.getInputPort());
        connection2.setTarget(printer.getInputPort());

        generator.getOutputPort().connect(connection1);
        filter.getOutputPort().connect(connection2);

        generator.generate("temperature", 25.0);
        generator.generate("temperature", 35.0);
    }

}
