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
import java.util.HashMap;
import java.util.Map;

public class ModbusReaderNode extends ProtocolNode {

    private ModbusTcpClient client;

    public ModbusReaderNode(String id, Map<String, Object> config) {
        super(id, config);
        addInputPort("trigger");
        addOutputPort("out");
        addOutputPort("error");
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

        int slaveId = (int) getConfig("slaveId");
        int startAddress = (int) getConfig("startAddress");
        int count = (int) getConfig("count");
        try {
            int[] registers = client.readHoldingRegisters(slaveId, startAddress, count);

            Map<String, Object> data = new HashMap<>();
            data.put("slaveId", slaveId);

            Map<String, Object> registerMapping = (Map<String, Object>) getConfig("registerMapping");

            if (registerMapping != null) {
                for (int i = 0; i < count; i++) {
                    String address = String.valueOf(startAddress + i);

                    if (registerMapping.containsKey(address)) {
                        Map<String, Object> mapInfo = (Map<String, Object>) registerMapping.get(address);

                        String name = (String) mapInfo.get("name");
                        double scale = mapInfo.get("scale") != null ? (double) mapInfo.get("scale") : 1.0;

                        data.put(name, registers[i] * scale);
                    }
                }
            } else {
                data.put("registers", registers);
            }

            send("out", new Message(data));

        } catch (Exception e) {
            send("error", new Message(Map.of("error", e.getMessage())));
        }
    }

}
