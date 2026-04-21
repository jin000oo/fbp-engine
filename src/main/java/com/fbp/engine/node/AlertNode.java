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

public class AlertNode extends AbstractNode {

    public AlertNode(String id) {
        super(id);
        addInputPort("in");
    }

    @Override
    public void onProcess(Message message) {
        Object sensorId = message.get("sensorId");

        if (message.hasKey("temperature")) {
            Object temperature = message.get("temperature");

            if (temperature instanceof Number) {
                System.out.printf("[경고] 센서 %s 온도 %.1f°C — 임계값 초과!%n", sensorId, ((Number) temperature).doubleValue());
            }
        } else if (message.hasKey("humidity")) {
            Object humidity = message.get("humidity");

            if (humidity instanceof Number) {
                System.out.printf("[경고] 센서 %s 습도 %.1f%% — 임계값 초과!%n", sensorId, ((Number) humidity).doubleValue());
            }
        } else {
            System.out.println("[경고] 알 수 없는 센서 데이터");
        }
    }

    @Override
    public void initialize() {
    }

    @Override
    public void shutdown() {
    }

}
