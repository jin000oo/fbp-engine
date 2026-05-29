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

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import lombok.Getter;

public class InfluxDBWriter {

    private final InfluxDBConfig config;

    private final InfluxDBClient client;

    @Getter
    private final WriteApiBlocking writeApi;

    public InfluxDBWriter(InfluxDBConfig config) {
        this.config = config;
        this.client =
                InfluxDBClientFactory.create(config.url(), config.token().toCharArray(), config.org(), config.bucket());
        this.writeApi = client.getWriteApiBlocking();
    }

    public void writeNodeMetric(String nodeId, long durationNs, boolean success) {
        Point point = Point.measurement("node_metrics")
                .addTag("node_id", nodeId)
                .addField("duration_ns", durationNs)
                .addField("success", success)
                .time(System.currentTimeMillis(), WritePrecision.MS);

        writeApi.writePoint(point);
    }

    public void close() {
        client.close();
    }

}
