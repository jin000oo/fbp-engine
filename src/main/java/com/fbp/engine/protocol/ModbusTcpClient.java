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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ModbusTcpClient {

    private Socket socket;

    private DataOutputStream out;

    private DataInputStream in;

    private int transactionId = 0;

    private final String host;

    private final int port;

    public ModbusTcpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() throws IOException {
        socket = new Socket(host, port);
        socket.setSoTimeout(3000);

        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
    }

    public void disconnect() throws IOException {
        if (in != null) {
            in.close();
        }

        if (out != null) {
            out.close();
        }

        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public synchronized int[] readHoldingRegisters(int unitId, int startAddress, int quantity)
            throws IOException, ModbusException {
        // FC 03 요청 프레임 조립 (MBAP 헤더 + PDU)
        transactionId++;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        buildMbapHeader(dos, transactionId, 6, unitId);

        dos.writeByte(0x03);         // Function Code
        dos.writeShort(startAddress);
        dos.writeShort(quantity);

        byte[] frame = baos.toByteArray();
        out.write(frame);
        out.flush();

        readMbapHeader();

        int respFunctionCode = in.readUnsignedByte();

        if (respFunctionCode == (0x03 | 0x80)) {
            throw new ModbusException(0x03, in.readUnsignedByte());
        }

        int[] registers = new int[in.readUnsignedByte() / 2];

        for (int i = 0; i < registers.length; i++) {
            registers[i] = in.readUnsignedShort();
        }

        return registers;
    }

    public synchronized void writeSingleRegister(int unitId, int address, int value)
            throws IOException, ModbusException {
        // FC 06 요청 프레임 조립
        transactionId++;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        buildMbapHeader(dos, transactionId, 6, unitId);

        dos.writeByte(0x06);         // Function Code
        dos.writeShort(address);
        dos.writeShort(value);

        byte[] frame = baos.toByteArray();
        out.write(frame);
        out.flush();

        readMbapHeader();

        int respFunctionCode = in.readUnsignedByte();

        if (respFunctionCode == (0x06 | 0x80)) {
            throw new ModbusException(0x06, in.readUnsignedByte());
        }

        int respAddress = in.readUnsignedShort();
        int respValue = in.readUnsignedShort();

        if (respAddress != address || respValue != value) {
            throw new ModbusException(0x06, in.readUnsignedByte());
        }
    }

    private void buildMbapHeader(DataOutputStream dos, int transactionId, int length, int unitId) throws IOException {
        dos.writeShort(transactionId);  // 2바이트 Big-Endian
        dos.writeShort(0x0000);      // Protocol ID
        dos.writeShort(length);         // Length
        dos.writeByte(unitId);          // Unit ID
    }

    private void readMbapHeader() throws IOException {
        in.readUnsignedShort(); // respTransactionId
        in.readUnsignedShort(); // respProtocolId
        in.readUnsignedShort(); // respLength
        in.readUnsignedByte();  // respUnitId
    }

}
