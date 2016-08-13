package com.ghgande.j2mod.modbus.net;

import com.ghgande.j2mod.modbus.io.AbstractModbusTransport;

/**
 * Interface that represents an abstract serial port connection
 *
 * @author Felipe Herranz
 * @version 2.0 (March 2016)
 */
public interface SerialConnection {
    /**
     * Returns the <tt>ModbusTransport</tt> instance to be used for receiving
     * and sending messages.
     *
     * @return a <tt>ModbusTransport</tt> instance.
     */
    void open() throws Exception;

    /**
     * Returns the <tt>ModbusTransport</tt> instance to be used for receiving
     * and sending messages.
     *
     * @return a <tt>ModbusTransport</tt> instance.
     */
    AbstractModbusTransport getModbusTransport();

    /**
     * Sets the connection parameters to the setting in the parameters object.
     * If set fails return the parameters object to original settings and throw
     * exception.
     */
    void setConnectionParameters();

    /**
     * Close the port and clean up associated elements.
     */
    void close();

    /**
     * Reports the open status of the port.
     *
     * @return true if port is open, false if port is closed.
     */
    boolean isOpen();

    /**
     * Returns the timeout for this <tt>UDPMasterConnection</tt>.
     *
     * @return the timeout as <tt>int</tt>.
     */
    int getTimeout();

    /**
     * Sets the timeout for this <tt>UDPMasterConnection</tt>.
     *
     * @param timeout the timeout as <tt>int</tt>.
     */
    void setTimeout(int timeout);

}
