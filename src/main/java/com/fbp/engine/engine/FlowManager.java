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

package com.fbp.engine.engine;

import com.fbp.engine.core.AbstractNode;
import com.fbp.engine.core.Flow;
import com.fbp.engine.core.FlowEngine;
import com.fbp.engine.core.Node;
import com.fbp.engine.parser.ConnectionDefinition;
import com.fbp.engine.parser.FlowDefinition;
import com.fbp.engine.parser.NodeDefinition;
import com.fbp.engine.registry.NodeRegistry;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FlowManager {

    private final FlowEngine engine;

    private final NodeRegistry registry;

    private final Map<String, Flow> deployedFlows = new ConcurrentHashMap<>();

    public FlowManager(FlowEngine engine, NodeRegistry registry) {
        this.engine = engine;
        this.registry = registry;
    }

    public void deploy(FlowDefinition flowDefinition) {
        if (deployedFlows.containsKey(flowDefinition.getId())) {
            throw new IllegalArgumentException("이미 존재하는 Flow ID");
        }

        Flow flow = new Flow(flowDefinition.getId());

        for (NodeDefinition node : flowDefinition.getNodes()) {
            if (!registry.isRegistered(node.type())) {
                throw new IllegalArgumentException("등록되지 않은 노드 타입");
            }

            Node newNode = registry.create(node.type(), node.id(), node.config());

            flow.addNode((AbstractNode) newNode);
        }

        for (ConnectionDefinition connection : flowDefinition.getConnections()) {
            flow.connect(connection.fromNode(), connection.fromPort(), connection.toNode(), connection.toPort());
        }

        engine.register(flow);
        engine.startFlow(flow.getId());

        deployedFlows.put(flow.getId(), flow);
    }

    public Collection<Flow> list() {
        return deployedFlows.values();
    }

    public String getStatus(String flowId) {
        Flow flow = deployedFlows.get(flowId);
        if (flow == null) {
            throw new IllegalArgumentException("존재하지 않는 Flow ID");
        }

        return flow.getState().name();
    }

    public void stop(String flowId) {
        if (!deployedFlows.containsKey(flowId)) {
            throw new IllegalArgumentException("존재하지 않는 Flow ID");
        }

        engine.stopFlow(flowId);
    }

    public void restart(String flowId) {
        if (!deployedFlows.containsKey(flowId)) {
            throw new IllegalArgumentException("존재하지 않는 Flow ID");
        }

        engine.startFlow(flowId);
    }

    public void remove(String flowId) {
        if (!deployedFlows.containsKey(flowId)) {
            throw new IllegalArgumentException("존재하지 않는 Flow ID");
        }

        if ("RUNNING".equals(getStatus(flowId))) {
            stop(flowId);
        }

        engine.removeFlow(flowId);
        deployedFlows.remove(flowId);
    }

}
