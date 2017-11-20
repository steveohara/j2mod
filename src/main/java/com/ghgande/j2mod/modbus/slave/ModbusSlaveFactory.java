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
import com.ghgande.j2mod.modbus.util.ModbusUtil;
import com.ghgande.j2mod.modbus.util.SerialParameters;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is a factory class that allows users to easily create and manages slaves.<br>
 * Each slave is uniquely identified by the port it is listening on, irrespective of if
 * the socket type (TCP or UDP)
 *
 * @author Steve O'Hara (4energy)
 * @author Joe Montanari
 * @version 2.0 (March 2016)
 */
public class ModbusSlaveFactory {
    private static Map<String, AbstractModbusSlave<?>> slaves = new HashMap<String, AbstractModbusSlave<?>>();

    /**
     * Creates a TCP modbus slave or returns the one already allocated to this port
     *
     * @param port Port to listen on
     * @param poolSize Pool size of listener threads
     * @return new or existing TCP modbus slave associated with the port
     *
     * @throws ModbusException If a problem occurs e.g. port already in use
     */
    public static synchronized ModbusTCPSlave createTCPSlave(int port, int poolSize) throws ModbusException {
        return createTCPSlave(port, poolSize, false);
    }

    /**
     * Creates a TCP modbus slave or returns the one already allocated to this port
     *
     * @param port Port to listen on
     * @param poolSize Pool size of listener threads
     * @param useRtuOverTcp True if the RTU protocol should be used over TCP
     * @return new or existing TCP modbus slave associated with the port
     *
     * @throws ModbusException If a problem occurs e.g. port already in use
     */
    public static synchronized ModbusTCPSlave createTCPSlave(int port, int poolSize, boolean useRtuOverTcp) throws ModbusException {
        return createTCPSlave(null, port, poolSize, useRtuOverTcp);
    }

    /**
     * Creates a TCP modbus slave or returns the one already allocated to this port
     *
     * @param address IP address to listen on
     * @param port Port to listen on
     * @param poolSize Pool size of listener threads
     * @param useRtuOverTcp True if the RTU protocol should be used over TCP
     * @return new or existing TCP modbus slave associated with the port
     *
     * @throws ModbusException If a problem occurs e.g. port already in use
     */
    public static synchronized ModbusTCPSlave createTCPSlave(InetAddress address, int port, int poolSize, boolean useRtuOverTcp) throws ModbusException {
        return createTCPSlave(address, port, Executors.newFixedThreadPool(poolSize), useRtuOverTcp);
    }

    /**
     * Creates a TCP modbus slave or returns the one already allocated to this port
     *
     * @param address IP address to listen on
     * @param port Port to listen on
     * @param pool  Executor Service of listener threads.
     * @param useRtuOverTcp True if the RTU protocol should be used over TCP
     * @return new or existing TCP modbus slave associated with the port
     *
     * @throws ModbusException If a problem occurs e.g. port already in use
     */
    public static synchronized ModbusTCPSlave createTCPSlave(InetAddress address, int port, ExecutorService pool,
                                                             boolean useRtuOverTcp) throws ModbusException {
        address = standardizeAddress(address);

        String key = ModbusSlaveType.TCP + ":" + address + ":" + port;

        if (slaves.containsKey(key)) {
            return (ModbusTCPSlave) slaves.get(key);
        } else {
            ModbusTCPSlave slave = new ModbusTCPSlave(address, port, pool, useRtuOverTcp);
            slaves.put(key, slave);
            return slave;
        }
    }

    /**
     * Return the Modbus TCP slave at the Address:port
     * 
     * @param address IP address
     * @param port Port to listen on
     * @return ModbusTCP Slave if found
     */
    public static ModbusTCPSlave getTCPSlave(InetAddress address, int port) {
        address = standardizeAddress(address);

        String key = ModbusSlaveType.TCP + ":" + address + ":" + port;

        return (ModbusTCPSlave) slaves.get(key);
    }

    /**
     * Creates a UDP modbus slave or returns the one already allocated to this port
     *
     * @param port Port to listen on
     * @return new or existing UDP modbus slave associated with the port
     *
     * @throws ModbusException If a problem occurs e.g. port already in use
     */
    public static synchronized ModbusUDPSlave createUDPSlave(int port) throws ModbusException {
        return createUDPSlave(null, port);
    }

    /**
     * Creates a UDP modbus slave or returns the one already allocated to this port
     *
     * @param address IP address to listen on
     * @param port Port to listen on
     * @return new or existing UDP modbus slave associated with the port
     *
     * @throws ModbusException If a problem occurs e.g. port already in use
     */
    public static synchronized ModbusUDPSlave createUDPSlave(InetAddress address, int port) throws ModbusException {
        address = standardizeAddress(address);
        String key = ModbusSlaveType.UDP + ":" + address + ":" + port;
        if (slaves.containsKey(key)) {
            return (ModbusUDPSlave) slaves.get(key);
        } else {
            ModbusUDPSlave slave = new ModbusUDPSlave(address, port, null);
            slaves.put(key, slave);
            return slave;
        }
    }

    /**
     * Return the Modbus UDP slave at the Address:port
     * 
     * @param address IP address
     * @param port Port to listen on
     * @return ModbusUDP Slave if found
     */
    public static ModbusUDPSlave getUDPSlave(InetAddress address, int port) {
        address = standardizeAddress(address);

        String key = ModbusSlaveType.UDP + ":" + address + ":" + port;

        return (ModbusUDPSlave) slaves.get(key);
    }

    /**
     * Creates a serial modbus slave or returns the one already allocated to this port
     *
     * @param serialParams Serial parameters for serial type slaves
     * @return new or existing Serial modbus slave associated with the port
     *
     * @throws ModbusException If a problem occurs e.g. port already in use
     */
    public static synchronized ModbusSerialSlave createSerialSlave(SerialParameters serialParams) throws ModbusException {
        if (serialParams == null) {
            throw new ModbusException("Serial parameters are null");
        } else if (ModbusUtil.isBlank(serialParams.getPortName())) {
            throw new ModbusException("Serial port name is empty");
        }

        String key = ModbusSlaveType.SERIAL + ":" + serialParams.getPortName();
        if (slaves.containsKey(key)) {
            return (ModbusSerialSlave) slaves.get(serialParams.getPortName());
        } else {
            ModbusSerialSlave slave = new ModbusSerialSlave(serialParams);
            slaves.put(key, slave);
            return slave;
        }
    }

    /**
     * Return the Modbus Serial slave using the serialParameters
     * 
     * @param serialParams - Serial Parameters
     * @return ModbusSerial Slave if found;
     */
    public static ModbusSerialSlave getSerialSlave(SerialParameters serialParams) {
        String key = ModbusSlaveType.SERIAL + ":" + serialParams.getPortName();

        return (ModbusSerialSlave) slaves.get(key);
    }

    /**
     * Closes this slave and removes it from the running list
     *
     * @param slave Slave to remove
     */
    public static synchronized void close(AbstractModbusSlave<?> slave){
        if (slave != null) {
            slave.closeListener();
            if (slave instanceof ModbusTCPSlave) {
                ModbusTCPSlave tcpSlave = (ModbusTCPSlave) slave;
                slaves.remove(ModbusSlaveType.TCP + ":" + tcpSlave.getAddress() + ":" + tcpSlave.getPort());
            } else if (slave instanceof ModbusUDPSlave) {
                ModbusUDPSlave tcpSlave = (ModbusUDPSlave) slave;
                slaves.remove(ModbusSlaveType.UDP + ":" + tcpSlave.getAddress() + ":" + tcpSlave.getPort());
            }

            else if (slave instanceof ModbusSerialSlave) {
                ModbusSerialSlave tcpSlave = (ModbusSerialSlave) slave;
                slaves.remove(ModbusSlaveType.SERIAL + ":" + tcpSlave.getSerialParams().getPortName());
            }

        }
    }

    /**
     * Closes all slaves and removes them from the running list
     */
    public static synchronized void close() {
        for (AbstractModbusSlave<?> slave : new ArrayList<AbstractModbusSlave<?>>(slaves.values())) {
            slave.close();
        }
    }

    /**
     * Standardadize on the Inet address .IF null return 0.0.0.0
     * 
     * @param address - address
     * @return InetAddress
     */
    private static InetAddress standardizeAddress(InetAddress address) {
        try {
            address = address == null ? InetAddress.getByAddress(new byte[] { 0, 0, 0, 0 }) : address;
        } catch (UnknownHostException e) {
            // Can't happen -- size is fixed.
        }
        return address;
    }

}
