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
import com.ghgande.j2mod.modbus.net.ModbusSerialListener;
import com.ghgande.j2mod.modbus.net.ModbusTCPListener;
import com.ghgande.j2mod.modbus.net.ModbusUDPListener;
import com.ghgande.j2mod.modbus.procimg.ProcessImage;
import com.ghgande.j2mod.modbus.util.ModbusUtil;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that implements a wrapper around a Slave Listener
 *
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class ModbusSlave {

    private static final Logger logger = LoggerFactory.getLogger(ModbusSlave.class);

    private final ModbusSlaveType type;
    private final int port;
    private final SerialParameters serialParams;
    private final AbstractModbusListener listener;
    private boolean isRunning;
    private Thread listenerThread;

    private final Map<Integer, ProcessImage> processImages = new HashMap<Integer, ProcessImage>();

    /**
     * Creates a TCP modbus slave
     *
     * @param port          Port to listen on if IP type
     * @param poolSize      Pool size for TCP slaves
     * @param useRtuOverTcp True if the RTU protocol should be used over TCP
     * @throws ModbusException If a problem occurs e.g. port already in use
     */
    protected ModbusSlave(int port, int poolSize, boolean useRtuOverTcp) throws ModbusException {
        this(ModbusSlaveType.TCP, null, port, poolSize, null, useRtuOverTcp, 0);
    }

    /**
     * Creates a TCP modbus slave
     *
     * @param address       IP address to listen on
     * @param port          Port to listen on if IP type
     * @param poolSize      Pool size for TCP slaves
     * @param useRtuOverTcp True if the RTU protocol should be used over TCP
     * @throws ModbusException If a problem occurs e.g. port already in use
     */
    protected ModbusSlave(InetAddress address, int port, int poolSize, boolean useRtuOverTcp, int maxIdleSeconds) throws ModbusException {
        this(ModbusSlaveType.TCP, address, port, poolSize, null, useRtuOverTcp, maxIdleSeconds);
    }

    /**
     * Creates a UDP modbus slave
     *
     * @param port          Port to listen on if IP type
     * @param useRtuOverTcp True if the RTU protocol should be used over TCP
     * @throws ModbusException If a problem occurs e.g. port already in use
     */
    protected ModbusSlave(int port, boolean useRtuOverTcp) throws ModbusException {
        this(ModbusSlaveType.UDP, null, port, 0, null, useRtuOverTcp, 0);
    }

    /**
     * Creates a UDP modbus slave
     *
     * @param address       IP address to listen on
     * @param port          Port to listen on if IP type
     * @param useRtuOverTcp True if the RTU protocol should be used over TCP
     * @throws ModbusException If a problem occurs e.g. port already in use
     */
    protected ModbusSlave(InetAddress address, int port, boolean useRtuOverTcp) throws ModbusException {
        this(ModbusSlaveType.UDP, address, port, 0, null, useRtuOverTcp, 0);
    }

    /**
     * Creates a serial modbus slave
     *
     * @param serialParams Serial parameters for serial type slaves
     * @throws ModbusException If a problem occurs e.g. port already in use
     */
    protected ModbusSlave(SerialParameters serialParams) throws ModbusException {
        this(ModbusSlaveType.SERIAL, null, 0, 0, serialParams, false, 0);
    }

    /**
     * Creates an appropriate type of listener
     *
     * @param type           Type of slave to create
     * @param address        IP address to listen on
     * @param port           Port to listen on if IP type
     * @param poolSize       Pool size for TCP slaves
     * @param serialParams   Serial parameters for serial type slaves
     * @param useRtuOverTcp  True if the RTU protocol should be used over TCP
     * @param maxIdleSeconds Maximum idle seconds for TCP connection
     */
    private ModbusSlave(ModbusSlaveType type, InetAddress address, int port, int poolSize, SerialParameters serialParams, boolean useRtuOverTcp, int maxIdleSeconds) {
        this.type = type == null ? ModbusSlaveType.TCP : type;
        this.port = port;
        this.serialParams = serialParams;

        // Create the listener

        logger.debug("Creating {} listener", this.type);
        if (this.type.is(ModbusSlaveType.UDP)) {
            listener = new ModbusUDPListener();
        }
        else if (this.type.is(ModbusSlaveType.TCP)) {
            ModbusTCPListener tcpListener = new ModbusTCPListener(poolSize, useRtuOverTcp);
            tcpListener.setMaxIdleSeconds(maxIdleSeconds);
            listener = tcpListener;
        }
        else {
            listener = new ModbusSerialListener(serialParams);
        }

        listener.setAddress(address);
        listener.setPort(port);
        listener.setTimeout(0);
    }

    /**
     * Returns the type of this slave
     *
     * @return Type of slave
     */
    public ModbusSlaveType getType() {
        return type;
    }

    /**
     * Returns the port that this IP slave is listening on
     *
     * @return Port being listened on if TCP or UDP
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the process image for the given Unit ID
     *
     * @param unitId Unit ID of the associated image
     * @return Process image
     */
    public ProcessImage getProcessImage(int unitId) {
        return processImages.get(unitId);
    }

    /**
     * Removes the process image for the given Unit ID
     *
     * @param unitId Unit ID of the associated image
     * @return Process image
     */
    public ProcessImage removeProcessImage(int unitId) {
        return processImages.remove(unitId);
    }

    /**
     * Adds a process image for the given Unit ID
     *
     * @param unitId       Unit ID to associate with this image
     * @param processImage Process image to add
     * @return Process image
     */
    public ProcessImage addProcessImage(int unitId, ProcessImage processImage) {
        return processImages.put(unitId, processImage);
    }

    /**
     * Returns the serial parameters of this slave if it is a Serial type
     *
     * @return Serial parameters
     */
    public SerialParameters getSerialParams() {
        return serialParams;
    }

    /**
     * Opens the listener to service requests
     *
     * @throws ModbusException If we cannot listen
     */
    public void open() throws ModbusException {

        // Start the listener if it isn't already running
        if (!isRunning) {
            try {
                listenerThread = new Thread(listener);
                listenerThread.start();

                // Need to check that there isn't an issue with the port or some other reason why we can't
                // actually start listening
                while (!listener.isListening() && listener.getError() == null) {
                    ModbusUtil.sleep(50);
                }
                if (!listener.isListening()) {
                    throw new ModbusException(listener.getError());
                }
                isRunning = true;
            }
            catch (Exception x) {
                closeListener();
                throw new ModbusException(x.getMessage());
            }
        }
    }

    /**
     * Convenience method for closing this port and removing it from the running list - simply
     * calls ModbusSlaveFactory.close(this)
     */
    public void close() {
        ModbusSlaveFactory.close(this);
    }

    /**
     * Returns the last error accrued by the listener
     *
     * @return Error if there is one
     */
    public String getError() {
        return listener != null ? listener.getError() : null;
    }

    /**
     * Returns the listener used for this port
     *
     * @return Listener
     */
    protected AbstractModbusListener getListener() {
        return listener;
    }

    /**
     * Closes the listener of this slave
     */
    @SuppressWarnings("deprecation")
    void closeListener() {
        if (listener != null && listener.isListening()) {
            listener.stop();

            // Wait until the listener says it has stopped, but don't wait forever
            int count = 0;
            while (listenerThread != null && listenerThread.isAlive() && count < 50) {
                ModbusUtil.sleep(100);
                count++;
            }
            // If the listener is still not stopped, kill the thread
            if (listenerThread != null && listenerThread.isAlive()) {
                listenerThread.stop();
            }
            listenerThread = null;
        }
        isRunning = false;
    }

    /**
     * Gets the name of the thread used by the listener
     *
     * @return Name of thread or null if not assigned
     */
    public String getThreadName() {
        return listener == null ? null : listener.getThreadName();
    }

    /**
     * Sets the name of the thread used by the listener
     *
     * @param threadName Name to use for the thread
     */
    public void setThreadName(String threadName) {
        if (listener != null) {
            listener.setThreadName(threadName);
        }
    }
}
