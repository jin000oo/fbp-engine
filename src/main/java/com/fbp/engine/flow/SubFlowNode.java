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

package com.fbp.engine.flow;

import com.fbp.engine.core.AbstractNode;
import com.fbp.engine.core.Connection;
import com.fbp.engine.core.Flow;
import com.fbp.engine.message.Message;

public class SubFlowNode extends AbstractNode {

    private final Flow internalFlow;

    private final String entryNodeId;

    private final String exitNodeId;

    private Thread bridgeThread;

    public SubFlowNode(String id, Flow internalFlow, String entryNodeId, String exitNodeId) {
        super(id);
        this.internalFlow = internalFlow;
        this.entryNodeId = entryNodeId;
        this.exitNodeId = exitNodeId;
        addInputPort("in");
        addOutputPort("out");
    }

    @Override
    public void onProcess(Message message) {
        AbstractNode entryNode = internalFlow.getNodes().get(entryNodeId);

        if (entryNode != null) {
            entryNode.process(message);
        }
    }

    @Override
    public void initialize() {
        internalFlow.initialize();

        AbstractNode exitNode = internalFlow.getNodes().get(exitNodeId);

        if (exitNode != null) {
            Connection connection = new Connection("bridge");

            exitNode.getOutputPort("out").connect(connection);

            bridgeThread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    Message message = connection.poll();

                    if (message != null) {
                        send("out", message);
                    }
                }
            });

            bridgeThread.setDaemon(true);
            bridgeThread.start();
        }
    }

    @Override
    public void shutdown() {
        internalFlow.shutdown();

        if (bridgeThread != null && bridgeThread.isAlive()) {
            bridgeThread.interrupt();
        }
    }

}
