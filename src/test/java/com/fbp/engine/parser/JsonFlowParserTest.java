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

import java.io.ByteArrayInputStream;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JsonFlowParserTest {

    JsonFlowParser parser = new JsonFlowParser();

    String json = """
            {
              "id": "temperature-monitoring",
              "name": "온도 모니터링 플로우",
              "description": "MQTT 센서 데이터를 수신하여 임계값 초과 시 알림",
              "nodes": [
                {
                  "id": "sensor",
                  "type": "MqttSubscriber",
                  "config": {
                    "broker": "tcp://localhost:1883",
                    "topic": "sensor/temp",
                    "qos": 1
                  }
                },
                {
                  "id": "rule",
                  "type": "ThresholdFilter",
                  "config": {
                    "field": "value",
                    "operator": ">",
                    "threshold": 30
                  }
                },
                {
                  "id": "alert",
                  "type": "MqttPublisher",
                  "config": {
                    "broker": "tcp://localhost:1883",
                    "topic": "alert/temp"
                  }
                }
              ],
              "connections": [
                { "from": "sensor:out", "to": "rule:in" },
                { "from": "rule:out", "to": "alert:in" }
              ]
            }
            """;

    @Test
    @DisplayName("정상 파싱")
    void test1() {
        // 유효한 JSON → FlowDefinition 정상 변환
        Assertions.assertDoesNotThrow(() -> parser.parse(new ByteArrayInputStream(json.getBytes())));
    }

    @Test
    @DisplayName("노드 목록")
    void test2() {
        // 파싱된 FlowDefinition의 노드 수와 각 노드의 id, type, config 일치
        FlowDefinition flowDefinition = parser.parse(new ByteArrayInputStream(json.getBytes()));

        Map<String, Object> sensorConfig = Map.of(
                "broker", "tcp://localhost:1883",
                "topic", "sensor/temp",
                "qos", 1);
        Map<String, Object> ruleConfig = Map.of(
                "field", "value",
                "operator", ">",
                "threshold", 30);
        Map<String, Object> alertConfig = Map.of(
                "broker", "tcp://localhost:1883",
                "topic", "alert/temp");

        Assertions.assertAll(
                () -> Assertions.assertEquals(3, flowDefinition.getNodes().size()),
                () -> Assertions.assertEquals("sensor", flowDefinition.getNode("sensor").id()),
                () -> Assertions.assertEquals("rule", flowDefinition.getNode("rule").id()),
                () -> Assertions.assertEquals("alert", flowDefinition.getNode("alert").id()),
                () -> Assertions.assertEquals("MqttSubscriber", flowDefinition.getNode("sensor").type()),
                () -> Assertions.assertEquals("ThresholdFilter", flowDefinition.getNode("rule").type()),
                () -> Assertions.assertEquals("MqttPublisher", flowDefinition.getNode("alert").type()),
                () -> Assertions.assertEquals(sensorConfig, flowDefinition.getNode("sensor").config()),
                () -> Assertions.assertEquals(ruleConfig, flowDefinition.getNode("rule").config()),
                () -> Assertions.assertEquals(alertConfig, flowDefinition.getNode("alert").config())
        );
    }

    @Test
    @DisplayName("연결 목록")
    void test3() {
        // 파싱된 연결의 from/to 정보가 JSON과 일치
        FlowDefinition flowDefinition = parser.parse(new ByteArrayInputStream(json.getBytes()));

        Assertions.assertAll(
                () -> Assertions.assertEquals(2, flowDefinition.getConnections().size()),
                () -> Assertions.assertEquals("sensor", flowDefinition.getConnections().getFirst().fromNode()),
                () -> Assertions.assertEquals("out", flowDefinition.getConnections().getFirst().fromPort()),
                () -> Assertions.assertEquals("rule", flowDefinition.getConnections().getFirst().toNode()),
                () -> Assertions.assertEquals("in", flowDefinition.getConnections().getFirst().toPort()),
                () -> Assertions.assertEquals("rule", flowDefinition.getConnections().getLast().fromNode()),
                () -> Assertions.assertEquals("out", flowDefinition.getConnections().getLast().fromPort()),
                () -> Assertions.assertEquals("alert", flowDefinition.getConnections().getLast().toNode()),
                () -> Assertions.assertEquals("in", flowDefinition.getConnections().getLast().toPort())
        );
    }

    @Test
    @DisplayName("필수 필드 누락 — id")
    void test4() {
        // 플로우 id가 없으면 FlowParserException
        String exampleJson = """
                {
                    "nodes": []
                }
                """;

        Assertions.assertThrows(FlowParserException.class, () -> {
            parser.parse(new ByteArrayInputStream(exampleJson.getBytes()));
        });
    }

    @Test
    @DisplayName("필수 필드 누락 — nodes")
    void test5() {
        // nodes 배열이 없으면 예외
        String exampleJson = """
                {
                    "id": "flow"
                }
                """;

        Assertions.assertThrows(FlowParserException.class, () -> {
            parser.parse(new ByteArrayInputStream(exampleJson.getBytes()));
        });
    }

    @Test
    @DisplayName("빈 노드 목록")
    void test6() {
        // nodes가 빈 배열이면 예외 또는 경고
        String exampleJson = """
                {
                    "id": "flow",
                    "nodes": []
                }
                """;

        Assertions.assertThrows(FlowParserException.class, () -> {
            parser.parse(new ByteArrayInputStream(exampleJson.getBytes()));
        });
    }

    @Test
    @DisplayName("잘못된 JSON 형식")
    void test7() {
        // 문법 오류 JSON → 적절한 예외
        String exampleJson = """
                { wrong
                """;

        Assertions.assertThrows(FlowParserException.class, () -> {
            parser.parse(new ByteArrayInputStream(exampleJson.getBytes()));
        });
    }

    @Test
    @DisplayName("연결의 포트 파싱")
    void test8() {
        // "sensor:out" → sourceNode="sensor", sourcePort="out"
        FlowDefinition flowDefinition = parser.parse(new ByteArrayInputStream(json.getBytes()));

        ConnectionDefinition connectionDefinition = flowDefinition.getConnections().getFirst();

        Assertions.assertAll(
                () -> Assertions.assertEquals("sensor", connectionDefinition.fromNode()),
                () -> Assertions.assertEquals("out", connectionDefinition.fromPort())
        );
    }

    @Test
    @DisplayName("잘못된 연결 형식")
    void test9() {
        // "sensor" (포트 없음) → 예외
        String exampleJson = """
                 {
                    "id": "flow",
                    "nodes": [
                        {
                            "id": "sensor",
                            "type": "type-a",
                            "config": {}
                        }
                    ],
                    "connections": [
                        {
                            "from": "sensor",
                            "to": "sensor:in"
                        }
                    ]
                 }
                """;

        Assertions.assertThrows(FlowParserException.class, () -> {
            parser.parse(new ByteArrayInputStream(exampleJson.getBytes()));
        });
    }

    @Test
    @DisplayName("존재하지 않는 노드 참조")
    void test10() {
        // 연결에서 정의되지 않은 노드 id 참조 시 예외
        String exampleJson = """
                 {
                    "id": "flow",
                    "nodes": [
                        {
                            "id": "sensor",
                            "type": "type-a",
                            "config": {}
                        }
                    ],
                    "connections": [
                        {
                            "from": "sensor:out",
                            "to": "rule:in"
                        }
                    ]
                 }
                """;

        Assertions.assertThrows(FlowParserException.class, () -> {
            parser.parse(new ByteArrayInputStream(exampleJson.getBytes()));
        });
    }

    @Test
    @DisplayName("중복 노드 id")
    void test11() {
        // 같은 id의 노드가 두 개 이상이면 예외
        String exampleJson = """
                 {
                    "id": "flow",
                    "nodes": [
                        {
                            "id": "sensor",
                            "type": "type-a",
                            "config": {}
                        },
                        {
                            "id": "sensor",
                            "type": "type-b",
                            "config": {}
                        }
                    ]
                 }
                """;

        Assertions.assertThrows(FlowParserException.class, () -> {
            parser.parse(new ByteArrayInputStream(exampleJson.getBytes()));
        });
    }

    @Test
    @DisplayName("config 타입 보존")
    void test12() {
        // config의 숫자, 문자열, boolean 값이 타입을 유지한 채 파싱됨
        String exampleJson = """
                 {
                    "id": "flow",
                    "nodes": [
                        {
                            "id": "sensor",
                            "type": "type-a",
                            "config": {
                                "count": 10,
                                "name": "test",
                                "active": true
                            }
                        },
                        {
                            "id": "rule",
                            "type": "type-b",
                            "config": {}
                        }
                    ],
                    "connections": [
                        {
                            "from": "sensor:out",
                            "to": "rule:in"
                        }
                    ]
                 }
                """;

        FlowDefinition flowDefinition = parser.parse(new ByteArrayInputStream(exampleJson.getBytes()));

        Assertions.assertAll(
                () -> Assertions.assertEquals(10, flowDefinition.getNode("sensor").config().get("count")),
                () -> Assertions.assertEquals("test", flowDefinition.getNode("sensor").config().get("name")),
                () -> Assertions.assertEquals(true, flowDefinition.getNode("sensor").config().get("active"))
        );
    }

}