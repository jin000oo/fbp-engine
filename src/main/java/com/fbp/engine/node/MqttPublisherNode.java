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

package com.fbp.engine.node;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fbp.engine.core.ProtocolNode;
import com.fbp.engine.message.Message;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

@Slf4j
public class MqttPublisherNode extends ProtocolNode {

    private MqttClient client;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public MqttPublisherNode(String id, Map<String, Object> config) {
        super(id, config);
        addInputPort("in");
    }

    @Override
    protected void connect() throws IOException {
        String brokerUrl = (String) getConfig("brokerUrl");
        String clientId = (String) getConfig("clientId");

        try {
            client = new MqttClient(brokerUrl, clientId);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);

            client.connect(options);

            log.debug("[{}] Broker 연결 준비 완료", getId());

        } catch (Exception e) {
            log.error("[{}] Broker 연결 준비 중 오류 발생: {}", getId(), e.getMessage());
        }
    }

    @Override
    protected void disconnect() throws IOException {
        if (client != null && client.isConnected()) {
            try {
                client.disconnect();
                client.close();

            } catch (Exception e) {
                log.error("[{}] 연결 해제 중 오류 발생: {}", getId(), e.getMessage());
            }
        }
    }

    @Override
    public void onProcess(Message message) {
        if (!isConnected()) {
            return;
        }

        try {
            Map<String, Object> payloadMap = message.getPayload();

            String jsonPayload = objectMapper.writeValueAsString(payloadMap);

            String configTopic = (String) getConfig("topic");
            String topic =
                    (configTopic != null && !configTopic.isEmpty()) ? configTopic : (String) message.get("topic");

            if (topic == null || topic.isEmpty()) {
                throw new IllegalArgumentException();
            }

            int qos = getConfig("qos") != null ? (int) getConfig("qos") : 1;
            boolean retained = getConfig("retained") != null ? (boolean) getConfig("retained") : false;

            MqttMessage mqttMessage = new MqttMessage(jsonPayload.getBytes(StandardCharsets.UTF_8));
            mqttMessage.setQos(qos);
            mqttMessage.setRetained(retained);

            client.publish(topic, mqttMessage);

            log.debug("[{}] 발행 성공: {} -> {}", getId(), topic, jsonPayload);

        } catch (Exception e) {
            log.debug("[{}] 발행 실패: {}", getId(), e.getMessage());
        }
    }

}
