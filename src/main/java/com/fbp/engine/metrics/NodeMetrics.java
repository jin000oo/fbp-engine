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

package com.fbp.engine.metrics;

import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;

@Getter
public class NodeMetrics {

    private final AtomicLong processed = new AtomicLong(0);

    private final AtomicLong errors = new AtomicLong(0);

    private final AtomicLong totalTimeNs = new AtomicLong(0);

    public void record(long durationNs, boolean success) {
        processed.incrementAndGet();
        totalTimeNs.addAndGet(durationNs);

        if (!success) {
            errors.incrementAndGet();
        }
    }

    public double getAverageTimeMs() {
        long process = processed.get();
        if (process == 0) {
            return 0.0;
        }

        return ((totalTimeNs.get()) / (double) process) / 1_000_000.0;
    }

}
