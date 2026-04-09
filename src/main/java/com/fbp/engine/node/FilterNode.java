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

import com.fbp.engine.core.DefaultInputPort;
import com.fbp.engine.core.DefaultOutputPort;
import com.fbp.engine.core.InputPort;
import com.fbp.engine.core.Node;
import com.fbp.engine.core.OutputPort;
import com.fbp.engine.message.Message;
import lombok.Getter;

@Getter
public class FilterNode implements Node {

    private final String id;

    private final String key;

    private final double threshold;

    private final InputPort inputPort;

    private final OutputPort outputPort;

    public FilterNode(String id, String key, double threshold) {
        this.id = id;
        this.key = key;
        this.threshold = threshold;
        this.inputPort = new DefaultInputPort("in", this);
        this.outputPort = new DefaultOutputPort("out");
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void process(Message message) {
        if (!message.hasKey(key)) {
            return;
        }

        Object rawValue = message.get(key);

        if (rawValue instanceof Number) {
            double value = ((Number) rawValue).doubleValue();

            if (value >= threshold) {
                outputPort.send(message);
            }
        }
    }

}
