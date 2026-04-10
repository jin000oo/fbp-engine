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
import com.fbp.engine.node.LogNode;
import com.fbp.engine.node.PrintNode;
import com.fbp.engine.node.SplitNode;
import com.fbp.engine.node.TimerNode;
import com.fbp.engine.node.TransformNode;
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

//        System.out.println("===== 과제 5-6 =====");
//        test7();

//        System.out.println("===== 과제 6-2 =====");
//        test8();

//        System.out.println("===== 과제 6-4 =====");
//        test9();

        System.out.println("===== 과제 6-5 =====");
        test10();
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

    private static void test8() {
        GeneratorNode generator = new GeneratorNode("generator-1");
        TransformNode f2c = new TransformNode("f2c", message -> {
            Double fahrenheit = message.get("temperature");
            double celsius = (fahrenheit - 32) * 5.0 / 9.0;

            return message.withEntry("temperature", celsius);
        });
        PrintNode printer = new PrintNode("printer-1");

        Connection connection1 = new Connection("connection-1");
        Connection connection2 = new Connection("connection-2");
        connection1.setTarget(f2c.getInputPort("in"));
        connection2.setTarget(printer.getInputPort("in"));

        generator.getOutputPort("out").connect(connection1);
        f2c.getOutputPort("out").connect(connection2);

        Thread f2cThread = new Thread(() -> {
            System.out.println("[test8 - F2C] 대기 시작");

            while (running) {
                Message message = connection1.poll();

                if (message != null) {
                    f2c.getInputPort("in").receive(message);
                }
            }

            System.out.println("[test8 - F2C] 스레드 종료");
        });

        Thread printerThread = new Thread(() -> {
            System.out.println("[test8 - Printer] 대기 시작");

            while (running) {
                Message message = connection2.poll();

                if (message != null) {
                    printer.getInputPort("in").receive(message);
                }
            }

            System.out.println("[test8 - Printer] 스레드 종료");
        });

        Thread generatorThread = new Thread(() -> {
            generator.generate("temperature", 100.0);

            while (connection1.getBufferSize() > 0 || connection2.getBufferSize() > 0) {
                try {
                    Thread.sleep(100);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            try {
                Thread.sleep(100);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            running = false;

            f2cThread.interrupt();
            printerThread.interrupt();
        });

        f2cThread.start();
        printerThread.start();
        generatorThread.start();
    }

    private static void test9() {
        TimerNode timer = new TimerNode("timer-1", 500);
        SplitNode split = new SplitNode("split-1", "tick", 3);
        PrintNode warnPrinter = new PrintNode("printer-warn");
        PrintNode passPrinter = new PrintNode("printer-pass");

        Connection connection = new Connection("connection-1");
        Connection matchConnection = new Connection("connection-match");
        Connection mismatchConnection = new Connection("connection-mismatch");
        connection.setTarget(split.getInputPort("in"));
        matchConnection.setTarget(warnPrinter.getInputPort("in"));
        mismatchConnection.setTarget(passPrinter.getInputPort("in"));

        timer.getOutputPort("out").connect(connection);
        split.getOutputPort("match").connect(matchConnection);
        split.getOutputPort("mismatch").connect(mismatchConnection);

        Thread splitThread = new Thread(() -> {
            while (running) {
                Message message = connection.poll();

                if (message != null) {
                    split.getInputPort("in").receive(message);
                }
            }
        });

        Thread warnThread = new Thread(() -> {
            while (running) {
                Message message = matchConnection.poll();
                if (message != null) {
                    warnPrinter.getInputPort("in").receive(message);
                }
            }
        });

        Thread passThread = new Thread(() -> {
            while (running) {
                Message message = mismatchConnection.poll();
                if (message != null) {
                    passPrinter.getInputPort("in").receive(message);
                }
            }
        });

        splitThread.start();
        warnThread.start();
        passThread.start();

        System.out.println("=== 분기 플로우 시작 ===");
        warnPrinter.initialize();
        passPrinter.initialize();
        split.initialize();
        timer.initialize();

        try {
            Thread.sleep(3000);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("=== 플로우 종료 ===");
        timer.shutdown();
        split.shutdown();
        warnPrinter.shutdown();
        passPrinter.shutdown();

        running = false;
        splitThread.interrupt();
        warnThread.interrupt();
        passThread.interrupt();
    }

    private static void test10() {
        TimerNode timer = new TimerNode("timer-1", 1000);
        LogNode logger = new LogNode("logger-1");
        FilterNode filter = new FilterNode("filter-1", "tick", 3);
        PrintNode printer = new PrintNode("printer-1");

        Connection connection1 = new Connection("connection-1");
        Connection connection2 = new Connection("connection-2");
        Connection connection3 = new Connection("connection-3");
        connection1.setTarget(logger.getInputPort("in"));
        connection2.setTarget(filter.getInputPort("in"));
        connection3.setTarget(printer.getInputPort("in"));

        timer.getOutputPort("out").connect(connection1);
        logger.getOutputPort("out").connect(connection2);
        filter.getOutputPort("out").connect(connection3);

        Thread loggerThread = new Thread(() -> {
            while (running) {
                Message message = connection1.poll();

                if (message != null) {
                    logger.getInputPort("in").receive(message);
                }
            }
        });

        Thread filterThread = new Thread(() -> {
            while (running) {
                Message message = connection2.poll();

                if (message != null) {
                    filter.getInputPort("in").receive(message);
                }
            }
        });

        Thread printerThread = new Thread(() -> {
            while (running) {
                Message message = connection3.poll();

                if (message != null) {
                    printer.getInputPort("in").receive(message);
                }
            }
        });

        loggerThread.start();
        filterThread.start();
        printerThread.start();

        printer.initialize();
        filter.initialize();
        logger.initialize();
        timer.initialize();

        try {
            Thread.sleep(7000);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        timer.shutdown();

        while (connection1.getBufferSize() > 0 || connection2.getBufferSize() > 0 || connection3.getBufferSize() > 0) {
            try {
                Thread.sleep(200);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        logger.shutdown();
        filter.shutdown();
        printer.shutdown();

        running = false;
        loggerThread.interrupt();
        filterThread.interrupt();
        printerThread.interrupt();
    }

}
