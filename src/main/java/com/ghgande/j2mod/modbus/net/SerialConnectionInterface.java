package com.ghgande.j2mod.modbus.net;

import com.ghgande.j2mod.modbus.io.AbstractModbusTransport;

/**
 * Interface that represents an public abstract serial port connection
 *
 * @author Felipe Herranz
 * @version 2.0 (March 2016)
 */
public abstract class SerialConnectionInterface {

    /**
     * Parity values
     */
    public static final int NO_PARITY = 0;
    public static final int ODD_PARITY = 1;
    public static final int EVEN_PARITY = 2;
    public static final int MARK_PARITY = 3;
    public static final int SPACE_PARITY = 4;

    /**
     *  Stop bits values
     */
    public static final int ONE_STOP_BIT = 1;
    public static final int ONE_POINT_FIVE_STOP_BITS = 2;
    public static final int TWO_STOP_BITS = 3;

    /**
     *  Flow control values
     */
    public static final int FLOW_CONTROL_DISABLED = 0;
    public static final int FLOW_CONTROL_RTS_ENABLED = 1;
    public static final int FLOW_CONTROL_CTS_ENABLED = 16;
    public static final int FLOW_CONTROL_DSR_ENABLED = 256;
    public static final int FLOW_CONTROL_DTR_ENABLED = 4096;
    public static final int FLOW_CONTROL_XONXOFF_IN_ENABLED = 65536;
    public static final int FLOW_CONTROL_XONXOFF_OUT_ENABLED = 1048576;

    /**
     * Timeout
     */
    public static final int TIMEOUT_READ_BLOCKING = 256;


    /**
     * Returns the <tt>ModbusTransport</tt> instance to be used for receiving
     * and sending messages.
     *
     * @return a <tt>ModbusTransport</tt> instance.
     */
    public abstract boolean open();

    /**
     * Returns the <tt>ModbusTransport</tt> instance to be used for receiving
     * and sending messages.
     *
     * @return a <tt>ModbusTransport</tt> instance.
     */
    public abstract AbstractModbusTransport getModbusTransport();

    /**
     * Read a specified number of bytes from the serial bytes.
     * @param buffer
     * @param bytesToRead
     * @return number of currently bytes read.
     */
    public abstract int readBytes(byte[] buffer, long bytesToRead);

    /**
     * Write a specified number of bytes to the serial port
     * @param buffer
     * @param bytesToWrite
     * @return number of currently bytes written
     */
    public abstract int writeBytes(byte[] buffer, long bytesToWrite);

    /**
     * Bytes available to read
     * @return number of bytes currently available to read
     */
    public abstract int bytesAvailable();

    /**
     * Sets the connection parameters to the setting in the parameters object.
     * If set fails return the parameters object to original settings and throw
     * exception.
     */
    public abstract void setConnectionParameters();

    /**
     * Close the port and clean up associated elements.
     */
    public abstract void close();

    /**
     * Returns current baud rate
     * @return
     */
    public abstract int getBaudRate();

    /**
     * Set new baud rate
     * @param newBaudRate
     */
    public abstract void setBaudRate(int newBaudRate);

    /**
     * Returns current data bits value
     * @return
     */
    public abstract int getNumDataBits();

    /**
     * Returns current stop bits
     * @return
     */
    public abstract int getNumStopBits();

    /**
     * Returns current parity
     * @return
     */
    public abstract int getParity();

    /**
     * Returns a descriptive name of the current port
     * @return a <tt>String</tt> instance.
     */
    public abstract String getDescriptivePortName();

    /**
     * Set port timeouts
     * @param newTimeoutMode
     * @param newReadTimeout
     * @param newWriteTimeout
     */
    public abstract void setComPortTimeouts(int newTimeoutMode, int newReadTimeout, int newWriteTimeout);

    /**
     * Reports the open status of the port.
     *
     * @return true if port is open, false if port is closed.
     */
    public abstract boolean isOpen();

    /**
     * Returns the timeout for this <tt>UDPMasterConnection</tt>.
     *
     * @return the timeout as <tt>int</tt>.
     */
    public abstract int getTimeout();

    /**
     * Sets the timeout for this <tt>UDPMasterConnection</tt>.
     *
     * @param timeout the timeout as <tt>int</tt>.
     */
    public abstract void setTimeout(int timeout);

}
