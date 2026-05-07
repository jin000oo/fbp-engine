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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonFlowParser implements FlowParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public FlowDefinition parse(InputStream in) throws FlowParserException {
        try {
            JsonNode root = objectMapper.readTree(in);
            if (root == null) {
                throw new FlowParserException("비어있는 JSON 문서");
            }

            if (!root.has("id") || root.get("id").asText().isEmpty()) {
                throw new FlowParserException("필수 필드 누락: id");
            }

            if (!root.has("nodes") || root.get("nodes").isEmpty()) {
                throw new FlowParserException("필수 필드 누락: nodes");
            }

            if (!root.get("nodes").isArray()) {
                throw new FlowParserException("잘못된 타입: nodes");
            }

            String flowId = root.get("id").asText();
            String name = root.has("name") ? root.get("name").asText() : "";
            String description = root.has("description") ? root.get("description").asText() : "";

            List<NodeDefinition> nodes = new ArrayList<>();
            Set<String> nodeIds = new HashSet<>();

            for (JsonNode node : root.get("nodes")) {
                String id = node.get("id").asText();
                if (!nodeIds.add(id)) {
                    throw new FlowParserException("중복 노드 id: " + id);
                }

                String type = node.get("type").asText();

                Map<String, Object> config = objectMapper.convertValue(node.get("config"),
                        new TypeReference<>() {
                        });

                nodes.add(new NodeDefinition(id, type, config));
            }

            List<ConnectionDefinition> connections = new ArrayList<>();

            if (root.has("connections") && root.get("connections").isArray()) {
                for (JsonNode connection : root.get("connections")) {
                    String from = connection.get("from").asText();
                    String to = connection.get("to").asText();

                    String[] fromParts = from.split(":");
                    String[] toParts = to.split(":");

                    if (fromParts.length != 2 || toParts.length != 2) {
                        throw new FlowParserException("잘못된 연결 형식");
                    }

                    connections.add(new ConnectionDefinition(fromParts[0], fromParts[1], toParts[0], toParts[1]));
                }
            }

            FlowDefinition flowDefinition = new FlowDefinition(flowId, name, description, nodes, connections);
            flowDefinition.validateConnections();

            return flowDefinition;

        } catch (Exception e) {
            throw new FlowParserException("JSON 파싱 실패: " + e.getMessage());
        }
    }

}
