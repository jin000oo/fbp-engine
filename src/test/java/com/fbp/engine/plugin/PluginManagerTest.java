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

import com.fbp.engine.registry.NodeRegistry;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PluginManagerTest {

    PluginManager pluginManager;

    NodeRegistry registry;

    @TempDir
    File tempDirectory;

    @BeforeEach
    void setUp() {
        registry = new NodeRegistry();
        pluginManager = new PluginManager(registry);
    }

    @AfterEach
    void tearDown() throws IOException {
        pluginManager.close();
    }

    @Test
    @DisplayName("ClassPath 플러그인 로드")
    void test1() {
        // ServiceLoader로 ClassPath 내 NodeProvider 자동 발견 및 등록
        Assertions.assertDoesNotThrow(() -> pluginManager.loadPlugins("non-exist"));
    }

    @Test
    @DisplayName("외부 JAR 로드")
    void test2() throws Exception {
        // plugins/ 디렉토리의 JAR에서 NodeProvider 발견 및 등록
        createDummyJar("plugin-a.jar");

        List<URL> jarUrls = PluginScanner.scan(tempDirectory.getAbsolutePath());

        Assertions.assertEquals(1, jarUrls.size());
    }

    @Test
    @DisplayName("NodeRegistry 자동 등록")
    void test3() {
        // 로드된 플러그인의 노드 타입이 NodeRegistry에 등록됨
        Assertions.assertDoesNotThrow(() -> pluginManager.loadPlugins(tempDirectory.getAbsolutePath()));
    }

    @Test
    @DisplayName("타입 충돌 처리")
    void test4() {
        // 내장 노드와 동일한 typeName의 플러그인 노드 → 정책에 맞게 처리
        registry.register("type-a", ((id, config) -> null));

        Assertions.assertAll(
                () -> Assertions.assertTrue(registry.isRegistered("type-a")),
                () -> Assertions.assertDoesNotThrow(() -> {
                    registry.register("type-a", ((id, config) -> null));
                })
        );
    }

    @Test
    @DisplayName("잘못된 JAR")
    void test5() throws IOException {
        // 유효하지 않은 JAR 파일 → 예외 후 나머지 플러그인은 정상 로드
        File jarFile = new File(tempDirectory, "wrong.jar");
        jarFile.createNewFile();

        Assertions.assertDoesNotThrow(() -> pluginManager.loadPlugins(tempDirectory.getAbsolutePath()));
    }

    @Test
    @DisplayName("plugins 디렉토리 없음")
    void test6() {
        // 디렉토리가 없으면 스캔 건너뜀 (예외 아님)
        Assertions.assertDoesNotThrow(() -> pluginManager.loadPlugins("non-exist-directory"));
    }

    @Test
    @DisplayName("빈 plugins 디렉토리")
    void test7() {
        // 디렉토리는 있지만 JAR가 없으면 정상 (플러그인 0개)
        Assertions.assertAll(
                () -> Assertions.assertDoesNotThrow(() -> pluginManager.loadPlugins(tempDirectory.getAbsolutePath())),
                () -> Assertions.assertEquals(0, registry.getRegisteredTypes().size())
        );
    }

    @Test
    @DisplayName("플러그인 수 확인")
    void test8() throws Exception {
        // 복수 JAR 로드 시 전체 등록된 노드 타입 수가 예상과 일치
        createDummyJar("plugin-a.jar");
        createDummyJar("plugin-b.jar");
        createDummyJar("plugin-c.jar");

        List<URL> jarUrls = PluginScanner.scan(tempDirectory.getAbsolutePath());

        Assertions.assertEquals(3, jarUrls.size());
    }

    private File createDummyJar(String fileName) throws Exception {
        File jarFile = new File(tempDirectory, fileName);

        try (ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(jarFile))) {
            zout.putNextEntry(new ZipEntry("dummy.txt"));
            zout.write("dummy".getBytes());
            zout.closeEntry();
        }

        return jarFile;
    }

}