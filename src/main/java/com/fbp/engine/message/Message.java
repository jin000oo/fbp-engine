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

package com.fbp.engine.message;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Message {

    private final String id;

    private final Map<String, Object> payload;

    private final long timestamp;

    public Message(Map<String, Object> payload) {
        this.id = UUID.randomUUID().toString();
        this.payload = Collections.unmodifiableMap(new HashMap<>(payload));
        this.timestamp = System.currentTimeMillis();
    }

    public <T> T get(String key) {
        return (T) payload.get(key);
    }

    public Message withEntry(String key, Object value) {
        Map<String, Object> newPayload = new HashMap<>(this.payload);
        newPayload.put(key, value);

        return new Message(newPayload);
    }

    public boolean hasKey(String key) {
        return payload.containsKey(key);
    }

    public Message withoutKey(String key) {
        Map<String, Object> newPayload = new HashMap<>(this.payload);
        newPayload.remove(key);

        return new Message(newPayload);
    }

}
