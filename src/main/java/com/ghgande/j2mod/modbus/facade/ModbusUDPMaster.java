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
import com.ghgande.j2mod.modbus.net.UDPMasterConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Modbus/UDP Master facade.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public class ModbusUDPMaster extends AbstractModbusMaster {

    private static final Logger logger = LoggerFactory.getLogger(ModbusUDPMaster.class);

    private UDPMasterConnection connection;

    /**
     * Constructs a new master facade instance for communication
     * with a given slave.
     *
     * @param addr an internet address as resolvable IP name or IP number,
     *             specifying the slave to communicate with.
     */
    public ModbusUDPMaster(String addr) {
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
    public ModbusUDPMaster(String addr, int port) {
        this(addr, port, Modbus.DEFAULT_TIMEOUT);
    }

    /**
     * Constructs a new master facade instance for communication
     * with a given slave.
     *
     * @param addr    an internet address as resolvable IP name or IP number,
     *                specifying the slave to communicate with.
     * @param port    the port the slave is listening to.
     * @param timeout Socket timeout in milliseconds
     */
    public ModbusUDPMaster(String addr, int port, int timeout) {
        super();
        try {
            InetAddress slaveAddress = InetAddress.getByName(addr);
            connection = new UDPMasterConnection(slaveAddress);
            connection.setPort(port);
            connection.setTimeout(timeout);
        }
        catch (UnknownHostException e) {
            throw new RuntimeException("Failed to construct ModbusUDPMaster instance.", e);
        }
    }

    /**
     * Connects this <tt>ModbusTCPMaster</tt> with the slave.
     *
     * @throws Exception if the connection cannot be established.
     */
    public synchronized void connect() throws Exception {
        if (connection != null && !connection.isConnected()) {
            connection.connect();
            transaction = connection.getModbusTransport().createTransaction();
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