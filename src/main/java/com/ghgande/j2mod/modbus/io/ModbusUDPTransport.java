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
import com.ghgande.j2mod.modbus.net.AbstractUDPTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Arrays;

/**
 * Class that implements the Modbus UDP transport
 * flavor.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public class ModbusUDPTransport extends AbstractModbusTransport {

    private static final Logger logger = LoggerFactory.getLogger(ModbusUDPTransport.class);

    //instance attributes
    private AbstractUDPTerminal terminal;
    private final BytesOutputStream byteOutputStream = new BytesOutputStream(Modbus.MAX_MESSAGE_LENGTH);
    private final BytesInputStream byteInputStream = new BytesInputStream(Modbus.MAX_MESSAGE_LENGTH);

    /**
     * Constructs a new <tt>ModbusTransport</tt> instance,
     * for a given <tt>UDPTerminal</tt>.
     * <p>
     *
     * @param terminal the <tt>UDPTerminal</tt> used for message transport.
     */
    public ModbusUDPTransport(AbstractUDPTerminal terminal) {
        this.terminal = terminal;
    }

    @Override
    public void setTimeout(int time) {
        super.setTimeout(time);
        if (terminal != null) {
            terminal.setTimeout(timeout);
        }
    }

    @Override
    public void close() throws IOException {
        //?
    }

    @Override
    public ModbusTransaction createTransaction() {
        ModbusUDPTransaction trans = new ModbusUDPTransaction();
        trans.setTerminal(terminal);
        return trans;
    }

    @Override
    public void writeMessage(ModbusMessage msg) throws ModbusIOException {
        try {
            synchronized (byteOutputStream) {
                int len = msg.getOutputLength();
                byteOutputStream.reset();
                msg.writeTo(byteOutputStream);
                byte data[] = byteOutputStream.getBuffer();
                data = Arrays.copyOf(data, len);
                terminal.sendMessage(data);
            }
        }
        catch (Exception ex) {
            throw new ModbusIOException("I/O exception - failed to write", ex);
        }
    }

    @Override
    public ModbusRequest readRequest(AbstractModbusListener listener) throws ModbusIOException {
        try {
            ModbusRequest req;
            synchronized (byteInputStream) {
                byteInputStream.reset(terminal.receiveMessage());
                byteInputStream.skip(7);
                int functionCode = byteInputStream.readUnsignedByte();
                byteInputStream.reset();
                req = ModbusRequest.createModbusRequest(functionCode);
                req.readFrom(byteInputStream);
            }
            return req;
        }
        catch (Exception ex) {
            throw new ModbusIOException("I/O exception - failed to read", ex);
        }
    }

    @Override
    public ModbusResponse readResponse() throws ModbusIOException {

        try {
            ModbusResponse res;
            synchronized (byteInputStream) {
                byteInputStream.reset(terminal.receiveMessage());
                byteInputStream.skip(7);
                int functionCode = byteInputStream.readUnsignedByte();
                byteInputStream.reset();
                res = ModbusResponse.createModbusResponse(functionCode);
                res.readFrom(byteInputStream);
            }
            return res;
        }
        catch (InterruptedIOException ioex) {
            throw new ModbusIOException("Socket was interrupted", ioex);
        }
        catch (Exception ex) {
            logger.debug("I/O exception while reading modbus response.", ex);
            throw new ModbusIOException("I/O exception - failed to read - %s", ex.getMessage());
        }
    }

}