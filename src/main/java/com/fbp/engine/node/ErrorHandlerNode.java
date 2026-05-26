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
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ErrorHandlerNode extends AbstractNode {

    private final int maxRetries;

    public ErrorHandlerNode(String id) {
        this(id, 3);
    }

    public ErrorHandlerNode(String id, int maxRetries) {
        super(id);
        this.maxRetries = maxRetries;
        addInputPort("in");
        addOutputPort("out");
        addOutputPort("retry");
        addOutputPort("deadLetter");
    }

    @Override
    public void onProcess(Message message) {
        log.warn("[ErrorHandler] {} 수신", message.getPayload());

        Map<String, Object> payload = new HashMap<>(message.getPayload());
        int retryCount = (int) payload.getOrDefault("_retryCount", 0);

        if (retryCount < maxRetries) {
            retryCount++;

            payload.put("_retryCount", retryCount);
            log.info("[ErrorHandler] 재시도 횟수: {}/{}", retryCount, maxRetries);

            send("retry", new Message(payload));
        } else {
            log.error("[ErrorHandler] 재시도 횟수 초과. DeadLetter로 전송");
            send("deadLetter", message);
        }

        send("out", message);
    }

    @Override
    public void initialize() {
        log.info("[ErrorHandler] {} 초기화 완료", getId());
    }

    @Override
    public void shutdown() {
        log.info("[ErrorHandler] {} 종료", getId());
    }

}
