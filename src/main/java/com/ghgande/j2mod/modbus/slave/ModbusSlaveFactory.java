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
package com.ghgande.j2mod.modbus.slave;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.net.AbstractModbusListener;
import com.ghgande.j2mod.modbus.util.ModbusUtil;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a factory class that allows users to easily create and manages slaves.<br>
 * Each slave is uniquely identified by the port it is listening on, irrespective of if
 * the socket type (TCP or UDP)
 *
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public class ModbusSlaveFactory {

    private static final Logger logger = LoggerFactory.getLogger(ModbusSlaveFactory.class);
    private static Map<String, ModbusSlave> slaves = new HashMap<String, ModbusSlave>();

    /**
     * Creates a TCP modbus slave or returns the one already allocated to this port
     *
     * @param port     Port to listen on
     * @param poolSize Pool size of listener threads
     * @param useRtuOverTcp True if the RTU protocol should be used over TCP
     * @return new or existing TCP modbus slave associated with the port
     * @throws ModbusException If a problem occurs e.g. port already in use
     */
    public static synchronized ModbusSlave createTCPSlave(int port, int poolSize, boolean useRtuOverTcp) throws ModbusException {
        String key = ModbusSlaveType.TCP.getKey(port);
        if (slaves.containsKey(key)) {
            return slaves.get(key);
        }
        else {
            ModbusSlave slave = new ModbusSlave(port, poolSize, useRtuOverTcp);
            slaves.put(key, slave);
            return slave;
        }
    }

    /**
     * Creates a UDP modbus slave or returns the one already allocated to this port
     *
     * @param port Port to listen on
     * @return new or existing UDP modbus slave associated with the port
     * @throws ModbusException If a problem occurs e.g. port already in use
     */
    public static synchronized ModbusSlave createUDPSlave(int port) throws ModbusException {
        String key = ModbusSlaveType.UDP.getKey(port);
        if (slaves.containsKey(key)) {
            return slaves.get(key);
        }
        else {
            ModbusSlave slave = new ModbusSlave(port, false);
            slaves.put(key, slave);
            return slave;
        }
    }

    /**
     * Creates a serial modbus slave or returns the one already allocated to this port
     *
     * @param serialParams Serial parameters for serial type slaves
     * @return new or existing Serial modbus slave associated with the port
     * @throws ModbusException If a problem occurs e.g. port already in use
     */
    public static synchronized ModbusSlave createSerialSlave(SerialParameters serialParams) throws ModbusException {
        if (serialParams == null) {
            throw new ModbusException("Serial parameters are null");
        }
        else if (ModbusUtil.isBlank(serialParams.getPortName())) {
            throw new ModbusException("Serial port name is empty");
        }
        if (slaves.containsKey(serialParams.getPortName())) {
            return slaves.get(serialParams.getPortName());
        }
        else {
            ModbusSlave slave = new ModbusSlave(serialParams);
            slaves.put(serialParams.getPortName(), slave);
            return slave;
        }
    }

    /**
     * Closes this slave and removes it from the running list
     *
     * @param slave Slave to remove
     */
    public static synchronized void close(ModbusSlave slave) {
        if (slave != null) {
            slave.closeListener();
            slaves.remove(slave.getType().getKey(slave.getPort()));
        }
    }

    /**
     * Closes all slaves and removes them from the running list
     */
    public static synchronized void close() {
        for (ModbusSlave slave : new ArrayList<ModbusSlave>(slaves.values())) {
            slave.close();
        }
    }

    /**
     * Returns the running slave listening on the given IP port
     *
     * @param port Port to check for running slave
     * @return Null or ModbusSlave
     */
    public static ModbusSlave getSlave(int port) {
        return slaves.get(port + "");
    }

    /**
     * Returns the running slave listening on the given serial port
     *
     * @param port Port to check for running slave
     * @return Null or ModbusSlave
     */
    public static ModbusSlave getSlave(String port) {
        return ModbusUtil.isBlank(port) ? null : slaves.get(port);
    }

    /**
     * Returns the running slave that utilises the give listener
     *
     * @param listener Listener used for this slave
     * @return Null or ModbusSlave
     */
    public static synchronized ModbusSlave getSlave(AbstractModbusListener listener) {
        for (ModbusSlave slave : slaves.values()) {
            if (slave.getListener().equals(listener)) {
                return slave;
            }
        }
        return null;
    }

}
