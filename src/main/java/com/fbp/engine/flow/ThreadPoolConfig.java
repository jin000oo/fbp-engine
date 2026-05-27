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

package com.fbp.engine.flow;

public record ThreadPoolConfig(int corePoolSize,
                               int maxPoolSize,
                               int queueCapacity) {

    public ThreadPoolConfig() {
        this(10, 20, 1000);
    }

    public ThreadPoolConfig(int corePoolSize, int maxPoolSize) {
        this(corePoolSize, maxPoolSize, 1000);
    }

}
