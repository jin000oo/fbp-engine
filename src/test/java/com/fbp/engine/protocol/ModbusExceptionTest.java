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

package com.fbp.engine.protocol;

import java.lang.reflect.Field;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ModbusExceptionTest {

    @Test
    @DisplayName("getMessage 포맷")
    void test1() {
        // 생성 시 지정한 functionCode와 exceptionCode가 메시지에 포함
        ModbusException exception = new ModbusException(0x03, 0x02);
        String message = exception.getMessage();

        Assertions.assertAll(
                () -> Assertions.assertTrue(message.contains("FC: 0x03")),
                () -> Assertions.assertTrue(message.contains("Exception: 0x02")),
                () -> Assertions.assertTrue(message.contains("Illegal Data Address"))
        );
    }

    @Test
    @DisplayName("getExceptionCode")
    void test2() {
        // 생성 시 지정한 exceptionCode가 반환됨
        ModbusException exception = new ModbusException(0x06, 0x04);

        Assertions.assertEquals(0x04, exception.getExceptionCode());
    }

    @Test
    @DisplayName("상수 값")
    void test3() throws NoSuchFieldException, IllegalAccessException {
        // ILLEGAL_FUNCTION이 0x01, ILLEGAL_DATA_ADDRESS가 0x02 등
        Field field = ModbusException.class.getDeclaredField("ILLEGAL_FUNCTION");
        field.setAccessible(true);

        Assertions.assertEquals(0x01, field.get(null));
    }

}