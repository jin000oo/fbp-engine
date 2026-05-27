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
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ServiceLoader;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PluginManager {

    private final NodeRegistry registry;

    private PluginClassLoader pluginClassLoader;

    public PluginManager(NodeRegistry registry) {
        this.registry = registry;
        this.pluginClassLoader = new PluginClassLoader(new URL[0], this.getClass().getClassLoader());
    }

    public void loadPlugins(String pluginDirectory) {
        ServiceLoader<NodeProvider> defaultLoader = ServiceLoader.load(NodeProvider.class);
        registerProviders(defaultLoader);

        List<URL> jarUrls = PluginScanner.scan(pluginDirectory);

        for (URL jarUrl : jarUrls) {
            pluginClassLoader.addJar(jarUrl);
        }

        if (!jarUrls.isEmpty()) {
            ServiceLoader<NodeProvider> externalLoader = ServiceLoader.load(NodeProvider.class, pluginClassLoader);
            registerProviders(externalLoader);
        }
    }

    public void close() throws IOException {
        if (pluginClassLoader != null) {
            pluginClassLoader.close();
        }
    }

    private void registerProviders(ServiceLoader<NodeProvider> loader) {
        int count = 0;

        for (NodeProvider provider : loader) {
            try {
                for (NodeDescriptor descriptor : provider.getNodeDescriptors()) {
                    if (descriptor.typeName() == null || descriptor.factory() == null) {
                        throw new PluginException("필수 값(typeName or factory) 누락");
                    }

                    registry.register(descriptor.typeName(), descriptor.factory());

                    count++;
                }

            } catch (Exception e) {
                log.error("NodeProvider 처리 중 예외: {}", e.getMessage());
            }
        }

        log.info("총 {} 개의 플러그인 노드 등록", count);
    }

}
