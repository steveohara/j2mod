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
package com.ghgande.j2mod.modbus.facade;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.io.AbstractModbusTransport;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Modbus/TCP Master facade.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public class ModbusTCPMaster extends AbstractModbusMaster {

    private TCPMasterConnection connection;
    private boolean reconnecting = false;

    /**
     * Constructs a new master facade instance for communication
     * with a given slave.
     *
     * @param addr an internet address as resolvable IP name or IP number,
     *             specifying the slave to communicate with.
     */
    public ModbusTCPMaster(String addr) {
        this(addr, Modbus.DEFAULT_PORT);
    }

    /**
     * Constructs a new master facade instance for communication
     * with a given slave.
     *
     * @param addr an internet address as resolvable IP name or IP number,
     *             specifying the slave to communicate with.
     * @param port the port the slave is listening to.
     */
    public ModbusTCPMaster(String addr, int port) {
        this(addr, port, Modbus.DEFAULT_TIMEOUT, false);
    }

    /**
     * Constructs a new master facade instance for communication
     * with a given slave.
     *
     * @param addr      an internet address as resolvable IP name or IP number,
     *                  specifying the slave to communicate with.
     * @param port      the port the slave is listening to.
     * @param timeout   Socket timeout in milliseconds
     * @param reconnect True if the socket should reconnect if it detcts a connection failure
     */
    public ModbusTCPMaster(String addr, int port, int timeout, boolean reconnect) {
        super();
        try {
            InetAddress slaveAddress = InetAddress.getByName(addr);
            connection = new TCPMasterConnection(slaveAddress);
            connection.setPort(port);
            connection.setTimeout(timeout);
            this.timeout = timeout;
            setReconnecting(reconnect);
        }
        catch (UnknownHostException e) {
            throw new RuntimeException("Failed to contruct ModbusTCPMaster instance.", e);
        }
    }

    /**
     * Connects this <tt>ModbusTCPMaster</tt> with the slave.
     *
     * @throws Exception if the connection cannot be established.
     */
    public synchronized void connect() throws Exception {
        connect(false);
    }

    /**
     * Connects this <tt>ModbusTCPMaster</tt> with the slave.
     *
     * @param useRtuOverTcp True if the RTU protocol should be used over TCP
     *
     * @throws Exception if the connection cannot be established.
     */
    public synchronized void connect(boolean useRtuOverTcp) throws Exception {
        if (connection != null && !connection.isConnected()) {
            connection.connect(useRtuOverTcp);
            transaction = connection.getModbusTransport().createTransaction();
            ((ModbusTCPTransaction)transaction).setReconnecting(reconnecting);
            setTransaction(transaction);
        }
    }

    /**
     * Disconnects this <tt>ModbusTCPMaster</tt> from the slave.
     */
    public synchronized void disconnect() {
        if (connection != null && connection.isConnected()) {
            connection.close();
            transaction = null;
            setTransaction(null);
        }
    }

    /**
     * Tests if a constant connection is maintained or if a new
     * connection is established for every transaction.
     *
     * @return true if a new connection should be established for each
     * transaction, false otherwise.
     */
    public boolean isReconnecting() {
        return reconnecting;
    }

    /**
     * Sets the flag that specifies whether to maintain a
     * constant connection or reconnect for every transaction.
     *
     * @param b true if a new connection should be established for each
     *          transaction, false otherwise.
     */
    public synchronized void setReconnecting(boolean b) {
        reconnecting = b;
        if (transaction != null) {
            ((ModbusTCPTransaction)transaction).setReconnecting(b);
        }
    }

    @Override
    public void setTimeout(int timeout) {
        super.setTimeout(timeout);
        if (connection != null) {
            connection.setTimeout(timeout);
        }
    }

    @Override
    public AbstractModbusTransport getTransport() {
        return connection == null ? null : connection.getModbusTransport();
    }
}