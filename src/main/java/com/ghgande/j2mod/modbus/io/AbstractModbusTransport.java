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
package com.ghgande.j2mod.modbus.io;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.msg.ModbusMessage;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.net.AbstractModbusListener;

import java.io.IOException;

/**
 * Interface defining the I/O mechanisms for
 * <tt>ModbusMessage</tt> instances.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public abstract class AbstractModbusTransport {

    protected int timeout = Modbus.DEFAULT_TIMEOUT;

    /**
     * Set the socket timeout
     *
     * @param time Timeout in milliseconds
     */
    public void setTimeout(int time) {
        timeout = time;
    }

    /**
     * Closes the raw input and output streams of
     * this <tt>ModbusTransport</tt>.
     * <p>
     *
     * @throws IOException if a stream
     *                     cannot be closed properly.
     */
    public abstract void close() throws IOException;

    /**
     * Creates a Modbus transaction for the underlying transport.
     *
     * @return the new transaction
     */
    public abstract ModbusTransaction createTransaction();

    /**
     * Writes a <tt>ModbusMessage</tt> to the
     * output stream of this <tt>ModbusTransport</tt>.
     * <p>
     *
     * @param msg a <tt>ModbusMessage</tt>.
     *
     * @throws ModbusIOException data cannot be
     *                           written properly to the raw output stream of
     *                           this <tt>ModbusTransport</tt>.
     */
    public abstract void writeMessage(ModbusMessage msg) throws ModbusIOException;

    /**
     * Reads a <tt>ModbusRequest</tt> from the
     * input stream of this <tt>ModbusTransport</tt>.
     * <p>
     *
     * @param listener Listener the request was received by
     *
     * @return req the <tt>ModbusRequest</tt> read from the underlying stream.
     *
     * @throws ModbusIOException data cannot be
     *                           read properly from the raw input stream of
     *                           this <tt>ModbusTransport</tt>.
     */
    public abstract ModbusRequest readRequest(AbstractModbusListener listener) throws ModbusIOException;

    /**
     * Reads a <tt>ModbusResponse</tt> from the
     * input stream of this <tt>ModbusTransport</tt>.
     * <p>
     *
     * @return res the <tt>ModbusResponse</tt> read from the underlying stream.
     *
     * @throws ModbusIOException data cannot be
     *                           read properly from the raw input stream of
     *                           this <tt>ModbusTransport</tt>.
     */
    public abstract ModbusResponse readResponse() throws ModbusIOException;

}