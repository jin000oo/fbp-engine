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

package com.fbp.engine.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PluginClassLoaderTest {

    @Test
    @DisplayName("JAR 로드")
    void test1() throws IOException {
        // 외부 JAR의 클래스를 정상적으로 로드
        File tempFile = File.createTempFile("dummy", ".jar");
        tempFile.deleteOnExit();

        URL[] urls = new URL[] {tempFile.toURI().toURL()};

        try (PluginClassLoader loader = new PluginClassLoader(urls, getClass().getClassLoader())) {
            Assertions.assertAll(
                    () -> Assertions.assertNotNull(loader.getURLs()),
                    () -> Assertions.assertEquals(1, loader.getURLs().length)
            );
        }
    }

    @Test
    @DisplayName("클래스 격리")
    void test2() throws IOException {
        // 플러그인 클래스가 엔진의 내부 클래스에 영향을 주지 않음
        URL[] urls = new URL[0];

        try (PluginClassLoader loader = new PluginClassLoader(urls, getClass().getClassLoader())) {
            Assertions.assertNotEquals(ClassLoader.getSystemClassLoader(), loader);
        }

    }

    @Test
    @DisplayName("리소스 해제")
    void test3() {
        // close() 호출 시 JAR 파일 핸들 해제
        URL[] urls = new URL[0];

        PluginClassLoader loader = new PluginClassLoader(urls, getClass().getClassLoader());

        Assertions.assertDoesNotThrow(loader::close);
    }

    @Test
    @DisplayName("존재하지 않는 JAR")
    void test4() {
        // 없는 경로의 JAR → 예외
        Assertions.assertThrows(Exception.class, () -> {
            URL url = new URL("file://non-exist-path.jar");

            PluginClassLoader loader = new PluginClassLoader(new URL[] {url}, getClass().getClassLoader());
            loader.loadClass("com.unknown");
        });
    }

}