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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FileWriterNodeTest {

    String TEST_FILE_PATH = "test.log";

    @AfterEach
    void tearDown() {
        new File(TEST_FILE_PATH).delete();
    }

    @Test
    @DisplayName("파일 생성")
    void test1() {
        // initialize() 후 지정 경로에 파일이 생성됨
        FileWriterNode writer = new FileWriterNode("writer", TEST_FILE_PATH);
        writer.initialize();

        writer.shutdown();

        File file = new File(TEST_FILE_PATH);
        Assertions.assertTrue(file.exists());
    }

    @Test
    @DisplayName("내용 기록")
    void test2() throws IOException {
        // 메시지 3개를 보낸 후 shutdown(), 파일에 3줄이 기록되어 있음
        FileWriterNode writer = new FileWriterNode("writer", TEST_FILE_PATH);
        writer.initialize();

        writer.process(new Message(Map.of("data", "line1")));
        writer.process(new Message(Map.of("data", "line2")));
        writer.process(new Message(Map.of("data", "line3")));

        writer.shutdown();

        File file = new File(TEST_FILE_PATH);
        Assertions.assertEquals(3, Files.lines(file.toPath()).count());
    }

    @Test
    @DisplayName("shutdown 후 파일 닫힘")
    void test3() throws IOException {
        // shutdown() 후 추가 메시지를 보내도 기록되지 않거나 예외 발생
        FileWriterNode writer = new FileWriterNode("writer", TEST_FILE_PATH);
        writer.initialize();

        writer.shutdown();

        File file = new File(TEST_FILE_PATH);
        Assertions.assertEquals(0, Files.lines(file.toPath()).count());
    }

}