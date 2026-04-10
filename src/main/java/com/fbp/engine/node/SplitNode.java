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

public class SplitNode extends AbstractNode {

    private final String key;

    private final double threshold;

    public SplitNode(String id, String key, double threshold) {
        super(id);
        this.key = key;
        this.threshold = threshold;
        addInputPort("in");
        addOutputPort("match");
        addOutputPort("mismatch");
    }

    @Override
    public void onProcess(Message message) {
        if (!message.hasKey(key)) {
            return;
        }

        Object rawValue = message.get(key);

        if (rawValue instanceof Number) {
            double value = ((Number) rawValue).doubleValue();

            if (value >= threshold) {
                send("match", message);
            } else {
                send("mismatch", message);
            }
        }
    }

    @Override
    public void initialize() {
    }

    @Override
    public void shutdown() {
    }

}
