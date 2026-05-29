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

import com.fbp.engine.flow.ThreadPoolConfig;
import com.fbp.engine.message.Message;
import com.fbp.engine.metrics.InfluxDBConfig;
import com.fbp.engine.metrics.InfluxDBWriter;
import com.fbp.engine.metrics.MetricWorker;
import com.fbp.engine.metrics.MetricsCollector;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.Getter;

public class FlowEngine {

    public enum State {
        INITIALIZED, RUNNING, STOPPED
    }

    @Getter
    private State state = State.INITIALIZED;

    @Getter
    private final Map<String, Flow> flows = new ConcurrentHashMap<>();

    private final ExecutorService executor;

    private final InfluxDBConfig config;

    private final InfluxDBWriter writer;

    private final MetricWorker worker;

    private Thread workerThread;

    public FlowEngine() {   // 테스트 코드 수정 귀찮아서 일단 더미 설정..
        this(new ThreadPoolConfig(10, 20, 1000),
                new InfluxDBConfig("http://localhost:8086", "token", "org", "bucket"));
    }

    public FlowEngine(InfluxDBConfig config) {
        this(new ThreadPoolConfig(10, 20, 1000), config);
    }

    public FlowEngine(ThreadPoolConfig threadPoolConfig, InfluxDBConfig config) {
        this.executor = new ThreadPoolExecutor(
                threadPoolConfig.corePoolSize(),
                threadPoolConfig.maxPoolSize(),
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(threadPoolConfig.queueCapacity())
        );
        this.config = config;
        this.writer = new InfluxDBWriter(config);
        this.worker = new MetricWorker(config, writer, MetricsCollector.getInstance().getEventQueue());
        this.workerThread = new Thread(worker, "metric-worker-thread");
        this.workerThread.setDaemon(true);
        this.workerThread.start();
    }

    public void register(Flow flow) {
        flows.put(flow.getId(), flow);
        System.out.printf("[Engine] 플로우 '%s' 등록됨%n", flow.getId());
    }

    public void startFlow(String flowId) {
        Flow flow = flows.get(flowId);
        if (flow == null) {
            throw new IllegalArgumentException();
        }

        List<String> errors = flow.validate();
        if (!errors.isEmpty()) {
            throw new IllegalStateException();
        }

        flow.initialize();
        flow.setState(Flow.State.RUNNING);
        this.state = State.RUNNING;

        for (Connection connection : flow.getConnections()) {
            executor.submit(() -> {
                while (flow.getState() == Flow.State.RUNNING) {
                    Message message = connection.take();

                    if (message != null && connection.getTarget() != null) {
                        connection.getTarget().receive(message);
                    }
                }
            });
        }

        System.out.printf("[Engine] 플로우 '%s' 시작됨%n", flowId);
    }

    public void stopFlow(String flowId) {
        Flow flow = flows.get(flowId);
        if (flow != null) {
            flow.shutdown();
            flow.setState(Flow.State.STOPPED);
            this.state = State.STOPPED;

            System.out.printf("[Engine] 플로우 '%s' 정지됨%n", flowId);
        }
    }

    public void removeFlow(String flowId) {
        Flow flow = flows.get(flowId);
        if (flow != null) {
            if (flow.getState() == Flow.State.RUNNING) {
                stopFlow(flowId);
            }

            flows.remove(flowId);

            System.out.printf("[Engine] 플로우 '%s' 삭제됨%n", flowId);
        }
    }

    public void shutdown() {
        if (worker != null) {
            worker.stop();

            try {
                workerThread.join(3000);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (writer != null) {
            writer.close();
        }

        for (Flow flow : flows.values()) {
            flow.shutdown();
            flow.setState(Flow.State.STOPPED);
        }

        this.state = State.STOPPED;

        executor.shutdownNow();

        System.out.println("[Engine] 엔진 종료됨");
    }

    public void listFlows() {
        int count = 0;

        for (Flow flow : flows.values()) {
            System.out.printf("[%d] %s  %s%n", count++, flow.getId(), flow.getState());
        }
    }

}
