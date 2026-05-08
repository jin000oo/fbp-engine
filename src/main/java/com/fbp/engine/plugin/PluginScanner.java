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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PluginScanner {

    public static List<URL> scan(String directoryPath) {
        List<URL> jarUrls = new ArrayList<>();

        File directory = new File(directoryPath);

        if (!directory.exists() || !directory.isDirectory()) {
            log.warn("찾을 수 없는 플러그인 디렉토리");

            return jarUrls;
        }

        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));

        if (files != null) {
            for (File file : files) {
                try {
                    jarUrls.add(file.toURI().toURL());

                } catch (MalformedURLException e) {
                    log.error("JAR URL 변환 실패: {}", file.getName(), e);
                }
            }
        }

        return jarUrls;
    }

}
