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
import java.util.Queue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public class Connection {

    private final String id;

    private final Queue<Message> buffer;

    @Setter
    private InputPort target;

    public void deliver(Message message) {
        buffer.add(message);

        if (target != null) {
            Message deliverMessage = buffer.poll();

            if (deliverMessage != null) {
                target.receive(deliverMessage);
            }
        }
    }

    public int getBufferSize() {
        return buffer.size();
    }

}
