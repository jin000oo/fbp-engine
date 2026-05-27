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

package com.fbp.engine.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Flow {

    public enum State {
        INITIALIZED, RUNNING, STOPPED
    }

    private final String id;

    private final Map<String, AbstractNode> nodes = new LinkedHashMap<>();

    private final List<Connection> connections = new CopyOnWriteArrayList<>();

    private final Map<String, List<String>> adjacencyList = new HashMap<>();

    @Getter
    @Setter
    private State state = State.INITIALIZED;

    public Flow(String id) {
        this.id = id;
    }

    public Flow addNode(AbstractNode node) {
        nodes.put(node.getId(), node);
        adjacencyList.putIfAbsent(node.getId(), new ArrayList<>());

        return this;
    }

    public Flow connect(String sourceNodeId, String sourcePort, String targetNodeId, String targetPort) {
        return connect(sourceNodeId, sourcePort, targetNodeId, targetPort, new DropOldestStrategy());
    }

    public Flow connect(String sourceNodeId, String sourcePort, String targetNodeId, String targetPort, BackpressureStrategy strategy) {
        AbstractNode source = nodes.get(sourceNodeId);
        if (source == null) {
            throw new IllegalArgumentException("존재하지 않는 소스 노드");
        }

        AbstractNode target = nodes.get(targetNodeId);
        if (target == null) {
            throw new IllegalArgumentException("존재하지 않는 타겟 노드");
        }

        OutputPort out = source.getOutputPort(sourcePort);
        if (out == null) {
            throw new IllegalArgumentException("존재하지 않는 소스 포트");
        }

        InputPort in = target.getInputPort(targetPort);
        if (in == null) {
            throw new IllegalArgumentException("존재하지 않는 타겟 포트");
        }

        String connectionId = String.format("%s:%s->%s:%s", sourceNodeId, sourcePort, targetNodeId, targetPort);
        Connection connection = new BackpressureConnection(connectionId, 100, strategy);
        connection.setTarget(in);
        out.connect(connection);

        connections.add(connection);

        adjacencyList.get(sourceNodeId).add(targetNodeId);

        return this;
    }

    public void initialize() {
        for (AbstractNode node : nodes.values()) {
            node.initialize();
        }
    }

    public void shutdown() {
        for (AbstractNode node : nodes.values()) {
            node.shutdown();
        }
    }

    public List<String> validate() {
        List<String> errors = new ArrayList<>();

        if (nodes.isEmpty()) {
            errors.add("Flow에 등록되니 노드가 없음");
            return errors;
        }

        if (hasCycle()) {
            errors.add("순환 참조 발견");
        }

        return errors;
    }

    private enum Visit {
        UNVISITED, VISITING, VISITED
    }

    private boolean hasCycle() {
        Map<String, Visit> states = new HashMap<>();
        for (String nodeId : nodes.keySet()) {
            states.put(nodeId, Visit.UNVISITED);
        }

        for (String nodeId : nodes.keySet()) {
            if (states.get(nodeId) == Visit.UNVISITED) {
                if (dfs(nodeId, states)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean dfs(String nodeId, Map<String, Visit> states) {
        states.put(nodeId, Visit.VISITING);

        for (String neighbor : adjacencyList.getOrDefault(nodeId, Collections.emptyList())) {
            if (states.get(neighbor) == Visit.UNVISITED) {
                if (dfs(neighbor, states)) {
                    return true;
                }
            } else if (states.get(neighbor) == Visit.VISITING) {
                return true;
            }
        }

        states.put(nodeId, Visit.VISITED);

        return false;
    }

}
