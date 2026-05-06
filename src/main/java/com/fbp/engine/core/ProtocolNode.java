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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Getter;

public abstract class ProtocolNode extends AbstractNode {

    private final Map<String, Object> config;

    @Getter
    private ConnectionState connectionState = ConnectionState.DISCONNECTED;

    private final long reconnectIntervalMs;

    private final int maxRetries;

    private int currentRetries = 0;

    private ScheduledExecutorService reconnectScheduler;

    public ProtocolNode(String id, Map<String, Object> config) {
        super(id);
        this.config = config != null ? config : new HashMap<>();
        this.reconnectIntervalMs = ((Number) this.config.getOrDefault("reconnectIntervalMs", 5000L)).longValue();
        this.maxRetries = (int) this.config.getOrDefault("maxRetries", 10);
    }

    @Override
    public void onProcess(Message message) {
    }

    @Override
    public void initialize() {
        this.reconnectScheduler = Executors.newSingleThreadScheduledExecutor();
        tryConnect();
    }

    @Override
    public void shutdown() {
        if (reconnectScheduler != null && !reconnectScheduler.isShutdown()) {
            reconnectScheduler.shutdownNow();
        }

        try {
            disconnect();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        connectionState = ConnectionState.DISCONNECTED;
    }

    private void tryConnect() {
        this.connectionState = ConnectionState.CONNECTING;

        try {
            connect();
            connectionState = ConnectionState.CONNECTED;
            currentRetries = 0;

        } catch (Exception e) {
            connectionState = ConnectionState.ERROR;
            reconnect();
        }
    }

    protected abstract void connect() throws IOException;

    protected abstract void disconnect() throws IOException;

    protected void reconnect() {
        if (currentRetries >= maxRetries) {
            connectionState = ConnectionState.DISCONNECTED;
            return;
        }

        currentRetries++;

        reconnectScheduler.schedule(this::tryConnect, reconnectIntervalMs, TimeUnit.MILLISECONDS);
    }

    public Object getConfig(String key) {
        return config.get(key);
    }

    public boolean isConnected() {
        return connectionState == ConnectionState.CONNECTED;
    }

}
