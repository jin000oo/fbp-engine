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

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetricWorker implements Runnable {

    private final InfluxDBConfig config;

    private final InfluxDBWriter writer;

    private final BlockingQueue<MetricEvent> queue;

    private final List<Point> batchBuffer;

    private long lastFlushTime = System.currentTimeMillis();

    private volatile boolean running = true;

    public MetricWorker(InfluxDBConfig config, InfluxDBWriter writer, BlockingQueue<MetricEvent> queue) {
        this.config = config;
        this.writer = writer;
        this.queue = queue;
        this.batchBuffer = new ArrayList<>();
    }

    @Override
    public void run() {
        while (running || !queue.isEmpty()) {
            try {
                MetricEvent event = queue.poll(100, TimeUnit.MILLISECONDS);

                if (event != null) {
                    Point point = Point.measurement("node_metrics")
                            .addTag("node_id", event.nodeId())
                            .addField("duration_ns", event.durationNs())
                            .addField("success", event.success())
                            .time(event.timestamp(), WritePrecision.MS);

                    batchBuffer.add(point);
                }

                boolean isBatchFull = batchBuffer.size() >= config.batchSize();

                boolean isTimeUp = System.currentTimeMillis() - lastFlushTime >= config.flushIntervalMs();

                if ((isBatchFull || isTimeUp) && !batchBuffer.isEmpty()) {
                    try {
                        List<Point> pointsToSend = new ArrayList<>(batchBuffer);
                        writer.getWriteApi().writePoints(pointsToSend);
                        batchBuffer.clear();
                        lastFlushTime = System.currentTimeMillis();

                    } catch (Exception e) {
                        log.error("InfluxDB 전송 실패: {}, 다음 배치에 재시도", e.getMessage());
                    }
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void stop() {
        running = false;
    }

}
