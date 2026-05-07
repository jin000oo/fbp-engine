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
public class ArrayListTest {

    public static void main(String[] args) {
        List<String> buffer = new ArrayList<>();

        Thread producer = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                buffer.add("메시지-" + i);

                try {
                    Thread.sleep(100);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            log.debug("생산자 끝");
        });

        Thread consumer = new Thread(() -> {
            while (true) {
                if (!buffer.isEmpty()) {
                    try {
                        String message = buffer.remove(0);

                        log.debug("소비: {}", message);

                    } catch (Exception e) {
                        log.error("충돌");
                    }
                }

                // 생산자가 끝났다는 걸 알 방법이 없음 -> 종료되지 않음
            }
        });

        producer.start();
        consumer.start();
    }

}
