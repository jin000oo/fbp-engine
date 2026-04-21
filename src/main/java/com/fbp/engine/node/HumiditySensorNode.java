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
import java.util.Map;

public class HumiditySensorNode extends AbstractNode {

    private final double min;

    private final double max;

    public HumiditySensorNode(String id, double min, double max) {
        super(id);
        this.min = min;
        this.max = max;
        addInputPort("trigger");
        addOutputPort("out");
    }

    @Override
    public void onProcess(Message message) {
        double randomHumidity = min + Math.random() * (max - min);
        double humidity = Math.round(randomHumidity * 10.0) / 10.0;

        Message msg = new Message(Map.of(
                "sensorId", getId(),
                "humidity", humidity,
                "unit", "%",
                "timestamp", System.currentTimeMillis())
        );

        send("out", msg);
    }

    @Override
    public void initialize() {
    }

    @Override
    public void shutdown() {
    }

}
