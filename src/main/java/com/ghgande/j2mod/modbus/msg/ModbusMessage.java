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
package com.ghgande.j2mod.modbus.msg;

import com.ghgande.j2mod.modbus.io.Transportable;

/**
 * Interface defining a ModbusMessage.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public interface ModbusMessage extends Transportable {

    /**
     * Check the flag which indicates that this <tt>ModbusMessage</tt> is for a
     * headless (serial, or headless networked) connection.
     * @return is for a headless (serial, or headless networked) connection
     */
    boolean isHeadless();

    /**
     * Sets the flag that marks this <tt>ModbusMessage</tt> as headless (for
     * serial transport).
     */
    void setHeadless();

    /**
     * Returns the transaction identifier of this <tt>ModbusMessage</tt> as
     * <tt>int</tt>.
     *
     * <p>
     * The identifier is a 2-byte (short) non negative integer value valid in
     * the range of 0-65535.
     *
     * @return the transaction identifier as <tt>int</tt>.
     */
    int getTransactionID();

    /**
     * Returns the protocol identifier of this <tt>ModbusMessage</tt> as
     * <tt>int</tt>.
     *
     * <p>
     * The identifier is a 2-byte (short) non negative integer value valid in
     * the range of 0-65535.
     *
     * @return the protocol identifier as <tt>int</tt>.
     */
    int getProtocolID();

    /**
     * Returns the length of the data appended after the protocol header.
     * <p>
     *
     * @return the data length as <tt>int</tt>.
     */
    int getDataLength();

    /**
     * Returns the unit identifier of this <tt>ModbusMessage</tt> as
     * <tt>int</tt>.
     *
     * <p>
     * The identifier is a 1-byte non negative integer value valid in the range
     * of 0-255.
     *
     * @return the unit identifier as <tt>int</tt>.
     */
    int getUnitID();

    /**
     * Returns the function code of this <tt>ModbusMessage</tt> as <tt>int</tt>.<br>
     * The function code is a 1-byte non negative integer value valid in the
     * range of 0-127.
     *
     * <p>
     * Function codes are ordered in conformance classes their values are
     * specified in <tt>com.ghgande.j2mod.modbus.Modbus</tt>.
     *
     * @return the function code as <tt>int</tt>.
     *
     * @see com.ghgande.j2mod.modbus.Modbus
     */
    int getFunctionCode();

    /**
     * Returns the <i>raw</i> message as an array of bytes.
     * <p>
     *
     * @return the <i>raw</i> message as <tt>byte[]</tt>.
     */
    byte[] getMessage();

    /**
     * Returns the <i>raw</i> message as <tt>String</tt> containing a
     * hexadecimal series of bytes.
     *
     * <p>
     * This method is specially for debugging purposes, allowing the user to log
     * the communication in a manner used in the specification document.
     *
     * @return the <i>raw</i> message as <tt>String</tt> containing a
     * hexadecimal series of bytes.
     */
    String getHexMessage();
}
