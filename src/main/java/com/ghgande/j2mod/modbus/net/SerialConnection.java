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

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.io.AbstractModbusTransport;
import com.ghgande.j2mod.modbus.io.ModbusASCIITransport;
import com.ghgande.j2mod.modbus.io.ModbusRTUTransport;
import com.ghgande.j2mod.modbus.io.ModbusSerialTransport;
import com.ghgande.j2mod.modbus.util.ModbusUtil;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.TreeSet;

/**
 * Class that implements a serial connection which can be used for master and
 * slave implementations.
 *
 * @author Dieter Wimberger
 * @author John Charlton
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class SerialConnection extends AbstractSerialConnection {

    private static final Logger logger = LoggerFactory.getLogger(SerialConnection.class);
    public static final int CONNECT_RETRY_DELAY = 100;
    public static final int CONNECT_RETRIES = 3;

    private SerialParameters parameters;
    private ModbusSerialTransport transport;
    private SerialPort serialPort;
    private InputStream inputStream;
    private int timeout = Modbus.DEFAULT_TIMEOUT;

    /**
     * Default constructor
     */
    public SerialConnection() {
    }

    /**
     * Creates a SerialConnection object and initializes variables passed in as
     * params.
     *
     * @param parameters A SerialParameters object.
     */
    public SerialConnection(SerialParameters parameters) {
        this.parameters = parameters;
    }

    /**
     * Returns a JSerialComm implementation for the given comms port
     *
     * @param commPort Comms port e.g. /dev/ttyAMA0
     * @return JSerialComm implementation
     */
    public static AbstractSerialConnection getCommPort(String commPort) {
        SerialConnection jSerialCommPort = new SerialConnection();
        jSerialCommPort.serialPort = SerialPort.getCommPort(commPort);
        return jSerialCommPort;
    }


    @Override
    public AbstractModbusTransport getModbusTransport() {
        return transport;
    }

    @Override
    public synchronized void open() throws IOException {
    	boolean reopen = serialPort != null;
    	
        if (serialPort == null) {
            serialPort = SerialPort.getCommPort(parameters.getPortName());
            if (serialPort.getDescriptivePortName().contains("Bad Port")) {
                close();
                throw new IOException(String.format("Port %s is not a valid name for a port on this platform", parameters.getPortName()));
            }
        }
        serialPort.closePort();
	    applyConnectionParameters();
	
	    if (!reopen) {
	        if (Modbus.SERIAL_ENCODING_ASCII.equals(parameters.getEncoding())) {
	            transport = new ModbusASCIITransport();
	        }
	        else if (Modbus.SERIAL_ENCODING_RTU.equals(parameters.getEncoding())) {
	            transport = new ModbusRTUTransport();
	        }
	        else {
	            transport = new ModbusRTUTransport();
	            logger.warn("Unknown transport encoding [{}] - reverting to RTU", parameters.getEncoding());
	        }
	        transport.setEcho(parameters.isEcho());
	        transport.setTimeout(timeout);
	
	        // Open the input and output streams for the connection. If they won't
	        // open, close the port before throwing an exception.
	        transport.setCommPort(this);
        }
        // Open the port so that we can get it's input stream
        int attempts = 0;
        while (!serialPort.openPort(parameters.getOpenDelay()) && attempts < CONNECT_RETRIES) {
            serialPort.closePort();
            ModbusUtil.sleep(CONNECT_RETRY_DELAY);
            attempts++;
            logger.debug("Retrying to open port [{}] - attempt [{}}", parameters.getPortName(), attempts);
        }
        if (!serialPort.isOpen()) {
            Set<String> ports = getCommPorts();
            StringBuilder portList = new StringBuilder("<NONE>");
            if (!ports.isEmpty()) {
                portList = new StringBuilder();
                for (String port : ports) {
                    portList.append(portList.length() == 0 ? "" : ",").append(port);
                }
            }
            throw new IOException(String.format("Port [%s] cannot be opened after [%d] attempts - valid ports are: [%s]", parameters.getPortName(), attempts, portList));
        }
        inputStream = serialPort.getInputStream();
        
        if (!reopen) {
	        serialPort.addDataListener(new SerialPortDataListener() {
	            @Override
	            public int getListeningEvents() {
	                return SerialPort.LISTENING_EVENT_PORT_DISCONNECTED;
	            }
	
	            @Override
	            public void serialEvent(SerialPortEvent event) {
	                if (event.getEventType() == SerialPort.LISTENING_EVENT_PORT_DISCONNECTED) {
	                    serialPort.closePort();
	                	transport.notifyListenersDisconnected();
	                }
	            }
	        });
        }
    }

    /**
     * Applies the serial parameters to the actual hardware
     */
    private void applyConnectionParameters() {

        // Set connection parameters, if set fails return parameters object to original state
        if (serialPort != null) {
            serialPort.setComPortParameters(parameters.getBaudRate(), parameters.getDatabits(), parameters.getStopbits(), parameters.getParity());
            serialPort.setFlowControl(parameters.getFlowControlIn() | parameters.getFlowControlOut());
            serialPort.setRs485ModeParameters(parameters.getRs485Mode(), parameters.getRs485TxEnableActiveHigh(), parameters.getRs485EnableTermination(), parameters.getRs485RxDuringTx(), parameters.getRs485DelayBeforeTxMicroseconds(), parameters.getRs485DelayAfterTxMicroseconds());
        }
    }

    @Override
    public synchronized void close() {

        // Check to make sure serial port has reference to avoid a NPE
        if (serialPort != null) {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            catch (IOException e) {
                logger.debug(e.getMessage());
            }
            finally {
                // Close the port.
                serialPort.closePort();
            }
        }
        serialPort = null;
    }

    @Override
    public synchronized boolean isOpen() {
        return serialPort != null && serialPort.isOpen();
    }

    @Override
    public synchronized int getTimeout() {
        return timeout;
    }

    @Override
    public synchronized void setTimeout(int timeout) {
        this.timeout = timeout;
        if (transport != null) {
            transport.setTimeout(timeout);
        }
    }

    @Override
    public int readBytes(byte[] buffer, int bytesToRead) {
        return serialPort == null ? 0 : serialPort.readBytes(buffer, bytesToRead);
    }

    @Override
    public int writeBytes(byte[] buffer, int bytesToWrite) {
        return serialPort == null ? 0 : serialPort.writeBytes(buffer, bytesToWrite);
    }

    @Override
    public int bytesAvailable() {
        return serialPort == null ? 0 : serialPort.bytesAvailable();
    }

    @Override
    public int getBaudRate() {
        return parameters.getBaudRate();
    }

    @Override
    public int getNumDataBits() {
        return parameters.getDatabits();
    }

    @Override
    public int getNumStopBits() {
        return parameters.getStopbits();
    }

    @Override
    public int getParity() {
        return parameters.getParity();
    }

    @Override
    public String getPortName() {
        return parameters.getPortName();
    }

    @Override
    public String getDescriptivePortName() {
        return serialPort == null ? parameters.getPortName() : serialPort.getDescriptivePortName();
    }

    @Override
    public void setComPortTimeouts(int newTimeoutMode, int newReadTimeout, int newWriteTimeout) {
        if (serialPort != null) {
            serialPort.setComPortTimeouts(newTimeoutMode, newReadTimeout, newWriteTimeout);
        }
    }

    @Override
    public Set<String> getCommPorts() {
        Set<String> returnValue = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        SerialPort[] ports = SerialPort.getCommPorts();
        if (ports != null && ports.length > 0) {
            for (SerialPort port : ports) {
                returnValue.add(port.getSystemPortName());
            }
        }
        return returnValue;
    }
}
