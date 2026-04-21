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

package com.fbp.engine.node;

import com.fbp.engine.message.Message;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AlertNodeTest {

    @Test
    @DisplayName("정상 처리")
    void test1() {
        // sensorId와 temperature가 포함된 메시지 수신 시 예외 없이 동작
        AlertNode alert = new AlertNode("alert");

        Message message = new Message(Map.of("sensorId", "sensor", "temperature", 30));
        Assertions.assertDoesNotThrow(() -> alert.process(message));
    }

    @Test
    @DisplayName("키 누락 시 처리")
    void test2() {
        // "temperature" 키가 없는 메시지 수신 시 예외가 발생하지 않음
        AlertNode alert = new AlertNode("alert");

        Message message = new Message(Map.of("sensorId", "sensor"));
        Assertions.assertDoesNotThrow(() -> alert.process(message));
    }

}