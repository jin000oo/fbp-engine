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

import com.fbp.engine.core.Connection;
import com.fbp.engine.message.Message;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DelayNodeTest {

    @Test
    @DisplayName("지연 후 전달")
    void test1() {
        // 메시지가 지정된 지연 시간 이후에 OutputPort로 전달됨 (시간 측정)
        long delayMs = 300;

        DelayNode delay = new DelayNode("delay-1", delayMs);

        Connection connection = new Connection("out");
        delay.getOutputPort("out").connect(connection);

        Message message = new Message(Map.of("test", 1));

        long startTime = System.currentTimeMillis();
        delay.process(message);
        long endTime = System.currentTimeMillis();

        Message result = connection.poll();
        Assertions.assertNotNull(result);
        Assertions.assertTrue((endTime - startTime) >= delayMs);
    }

    @Test
    @DisplayName("메시지 내용 보존")
    void test2() {
        // 지연 후에도 메시지 내용이 동일함
        long delayMs = 300;

        DelayNode delay = new DelayNode("delay-1", delayMs);

        Connection connection = new Connection("out");
        delay.getOutputPort("out").connect(connection);

        Message message = new Message(Map.of("test", 1));

        delay.process(message);

        Message result = connection.poll();
        Assertions.assertEquals(1, (Integer) result.get("test"));
    }

}