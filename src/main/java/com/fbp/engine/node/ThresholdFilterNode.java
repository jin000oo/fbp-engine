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

public class ThresholdFilterNode extends AbstractNode {

    private final String fieldName;

    private final double threshold;

    public ThresholdFilterNode(String id, String fieldName, double threshold) {
        super(id);
        this.fieldName = fieldName;
        this.threshold = threshold;
        addInputPort("in");
        addOutputPort("alert");
        addOutputPort("normal");
    }

    @Override
    public void onProcess(Message message) {
        if (!message.hasKey(fieldName)) {
            return;
        }

        Object value = message.get(fieldName);

        if (value instanceof Number) {
            double result = ((Number) value).doubleValue();

            if (result > threshold) {
                send("alert", message);
            } else {
                send("normal", message);
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
