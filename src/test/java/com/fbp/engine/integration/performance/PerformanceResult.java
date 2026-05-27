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

package com.fbp.engine.integration.performance;

public record PerformanceResult(long totalMessages,
                                long durationMs,
                                double throughput,
                                double averageLatencyMs,
                                long errorCount,
                                double errorRate) {

    public PerformanceResult(long totalMessages, long durationMs, double throughput, long errorCount,
                             double errorRate) {
        this(totalMessages, durationMs, throughput, 0, errorCount, errorRate);
    }

}
