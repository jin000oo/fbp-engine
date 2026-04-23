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

import com.fbp.engine.message.Message;
import com.fbp.engine.protocol.ModbusTcpClient;
import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ModbusWriterNode extends ProtocolNode {

    private ModbusTcpClient client;

    public ModbusWriterNode(String id, Map<String, Object> config) {
        super(id, config);
        addInputPort("in");
        addOutputPort("result");
    }

    @Override
    protected void connect() throws IOException {
        String host = (String) getConfig("host");
        int port = getConfig("port") != null ? (int) getConfig("port") : 502;

        client = new ModbusTcpClient(host, port);
        client.connect();
    }

    @Override
    protected void disconnect() throws IOException {
        if (client != null) {
            client.disconnect();
        }
    }

    @Override
    public void onProcess(Message message) {
        if (!isConnected()) {
            return;
        }

        String valueField = (String) getConfig("valueField");

        if (!message.hasKey(valueField)) {
            return;
        }

        int slaveId = (int) getConfig("slaveId");
        int registerAddress = (int) getConfig("registerAddress");
        double scale = getConfig("scale") != null ? (double) getConfig("scale") : 1.0;

        try {
            double rawValue = ((Number) message.get(valueField)).doubleValue();
            int intValue = (int) Math.round(rawValue * scale);

            client.writeSingleRegister(slaveId, registerAddress, intValue);

            send("result", new Message(Map.of(
                    "status", "success",
                    "address", registerAddress,
                    "writtenValue", intValue
            )));

        } catch (Exception e) {
            log.error("[{}] 기록 실패: {}", getId(), e.getMessage());
        }
    }

}
