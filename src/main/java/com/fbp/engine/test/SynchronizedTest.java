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

package com.fbp.engine.test;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SynchronizedTest {

    public static void main(String[] args) {
        List<String> buffer = new ArrayList<>();

        Thread producer = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                synchronized (buffer) {
                    buffer.add("메시지-" + i);
                    buffer.notify();
                }

                try {
                    Thread.sleep(100);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            synchronized (buffer) {
                buffer.add("END");
                buffer.notify();
            }
        });

        Thread consumer = new Thread(() -> {
            while (true) {
                synchronized (buffer) {
                    while (buffer.isEmpty()) {
                        try {
                            buffer.wait();

                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    String message = buffer.remove(0);

                    if ("END".equals(message)) {
                        log.debug("소비자 끝");
                        break;
                    }

                    log.debug("소비: {}", message);
                }
            }
        });

        producer.start();
        consumer.start();
    }

}
