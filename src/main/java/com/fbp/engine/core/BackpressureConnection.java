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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BackpressureConnection extends Connection {

    private final BlockingQueue<Message> blockingQueue;

    private final BackpressureStrategy strategy;

    public BackpressureConnection(String id, int capacity, BackpressureStrategy strategy) {
        super(id);
        this.blockingQueue = new ArrayBlockingQueue<>(capacity);
        this.strategy = strategy;
    }

    @Override
    public void deliver(Message message) {
        try {
            strategy.handle(blockingQueue, message);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public Message take() {
        try {
            return blockingQueue.take();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @Override
    public Message poll() {
        return blockingQueue.poll();
    }

    @Override
    public Message poll(long timeout, java.util.concurrent.TimeUnit unit) {
        try {
            return blockingQueue.poll(timeout, unit);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @Override
    public int getBufferSize() {
        return blockingQueue.size();
    }

}
