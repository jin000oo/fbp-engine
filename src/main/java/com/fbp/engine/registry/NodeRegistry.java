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

package com.fbp.engine.registry;

import com.fbp.engine.core.Node;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NodeRegistry {

    private final Map<String, NodeFactory> registry = new ConcurrentHashMap<>();

    public void register(String typeName, NodeFactory factory) {
        if (typeName == null || typeName.isBlank()) {
            throw new NodeRegistryException("null이거나 비어있는 타입 이름");
        }

        if (factory == null) {
            throw new NodeRegistryException("빈 NodeFactory");
        }

        if (registry.containsKey(typeName)) {
            log.warn("이미 등록된 노트 타입 [{}]: 팩토리 덮어씀", typeName);
        }

        registry.put(typeName, factory);
    }

    public Node create(String typeName, String id, Map<String, Object> config) {
        if (typeName == null || typeName.isBlank()) {
            throw new NodeRegistryException("null이거나 비어있는 타입 이름");
        }

        NodeFactory factory = registry.get(typeName);
        if (factory == null) {
            throw new NodeRegistryException("등록되지 않은 노드 타입");
        }

        return factory.create(id, config != null ? config : Collections.emptyMap());
    }

    public Set<String> getRegisteredTypes() {
        return Collections.unmodifiableSet(registry.keySet());
    }

    public boolean isRegistered(String typeName) {
        return typeName != null && registry.containsKey(typeName);
    }

}
