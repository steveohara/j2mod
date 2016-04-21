/*
 * Copyright 2002-2016 jamod & j2mod development teams
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ghgande.j2mod.modbus.net;

import com.ghgande.j2mod.modbus.io.ModbusUDPTransport;
import com.ghgande.j2mod.modbus.util.ModbusUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Hashtable;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Class implementing a <tt>UDPSlaveTerminal</tt>.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
class UDPSlaveTerminal extends AbstractUDPTerminal {

    private static final Logger logger = LoggerFactory.getLogger(UDPSlaveTerminal.class);
    protected Hashtable<Integer, DatagramPacket> requests = new Hashtable<Integer, DatagramPacket>(342);
    private LinkedBlockingQueue<byte[]> sendQueue = new LinkedBlockingQueue<byte[]>();
    private LinkedBlockingQueue<byte[]> receiveQueue = new LinkedBlockingQueue<byte[]>();
    private PacketSender packetSender;
    private PacketReceiver packetReceiver;

    /**
     * Creates a slave terminal on the specified adapter address
     * Use 0.0.0.0 to listen on all adapters
     *
     * @param localaddress Local address to bind to
     */
    protected UDPSlaveTerminal(InetAddress localaddress) {
        address = localaddress;
    }

    @Override
    public synchronized void activate() throws Exception {
        if (!isActive()) {
            logger.debug("UDPSlaveTerminal.activate()");
            if (address != null && port != -1) {
                socket = new DatagramSocket(port, address);
            }
            else {
                socket = new DatagramSocket();
                port = socket.getLocalPort();
                address = socket.getLocalAddress();
            }
            logger.debug("UDPSlaveTerminal::haveSocket():{}", socket.toString());
            logger.debug("UDPSlaveTerminal::addr=:{}:port={}", address.toString(), port);

            socket.setReceiveBufferSize(1024);
            socket.setSendBufferSize(1024);

            // Never timeout the receive
            socket.setSoTimeout(0);

            // Start a sender
            packetSender = new PacketSender(socket);
            new Thread(packetSender).start();
            logger.debug("UDPSlaveTerminal::sender started()");

            // Start a receiver

            packetReceiver = new PacketReceiver(socket);
            new Thread(packetReceiver).start();
            logger.debug("UDPSlaveTerminal::receiver started()");

            // Create a transport to use

            transport = new ModbusUDPTransport(this);
            logger.debug("UDPSlaveTerminal::transport created");
            active = true;
        }
        logger.debug("UDPSlaveTerminal::activated");
    }

    @Override
    public synchronized void deactivate() {
        try {
            if (active) {
                // Stop receiver - this will stop and close the socket
                packetReceiver.stop();

                // Stop sender gracefully
                packetSender.stop();

                transport = null;
                active = false;
            }
        }
        catch (Exception ex) {
            logger.error("Error deactivating UDPSlaveTerminal", ex);
        }
    }

    @Override
    public void sendMessage(byte[] msg) throws Exception {
        sendQueue.add(msg);
    }

    @Override
    public byte[] receiveMessage() throws Exception {
        return receiveQueue.take();
    }

    /**
     * The background thread that is responsible for sending messages in response to requests
     */
    class PacketSender implements Runnable {

        private boolean running;
        private boolean closed;
        private Thread thread;
        private DatagramSocket socket;

        /**
         * Constructs a sender with th socket
         *
         * @param socket Socket to use
         */
        public PacketSender(DatagramSocket socket) {
            running = true;
            this.socket = socket;
        }

        /**
         * Stops the sender
         */
        public void stop() {
            running = false;
            thread.interrupt();
            while (!closed) {
                try {
                    Thread.sleep(100);
                }
                catch (InterruptedException e) {
                    logger.debug("interrupted");
                }
            }
        }

        /**
         * Thread loop that sends messages
         */
        public void run() {
            closed = false;
            thread = Thread.currentThread();
            do {
                try {
                    // Pickup the message and corresponding request
                    byte[] message = sendQueue.take();
                    DatagramPacket req = requests.remove(ModbusUtil.registersToInt(message));

                    // Create new Package with corresponding address and port
                    if (req != null) {
                        DatagramPacket res = new DatagramPacket(message, message.length, req.getAddress(), req.getPort());
                        socket.send(res);
                        logger.debug("Sent package from queue");
                    }
                }
                catch (Exception ex) {
                    // Ignore the error if we are no longer listening

                    if (running) {
                        logger.error("Problem reading UDP socket", ex);
                    }
                }
            } while (running);
            closed = true;
        }

    }

    /**
     * The background thread that receives messages and adds them to the process list
     * for further analysis
     */
    class PacketReceiver implements Runnable {

        private boolean running;
        private boolean closed;
        private DatagramSocket socket;

        /**
         * A receiver thread for reception of UDP messages
         *
         * @param socket Socket to use
         */
        public PacketReceiver(DatagramSocket socket) {
            running = true;
            this.socket = socket;
        }

        /**
         * Stops the thread
         */
        public void stop() {
            running = false;
            socket.close();
            while (!closed) {
                try {
                    Thread.sleep(100);
                }
                catch (InterruptedException e) {
                    logger.debug("interrupted");
                }
            }
        }

        /**
         * Background thread for reading UDP messages
         */
        public void run() {
            closed = false;
            do {
                try {
                    // 1. Prepare buffer and receive package
                    byte[] buffer = new byte[256];// max size
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    // 2. Extract TID and remember request
                    Integer tid = ModbusUtil.registersToInt(buffer);
                    requests.put(tid, packet);

                    // 3. place the data buffer in the queue
                    receiveQueue.put(buffer);
                    logger.debug("Received package to queue");
                }
                catch (Exception ex) {
                    // Ignore the error if we are no longer listening

                    if (running) {
                        logger.error("Problem reading UDP socket", ex);
                    }
                }
            } while (running);
            closed = true;
        }
    }
}
