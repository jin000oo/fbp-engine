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

package com.fbp.engine.parser;

import java.util.Collections;
import java.util.List;
import lombok.Getter;

public class FlowDefinition {

    @Getter
    private final String id;

    private final String name;

    private final String description;

    @Getter
    private final List<NodeDefinition> nodes;

    @Getter
    private final List<ConnectionDefinition> connections;

    public FlowDefinition(String id, String name, String description,
                          List<NodeDefinition> nodes, List<ConnectionDefinition> connections) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.nodes = Collections.unmodifiableList(nodes);
        this.connections = Collections.unmodifiableList(connections);
    }

    public NodeDefinition getNode(String nodeId) {
        return nodes.stream()
                .filter(n -> n.id().equals(nodeId))
                .findFirst()
                .orElse(null);
    }

    public void validateConnections() {
        for (ConnectionDefinition connection : connections) {
            if (getNode(connection.fromNode()) == null) {
                throw new FlowParserException("존재하지 않는 출발 노드");
            }

            if (getNode(connection.toNode()) == null) {
                throw new FlowParserException("존재하지 않는 도착 노드");
            }
        }
    }

}
