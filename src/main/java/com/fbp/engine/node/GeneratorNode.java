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

import com.fbp.engine.core.DefaultOutputPort;
import com.fbp.engine.core.Node;
import com.fbp.engine.core.OutputPort;
import com.fbp.engine.message.Message;
import java.util.Map;
import lombok.Getter;

@Getter
public class GeneratorNode implements Node {

    private final String id;

    private final OutputPort outputPort;

    public GeneratorNode(String id) {
        this.id = id;
        this.outputPort = new DefaultOutputPort("out");
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void process(Message message) {
        // GeneratorNode는 외부 메시지를 처리하지 않으므로 빈 구현
    }

    public void generate(String key, Object value) {
        Message message = new Message(Map.of(key, value));

        outputPort.send(message);
    }

}
