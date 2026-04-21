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

import com.fbp.engine.core.AbstractNode;
import com.fbp.engine.message.Message;
import java.util.HashMap;
import java.util.Map;

public class MergeNode extends AbstractNode {

    private Message pending1;

    private Message pending2;

    public MergeNode(String id) {
        super(id);
        addInputPort("in-1");
        addInputPort("in-2");
        addOutputPort("out");
    }

    @Override
    public synchronized void onProcess(Message message) {
        String source = "";

        if (message.hasKey("__source__")) {
            source = message.get("__source__");
        }

        if ("in-1".equals(source)) {
            pending1 = message;
        } else if ("in-2".equals(source)) {
            pending2 = message;
        }

        if (pending1 != null && pending2 != null) {
            Map<String, Object> mergedData = new HashMap<>();

            mergeKeys(pending1, mergedData);
            mergeKeys(pending2, mergedData);

            send("out", new Message(mergedData));

            pending1 = null;
            pending2 = null;
        }
    }

    private void mergeKeys(Message source, Map<String, Object> target) {
        String[] keys = {"sensorId", "temperature", "humidity"};

        for (String key : keys) {
            if (source.hasKey(key)) {
                target.put(key, source.get(key));
            }
        }

        target.put("timestamp", System.currentTimeMillis());
    }

    @Override
    public void initialize() {
    }

    @Override
    public void shutdown() {
    }

}
