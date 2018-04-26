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
import com.ghgande.j2mod.modbus.ModbusCoupler;
import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.io.AbstractModbusTransport;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.procimg.ProcessImage;
import com.ghgande.j2mod.modbus.slave.ModbusSlave;
import com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

/**
 * Definition of a listener class
 *
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public abstract class AbstractModbusListener implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(AbstractModbusListener.class);
    protected int port = Modbus.DEFAULT_PORT;
    protected boolean listening;
    protected InetAddress address;
    protected String error;
    protected int timeout = Modbus.DEFAULT_TIMEOUT;

    /**
     * Main execution loop for this Modbus interface listener - this is called by
     * starting the main listening thread
     */
    public abstract void run();

    /**
     * Stop the listener thread for this <tt>ModbusListener</tt> instance.
     */
    public abstract void stop();

    /**
     * Sets the port to be listened to.
     *
     * @param port the number of the IP port as <tt>int</tt>.
     */
    public void setPort(int port) {
        this.port = ((port > 0) ? port : Modbus.DEFAULT_PORT);
    }

    /**
     * Returns the port being listened on
     *
     * @return Port number &gt; 0
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the address of the interface to be listened to.
     *
     * @param addr an <tt>InetAddress</tt> instance.
     */
    public void setAddress(InetAddress addr) {
        address = addr;
    }

    /**
     * Returns the address bound to this socket
     *
     * @return Bound address
     */
    public InetAddress getAddress() {
        return address;
    }

    /**
     * Tests if this <tt>ModbusTCPListener</tt> is listening and accepting
     * incoming connections.
     *
     * @return true if listening (and accepting incoming connections), false
     * otherwise.
     */
    public boolean isListening() {
        return listening;
    }

    /**
     * Set the listening state of this <tt>ModbusTCPListener</tt> object.
     * A <tt>ModbusTCPListener</tt> will silently drop any requests if the
     * listening state is set to <tt>false</tt>.
     *
     * @param b listening state
     */
    public void setListening(boolean b) {
        listening = b;
    }

    /**
     * Returns any startup errors that may have aoccurred
     *
     * @return Error string
     */
    public String getError() {
        return error;
    }

    /**
     * Get the socket timeout
     *
     * @return Socket timeout in milliseconds
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Sets the socket timeout
     *
     * @param timeout Timeout in milliseconds
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * Reads the request, checks it is valid and that the unit ID is ok
     * and sends back a response
     *
     * @param transport Transport to read request from
     * @param listener Listener that the request was received by
     * @throws ModbusIOException If there is an issue with the transport or transmission
     */
    protected void handleRequest(AbstractModbusTransport transport, AbstractModbusListener listener) throws ModbusIOException {

        // Get the request from the transport. It will be processed
        // using an associated process image

        if (transport == null) {
            throw new ModbusIOException("No transport specified");
        }
        ModbusRequest request = transport.readRequest(listener);
        if (request == null) {
            throw new ModbusIOException("Request for transport %s is invalid (null)", transport.getClass().getSimpleName());
        }
        ModbusResponse response;

        // Test if Process image exists and has a correct unit ID
        ProcessImage spi = getProcessImage(request.getUnitID());
        if (spi == null || (spi.getUnitID() != 0 && request.getUnitID() != spi.getUnitID())) {
            response = request.createExceptionResponse(Modbus.ILLEGAL_ADDRESS_EXCEPTION);

            // The message is not for us. Read the expected response.
            if (logger.isDebugEnabled()) {
                logger.debug("Message not for us! Request: {}", request.getHexMessage());
            }

            ModbusResponse resp = transport.readResponse();

            if (logger.isDebugEnabled()) {
                logger.debug("Read slave! Response: {}", resp.getHexMessage());
            }
        } else {
            response = request.createResponse(this);

            if (logger.isDebugEnabled()) {
                logger.debug("Request:{}", request.getHexMessage());
                logger.debug("Response:{}", response.getHexMessage());
            }

            // Write the response
            transport.writeMessage(response);
        }
    }

    /**
     * Returns the related process image for this listener and Unit Id
     *
     * @param unitId Unit ID
     * @return Process image associated with this listener and Unit ID
     */
    public ProcessImage getProcessImage(int unitId) {
        ModbusSlave slave = ModbusSlaveFactory.getSlave(this);
        if (slave != null) {
            return slave.getProcessImage(unitId);
        }
        else {

            // Legacy: Use the ModbusCoupler if no image was associated with the listener
            //         This will be removed when the ModbusCoupler is removed

            return ModbusCoupler.getReference().getProcessImage(unitId);
        }
    }

}
