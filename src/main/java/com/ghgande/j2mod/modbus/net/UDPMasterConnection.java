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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

/**
 * Class that implements a UDPMasterConnection.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public class UDPMasterConnection {

    private static final Logger logger = LoggerFactory.getLogger(UDPMasterConnection.class);

    //instance attributes
    private UDPMasterTerminal terminal;
    private int timeout = Modbus.DEFAULT_TIMEOUT;
    private boolean connected;

    private InetAddress address;
    private int port = Modbus.DEFAULT_PORT;

    /**
     * Constructs a <tt>UDPMasterConnection</tt> instance
     * with a given destination address.
     *
     * @param adr the destination <tt>InetAddress</tt>.
     */
    public UDPMasterConnection(InetAddress adr) {
        address = adr;
    }

    /**
     * Opens this <tt>UDPMasterConnection</tt>.
     *
     * @throws Exception if there is a network failure.
     */
    public synchronized void connect() throws Exception {
        if (!connected) {
            terminal = new UDPMasterTerminal(address);
            terminal.setPort(port);
            terminal.setTimeout(timeout);
            terminal.activate();
            connected = true;
        }
    }

    /**
     * Closes this <tt>UDPMasterConnection</tt>.
     */
    public void close() {
        if (connected) {
            try {
                terminal.deactivate();
            }
            catch (Exception ex) {
                logger.debug("Exception occurred while closing UDPMasterConnection", ex);
            }
            connected = false;
        }
    }

    /**
     * Returns the <tt>ModbusTransport</tt> associated with this
     * <tt>UDPMasterConnection</tt>.
     *
     * @return the connection's <tt>ModbusTransport</tt>.
     */
    public AbstractModbusTransport getModbusTransport() {
        return terminal == null ? null : terminal.getTransport();
    }

    /**
     * Returns the terminal used for handling the package traffic.
     *
     * @return a <tt>UDPTerminal</tt> instance.
     */
    public AbstractUDPTerminal getTerminal() {
        return terminal;
    }

    /**
     * Returns the timeout for this <tt>UDPMasterConnection</tt>.
     *
     * @return the timeout as <tt>int</tt>.
     */
    public synchronized int getTimeout() {
        return timeout;
    }

    /**
     * Sets the timeout for this <tt>UDPMasterConnection</tt>.
     *
     * @param timeout the timeout as <tt>int</tt>.
     */
    public synchronized void setTimeout(int timeout) {
        this.timeout = timeout;
        if (terminal != null) {
            terminal.setTimeout(timeout);
        }
    }

    /**
     * Returns the destination port of this
     * <tt>UDPMasterConnection</tt>.
     *
     * @return the port number as <tt>int</tt>.
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the destination port of this
     * <tt>UDPMasterConnection</tt>.
     * The default is defined as <tt>Modbus.DEFAULT_PORT</tt>.
     *
     * @param port the port number as <tt>int</tt>.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Returns the destination <tt>InetAddress</tt> of this
     * <tt>UDPMasterConnection</tt>.
     *
     * @return the destination address as <tt>InetAddress</tt>.
     */
    public InetAddress getAddress() {
        return address;
    }

    /**
     * Sets the destination <tt>InetAddress</tt> of this
     * <tt>UDPMasterConnection</tt>.
     *
     * @param adr the destination address as <tt>InetAddress</tt>.
     */
    public void setAddress(InetAddress adr) {
        address = adr;
    }

    /**
     * Tests if this <tt>UDPMasterConnection</tt> is connected.
     *
     * @return <tt>true</tt> if connected, <tt>false</tt> otherwise.
     */
    public boolean isConnected() {
        return connected;
    }

}