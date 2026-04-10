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
import com.fbp.engine.node.TimerNode;
import java.util.HashMap;
import java.util.Map;

public class Main {

    static volatile boolean running = true;

    public static void main(String[] args) {
//        System.out.println("===== 과제 2-7 =====");
//        test1();

//        System.out.println("===== 과제 3-7 =====");
//        test2();

//        System.out.println("===== 과제 3-8 =====");
//        test3();

//        System.out.println("===== 과제 3-10 =====");
//        test4();

//        System.out.println("===== 과제 4-4 =====");
//        test5();

//        System.out.println("===== 과제 4-5 =====");
//        test6();

        System.out.println("===== 과제 5-6 =====");
        test7();
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

        Connection connection = new Connection("connection-1");
        connection.setTarget(printer.getInputPort("in"));

        generator.getOutputPort("out").connect(connection);

        generator.generate("temperature", 25.5);
    }

    private static void test3() {
        GeneratorNode generator = new GeneratorNode("generator-1");
        PrintNode printer1 = new PrintNode("printer-1");
        PrintNode printer2 = new PrintNode("printer-2");

        Connection connection1 = new Connection("connection-1");
        Connection connection2 = new Connection("connection-2");
        connection1.setTarget(printer1.getInputPort("in"));
        connection2.setTarget(printer2.getInputPort("in"));

        generator.getOutputPort("out").connect(connection1);
        generator.getOutputPort("out").connect(connection2);

        generator.generate("test", "success");
    }

    private static void test4() {
        GeneratorNode generator = new GeneratorNode("generator-1");
        FilterNode filter = new FilterNode("filter-1", "temperature", 35.0);
        PrintNode printer = new PrintNode("printer-1");

        Connection connection1 = new Connection("connection-1");
        Connection connection2 = new Connection("connection-2");
        connection1.setTarget(filter.getInputPort("in"));
        connection2.setTarget(printer.getInputPort("in"));

        generator.getOutputPort("out").connect(connection1);
        filter.getOutputPort("out").connect(connection2);

        generator.generate("temperature", 25.0);
        generator.generate("temperature", 35.0);
    }

    private static void test5() {
        GeneratorNode generator = new GeneratorNode("generator-1");
        PrintNode printer = new PrintNode("printer-1");

        Connection connection = new Connection("connection");
        connection.setTarget(printer.getInputPort("in"));

        generator.getOutputPort("out").connect(connection);

        Thread producer = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                System.out.printf("[test5 - Producer] 데이터 생성 (%d/5)%n", i);
                generator.generate("count", i);

                try {
                    Thread.sleep(1000);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            System.out.println("[test5 - Producer] 생산 완료");
        });

        Thread consumer = new Thread(() -> {
            System.out.println("[test5 - Consumer] 대기 시작");

            while (true) {
                Message message = connection.poll();

                if (message != null) {
                    printer.getInputPort("in").receive(message);
                }
            }
        });

        producer.start();
        consumer.start();
    }

    private static void test6() {
        GeneratorNode generator = new GeneratorNode("generator-1");
        FilterNode filter = new FilterNode("filter-1", "temperature", 35.0);
        PrintNode printer = new PrintNode("printer-1");

        Connection connection1 = new Connection("connection-1");
        Connection connection2 = new Connection("connection-2");
        connection1.setTarget(filter.getInputPort("in"));
        connection2.setTarget(printer.getInputPort("in"));

        generator.getOutputPort("out").connect(connection1);
        filter.getOutputPort("out").connect(connection2);

        Thread filterThread = new Thread(() -> {
            System.out.println("[test6 - Filter] 대기 시작");

            while (running) {
                Message message = connection1.poll();

                if (message != null) {
                    filter.getInputPort("in").receive(message);
                }
            }

            System.out.println("[test6 - Filter] 스레드 종료");
        });

        Thread printerThread = new Thread(() -> {
            System.out.println("[test6 - Printer] 대기 시작");

            while (running) {
                Message message = connection2.poll();

                if (message != null) {
                    printer.getInputPort("in").receive(message);

                    System.out.printf("  -> [현재 connection-2 버퍼 사이즈: %d]%n", connection2.getBufferSize());

                    try {
                        Thread.sleep(1000);

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            System.out.println("[test6 - Printer] 스레드 종료");
        });

        Thread generatorThread = new Thread(() -> {
            double[] testTemperatures = {32.5, 36.0, 29.0, 38.5, 35.0, 20.0, 39.0};

            for (double testTemperature : testTemperatures) {
                generator.generate("temperature", testTemperature);

                try {
                    Thread.sleep(100);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            System.out.println("[test6 - Generator] 생산 완료");

            while (connection1.getBufferSize() > 0 || connection2.getBufferSize() > 0) {
                try {
                    Thread.sleep(500);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            System.out.println("[test6 - Generator] 모든 처리 완료");
            running = false;

            filterThread.interrupt();
            printerThread.interrupt();
        });

        filterThread.start();
        printerThread.start();
        generatorThread.start();
    }

    private static void test7() {
        TimerNode timer = new TimerNode("timer-1", 500);
        FilterNode filter = new FilterNode("filter-1", "tick", 3);
        PrintNode printer = new PrintNode("printer-1");

        Connection connection1 = new Connection("connection-1");
        Connection connection2 = new Connection("connection-2");
        connection1.setTarget(filter.getInputPort("in"));
        connection2.setTarget(printer.getInputPort("in"));

        timer.getOutputPort("out").connect(connection1);
        filter.getOutputPort("out").connect(connection2);

        Thread filterThread = new Thread(() -> {
            while (running) {
                Message message = connection1.poll();

                if (message != null) {
                    filter.getInputPort("in").receive(message);
                }
            }
        });

        Thread printerThread = new Thread(() -> {
            while (running) {
                Message message = connection2.poll();

                if (message != null) {
                    printer.getInputPort("in").receive(message);
                }
            }
        });

        filterThread.start();
        printerThread.start();

        System.out.println("=== initialize ===");
        timer.initialize();
        filter.initialize();
        printer.initialize();

        try {
            Thread.sleep(3000);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("=== shutdown ===");
        printer.shutdown();
        filter.shutdown();
        timer.shutdown();

        running = false;

        filterThread.interrupt();
        printerThread.interrupt();
    }

}
