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

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.io.AbstractModbusTransport;
import com.ghgande.j2mod.modbus.io.ModbusRTUTCPTransport;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Class that implements a TCPSlaveConnection.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public class TCPSlaveConnection {

    private static final Logger logger = LoggerFactory.getLogger(TCPSlaveConnection.class);

    // instance attributes
    private Socket socket;
    private int timeout = Modbus.DEFAULT_TIMEOUT;
    private boolean connected;
    private ModbusTCPTransport transport;

    /**
     * Constructs a <tt>TCPSlaveConnection</tt> instance using a given socket
     * instance.
     *
     * @param socket the socket instance to be used for communication.
     */
    public TCPSlaveConnection(Socket socket) {
        this(socket, false);
    }

    /**
     * Constructs a <tt>TCPSlaveConnection</tt> instance using a given socket
     * instance.
     *
     * @param socket        the socket instance to be used for communication.
     * @param useRtuOverTcp True if the RTU protocol should be used over TCP
     */
    public TCPSlaveConnection(Socket socket, boolean useRtuOverTcp) {
        try {
            setSocket(socket, useRtuOverTcp);
        }
        catch (IOException ex) {
            logger.debug("TCPSlaveConnection::Socket invalid");

            throw new IllegalStateException("Socket invalid", ex);
        }
    }

    /**
     * Closes this <tt>TCPSlaveConnection</tt>.
     */
    public void close() {
        if (connected) {
            try {
                transport.close();
                socket.close();
            }
            catch (IOException ex) {
                logger.warn("Could not close socket", ex);
            }
            connected = false;
        }
    }

    /**
     * Returns the <tt>ModbusTransport</tt> associated with this
     * <tt>TCPMasterConnection</tt>.
     *
     * @return the connection's <tt>ModbusTransport</tt>.
     */
    public AbstractModbusTransport getModbusTransport() {
        return transport;
    }

    /**
     * Prepares the associated <tt>ModbusTransport</tt> of this
     * <tt>TCPMasterConnection</tt> for use.
     *
     * @param socket        the socket to be used for communication.
     * @param useRtuOverTcp True if the RTU protocol should be used over TCP
     * @throws IOException if an I/O related error occurs.
     */
    private void setSocket(Socket socket, boolean useRtuOverTcp) throws IOException {
        this.socket = socket;

        if (transport == null) {
            if (useRtuOverTcp) {
                logger.trace("setSocket() -> using RTU over TCP transport.");
                transport = new ModbusRTUTCPTransport(socket);
            }
            else {
                logger.trace("setSocket() -> using standard TCP transport.");
                transport = new ModbusTCPTransport(socket);
            }
        }
        else {
            transport.setSocket(socket);
        }

        connected = true;
    }

    /**
     * Returns the timeout for this <tt>TCPSlaveConnection</tt>.
     *
     * @return the timeout as <tt>int</tt>.
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Sets the timeout for this <tt>TCPSlaveConnection</tt>.
     *
     * @param timeout the timeout in milliseconds as <tt>int</tt>.
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;

        try {
            socket.setSoTimeout(timeout);
        }
        catch (IOException ex) {
            logger.warn("Could not set timeout to " + timeout, ex);
        }
    }

    /**
     * Returns the destination port of this <tt>TCPSlaveConnection</tt>.
     *
     * @return the port number as <tt>int</tt>.
     */
    public int getPort() {
        return socket.getLocalPort();
    }

    /**
     * Returns the destination <tt>InetAddress</tt> of this
     * <tt>TCPSlaveConnection</tt>.
     *
     * @return the destination address as <tt>InetAddress</tt>.
     */
    public InetAddress getAddress() {
        return socket.getLocalAddress();
    }

    /**
     * Tests if this <tt>TCPSlaveConnection</tt> is connected.
     *
     * @return <tt>true</tt> if connected, <tt>false</tt> otherwise.
     */
    public boolean isConnected() {
        return connected;
    }
}