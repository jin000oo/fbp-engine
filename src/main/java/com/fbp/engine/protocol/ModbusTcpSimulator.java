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

package com.fbp.engine.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ModbusTcpSimulator {

    private final int port;

    private ServerSocket serverSocket;

    private final int[] registers;

    private volatile boolean running;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public ModbusTcpSimulator(int port, int registerCount) {
        this.port = port;
        this.registers = new int[registerCount];
    }

    public void start() {
        running = true;

        executorService.submit(() -> {
            try {
                serverSocket = new ServerSocket(port);

                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    executorService.submit(() -> handleClient(clientSocket));
                }

            } catch (Exception e) {
                if (running) {
                    log.error(e.getMessage());
                }
            }
        });
    }

    public void stop() {
        running = false;

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }

        executorService.shutdownNow();
    }

    private void handleClient(Socket socket) {
        try (DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
            while (running) {
                int transactionId = in.readUnsignedShort();
                int protocolId = in.readUnsignedShort();
                int length = in.readUnsignedShort();
                int unitId = in.readUnsignedByte();
                int functionCode = in.readUnsignedByte();

                if (functionCode == 0x03) {
                    int startAddress = in.readUnsignedShort();
                    int quantity = in.readUnsignedShort();

                    if (startAddress + quantity > registers.length) {
                        sendError(out, transactionId, unitId, functionCode, 0x02);
                        continue;
                    }

                    out.writeShort(transactionId);
                    out.writeShort(protocolId);
                    out.writeShort(3 + (quantity * 2));
                    out.writeByte(unitId);
                    out.writeByte(functionCode);
                    out.writeByte(quantity * 2);

                    synchronized (this) {
                        for (int i = 0; i < quantity; i++) {
                            out.writeShort(registers[startAddress + i]);
                        }
                    }

                    out.flush();

                } else if (functionCode == 0x06) {
                    int address = in.readUnsignedShort();
                    int value = in.readUnsignedShort();

                    if (address >= registers.length) {
                        sendError(out, transactionId, unitId, functionCode, 0x02);
                        continue;
                    }

                    synchronized (this) {
                        registers[address] = value;
                    }

                    out.writeShort(transactionId);
                    out.writeShort(protocolId);
                    out.writeShort(6);
                    out.writeByte(unitId);
                    out.writeByte(functionCode);
                    out.writeShort(address);
                    out.writeShort(value);

                    out.flush();

                } else {
                    sendError(out, transactionId, unitId, functionCode, 0x01);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void setRegister(int address, int value) {
        if (address >= 0 && address < registers.length) {
            registers[address] = value;
        }
    }

    public synchronized int getRegister(int address) {
        if (address >= 0 && address < registers.length) {
            return registers[address];
        }

        return -1;
    }

    private void sendError(DataOutputStream out, int transactionId, int unitId, int functionCode, int exceptionCode)
            throws IOException {
        out.writeShort(transactionId);
        out.writeShort(0);
        out.writeShort(3);
        out.writeByte(unitId);
        out.writeByte(functionCode | 0x80);
        out.writeByte(exceptionCode);

        out.flush();
    }

}
