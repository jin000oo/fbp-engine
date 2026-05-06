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

package com.fbp.engine.node;

import com.fbp.engine.core.ProtocolNode;
import com.fbp.engine.message.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

public class EchoProtocolNode extends ProtocolNode {

    private Socket socket;

    private BufferedReader in;

    private PrintWriter out;

    private Thread thread;

    public EchoProtocolNode(String id, Map<String, Object> config) {
        super(id, config);
        addInputPort("in");
        addOutputPort("out");
    }

    @Override
    protected void connect() throws IOException {
        socket = new Socket((String) getConfig("host"), (int) getConfig("port"));
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        thread = new Thread(() -> {
            try {
                String response;

                while (!Thread.currentThread().isInterrupted() && (response = in.readLine()) != null) {
                    send("out", new Message(Map.of("response", response)));
                }

            } catch (IOException e) {
                if (isConnected()) {
                    reconnect();
                }
            }
        });

        thread.start();
    }

    @Override
    protected void disconnect() throws IOException {
        if (thread != null) {
            thread.interrupt();
        }

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

    @Override
    public void process(Message message) {
        if (isConnected() && message.hasKey("data")) {
            out.println(message.get("data").toString());
        }
    }

}
