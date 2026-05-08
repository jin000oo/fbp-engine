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

import com.fbp.engine.message.Message;
import com.fbp.engine.metrics.MetricsCollector;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class AbstractNode implements Node {

    private final String id;

    private final Map<String, InputPort> inputPorts = new HashMap<>();

    private final Map<String, OutputPort> outputPorts = new HashMap<>();

    protected void addInputPort(String name) {
        inputPorts.put(name, new DefaultInputPort(name, this));
    }

    protected void addOutputPort(String name) {
        outputPorts.put(name, new DefaultOutputPort(name));
    }

    public InputPort getInputPort(String name) {
        return inputPorts.get(name);
    }

    public OutputPort getOutputPort(String name) {
        return outputPorts.get(name);
    }

    protected void send(String portName, Message message) {
        OutputPort outputPort = outputPorts.get(portName);

        if (outputPort != null) {
            outputPort.send(message);
        }
    }

    public abstract void onProcess(Message message);

    @Override
    public void process(Message message) {
        long startNs = System.nanoTime();
        boolean success = true;

        System.out.printf("[%s] processing message...%n", id);

        try {
            onProcess(message);

        } catch (Exception e) {
            success = false;

        } finally {
            long durationNs = System.nanoTime() - startNs;

            MetricsCollector.getInstance().recordProcessing(getId(), durationNs, success);
        }
    }

}
