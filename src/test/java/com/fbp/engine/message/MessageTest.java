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

package com.fbp.engine.message;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MessageTest {

    private Map<String, Object> payload;
    private Message message;

    @BeforeEach
    void setUp() {
        payload = new HashMap<>();
        payload.put("temperature", 25.5);
        payload.put("location", "Gwangju");

        message = new Message(payload);
    }

    @Test
    @DisplayName("생성 시 ID 자동 할당")
    void test1() {
        // getId()가 null이 아니고 빈 문자열이 아님
        Assertions.assertAll(
                () -> Assertions.assertNotNull(message.getId()),
                () -> Assertions.assertFalse(message.getId().isEmpty())
        );
    }

    @Test
    @DisplayName("생성 시 timestamp 자동 기록")
    void test2() {
        // getTimestamp()가 0보다 큼
        Assertions.assertTrue(message.getTimestamp() > 0);
    }

    @Test
    @DisplayName("페이로드 조회")
    void test3() {
        // 생성 시 넣은 key-value를 get()으로 꺼낼 수 있음
        Assertions.assertEquals(25.5, message.get("temperature"));
    }

    @Test
    @DisplayName("제네릭 get 타입 캐스팅")
    void test4() {
        // get("temperature")의 반환 타입이 Double로 사용 가능
        Double temperature = message.get("temperature");
        Assertions.assertEquals(25.5, temperature);
    }

    @Test
    @DisplayName("존재하지 않는 키 조회")
    void test5() {
        // get("없는키")가 null 반환
        Assertions.assertNull(message.get("없는키"));
    }

    @Test
    @DisplayName("페이로드 불변 — 외부 수정 차단")
    void test6() {
        // getPayload().put()하면 UnsupportedOperationException 발생
        Assertions.assertThrows(UnsupportedOperationException.class, () ->
                message.getPayload().put("key", "value"));
    }

    @Test
    @DisplayName("페이로드 불변 — 원본 Map 수정 무영향")
    void test7() {
        // Message 생성에 사용한 원본 Map을 수정해도 Message 내용은 변하지 않음
        payload.put("temperature", 100.0);
        Assertions.assertEquals(25.5, message.get("temperature"));
    }

    @Test
    @DisplayName("withEntry — 새 객체 반환")
    void test8() {
        // withEntry()가 반환한 Message와 원본은 서로 다른 객체
        Message newMessage = message.withEntry("humidity", 60);
        Assertions.assertNotSame(message, newMessage);
    }

    @Test
    @DisplayName("withEntry — 원본 불변")
    void test9() {
        // withEntry() 후 원본 Message에 새 키가 없음
        message.withEntry("humidity", 60);
        Assertions.assertFalse(message.hasKey("humidity"));
    }

    @Test
    @DisplayName("withEntry — 새 메시지에 값 존재")
    void test10() {
        // 새 Message에서 추가한 키의 값을 조회할 수 있음
        Message newMessage = message.withEntry("humidity", 60);
        Assertions.assertEquals(60, (Integer) newMessage.get("humidity"));
    }

    @Test
    @DisplayName("hasKey — 존재하는 키")
    void test11() {
        // hasKey("temperature")가 true
        Assertions.assertTrue(message.hasKey("temperature"));
    }

    @Test
    @DisplayName("hasKey — 없는 키")
    void test12() {
        // hasKey("없는키")가 false
        Assertions.assertFalse(message.hasKey("없는키"));
    }

    @Test
    @DisplayName("withoutKey — 키 제거 확인")
    void test13() {
        // 반환된 Message에서 해당 키가 없음
        Message newMessage = message.withoutKey("temperature");
        Assertions.assertFalse(newMessage.hasKey("temperature"));
    }

    @Test
    @DisplayName("withoutKey — 원본 불변")
    void test14() {
        // 원본 Message에는 해당 키가 여전히 있음
        message.withoutKey("temperature");
        Assertions.assertTrue(message.hasKey("temperature"));
    }

    @Test
    @DisplayName("toString 포맷")
    void test15() {
        // toString()이 null이 아니고, payload 내용을 포함
        String string = message.toString();
        Assertions.assertNotNull(string);
        Assertions.assertTrue(string.contains("temperature=25.5"));
    }

}