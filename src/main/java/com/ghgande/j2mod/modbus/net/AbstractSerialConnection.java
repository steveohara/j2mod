package com.ghgande.j2mod.modbus.net;

import com.fazecast.jSerialComm.SerialPort;
import com.ghgande.j2mod.modbus.io.AbstractModbusTransport;

import java.io.IOException;
import java.util.Set;

/**
 * Interface that represents a public abstract serial port connection
 *
 * @author Felipe Herranz
 * @version 2.0 (March 2016)
 */
public abstract class AbstractSerialConnection {

    /**
     * Parity values
     */
    public static final int NO_PARITY = SerialPort.NO_PARITY;
    public static final int ODD_PARITY = SerialPort.ODD_PARITY;
    public static final int EVEN_PARITY = SerialPort.EVEN_PARITY;
    public static final int MARK_PARITY = SerialPort.MARK_PARITY;
    public static final int SPACE_PARITY = SerialPort.SPACE_PARITY;

    /**
     * Stop bits values
     */
    public static final int ONE_STOP_BIT = SerialPort.ONE_STOP_BIT;
    public static final int ONE_POINT_FIVE_STOP_BITS = SerialPort.ONE_POINT_FIVE_STOP_BITS;
    public static final int TWO_STOP_BITS = SerialPort.TWO_STOP_BITS;

    /**
     * Flow control values
     */
    public static final int FLOW_CONTROL_DISABLED = SerialPort.FLOW_CONTROL_DISABLED;
    public static final int FLOW_CONTROL_RTS_ENABLED = SerialPort.FLOW_CONTROL_RTS_ENABLED;
    public static final int FLOW_CONTROL_CTS_ENABLED = SerialPort.FLOW_CONTROL_CTS_ENABLED;
    public static final int FLOW_CONTROL_DSR_ENABLED = SerialPort.FLOW_CONTROL_DSR_ENABLED;
    public static final int FLOW_CONTROL_DTR_ENABLED = SerialPort.FLOW_CONTROL_DTR_ENABLED;
    public static final int FLOW_CONTROL_XONXOFF_IN_ENABLED = SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED;
    public static final int FLOW_CONTROL_XONXOFF_OUT_ENABLED = SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED;

    /**
     * Open delay (msec)
     */
    public static final int OPEN_DELAY = 0;

    /**
     * Timeout
     */
    public static final int TIMEOUT_NONBLOCKING = SerialPort.TIMEOUT_NONBLOCKING;
   	public static final int TIMEOUT_READ_SEMI_BLOCKING = SerialPort.TIMEOUT_READ_SEMI_BLOCKING;
   	public static final int TIMEOUT_READ_BLOCKING = SerialPort.TIMEOUT_READ_BLOCKING;
   	public static final int TIMEOUT_WRITE_BLOCKING = SerialPort.TIMEOUT_WRITE_BLOCKING;
   	public static final int TIMEOUT_SCANNER = SerialPort.TIMEOUT_SCANNER;

    /**
     * Opens the port and throws an error if it cannot for some reason
     *
     * @throws IOException If the port is not available or cannot be opened
     */
    public abstract void open() throws IOException;

    /**
     * Returns the <tt>ModbusTransport</tt> instance to be used for receiving
     * and sending messages.
     *
     * @return a <tt>ModbusTransport</tt> instance
     */
    public abstract AbstractModbusTransport getModbusTransport();

    /**
     * Read a specified number of bytes from the serial port
     *
     * @param buffer      Buffer to recieve bytes from the port
     * @param bytesToRead Number of bytes to read
     * @return number of currently bytes read
     */
    public abstract int readBytes(byte[] buffer, int bytesToRead);

    /**
     * Write a specified number of bytes to the serial port
     *
     * @param buffer       Bytes to send to the port
     * @param bytesToWrite How many bytes to send
     * @return number of currently bytes written
     */
    public abstract int writeBytes(byte[] buffer, int bytesToWrite);

    /**
     * Bytes available to read
     *
     * @return number of bytes currently available to read
     */
    public abstract int bytesAvailable();

    /**
     * Close the port and clean up associated elements
     */
    public abstract void close();

    /**
     * Returns current baud rate
     *
     * @return Baud rate
     */
    public abstract int getBaudRate();

    /**
     * Returns current data bits value
     *
     * @return Number of data bits
     */
    public abstract int getNumDataBits();

    /**
     * Returns current stop bits
     *
     * @return Number of stop bits
     */
    public abstract int getNumStopBits();

    /**
     * Returns current parity
     *
     * @return Parity type
     */
    public abstract int getParity();

    /**
     * Returns a name of the port
     *
     * @return a <tt>String</tt> instance
     */
    public abstract String getPortName();

    /**
     * Returns a descriptive name of the port
     *
     * @return a <tt>String</tt> instance
     */
    public abstract String getDescriptivePortName();

    /**
     * Set port timeouts
     *
     * @param newTimeoutMode  Timeout mode
     * @param newReadTimeout  Read timeout (milliseconds)
     * @param newWriteTimeout Write timeout (milliseconds)
     */
    public abstract void setComPortTimeouts(int newTimeoutMode, int newReadTimeout, int newWriteTimeout);

    /**
     * Reports the open status of the port
     *
     * @return true if port is open, false if port is closed
     */
    public abstract boolean isOpen();

    /**
     * Returns the timeout for this connection
     *
     * @return the timeout as <tt>int</tt> milliseconds
     */
    public abstract int getTimeout();

    /**
     * Sets the timeout for this connection.
     *
     * @param timeout the timeout as <tt>int</tt> milliseconds
     */
    public abstract void setTimeout(int timeout);

    /**
     * Returns a set of all the available comm port names
     *
     * @return Set of comm port names
     */
    public abstract Set<String> getCommPorts();

}
