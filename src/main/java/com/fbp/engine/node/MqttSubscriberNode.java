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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fbp.engine.core.ProtocolNode;
import com.fbp.engine.message.Message;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

@Slf4j
public class MqttSubscriberNode extends ProtocolNode {

    private MqttClient client;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public MqttSubscriberNode(String id, Map<String, Object> config) {
        super(id, config);
        addOutputPort("out");
    }

    @Override
    protected void connect() throws IOException {
        String brokerUrl = (String) getConfig("brokerUrl");
        String clientId = (String) getConfig("clientId");
        String topic = (String) getConfig("topic");
        int qos = getConfig("qos") != null ? (int) getConfig("qos") : 1;

        try {
            client = new MqttClient(brokerUrl, clientId);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);

            client.connect(options);

            client.subscribe(topic, qos, (receiveTopic, msg) -> {
                String payload = new String(msg.getPayload());

                Map<String, Object> data = new HashMap<>();

                try {
                    data = objectMapper.readValue(payload, new TypeReference<>() {
                    });

                } catch (Exception e) {
                    data.put("rawPayload", payload);

                    log.error("[{}] JSON 파싱 중 오류 발생: {}", getId(), e.getMessage());
                }

                data.put("topic", receiveTopic);
                data.put("mqttTimestamp", System.currentTimeMillis());

                send("out", new Message(data));
            });

        } catch (Exception e) {
            log.error("[{}] 연결 준비 중 오류 발생: {}", getId(), e.getMessage());
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

}
