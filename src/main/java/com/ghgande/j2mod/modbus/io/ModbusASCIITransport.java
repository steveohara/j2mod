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
import com.ghgande.j2mod.modbus.util.ModbusUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Class that implements the Modbus/ASCII transport
 * flavor.
 *
 * @author Dieter Wimberger
 * @author John Charlton
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class ModbusASCIITransport extends ModbusSerialTransport {

    private static final Logger logger = LoggerFactory.getLogger(ModbusASCIITransport.class);
    private final byte[] inBuffer = new byte[Modbus.MAX_MESSAGE_LENGTH];
    private final BytesInputStream byteInputStream = new BytesInputStream(inBuffer);         //to read message from
    private final BytesOutputStream byteInputOutputStream = new BytesOutputStream(inBuffer);     //to buffer message to
    private final BytesOutputStream byteOutputStream = new BytesOutputStream(Modbus.MAX_MESSAGE_LENGTH);      //write frames

    /**
     * Constructs a new <tt>MobusASCIITransport</tt> instance.
     */
    public ModbusASCIITransport() {
    }

    @Override
    protected void writeMessageOut(ModbusMessage msg) throws ModbusIOException {
        try {
            synchronized (byteOutputStream) {
                //write message to byte out
                msg.setHeadless();
                msg.writeTo(byteOutputStream);
                byte[] buf = byteOutputStream.getBuffer();
                int len = byteOutputStream.size();

                //write message
                writeAsciiByte(FRAME_START);               //FRAMESTART
                writeAsciiBytes(buf, len);                 //PDU
                if (logger.isDebugEnabled()) {
                    logger.debug("Writing: {}", ModbusUtil.toHex(buf, 0, len));
                }
                writeAsciiByte(calculateLRC(buf, 0, len)); //LRC
                writeAsciiByte(FRAME_END);                 //FRAMEEND
                byteOutputStream.reset();
                // clears out the echoed message
                // for RS485
                if (echo) {
                    // read back the echoed message
                    readEcho(len + 3);
                }
            }
        }
        catch (IOException ex) {
            throw new ModbusIOException("I/O failed to write");
        }
    }

    @Override
    public ModbusRequest readRequestIn(AbstractModbusListener listener) throws ModbusIOException {
        boolean done = false;
        ModbusRequest request = null;
        int in;

        try {
            do {
                //1. Skip to FRAME_START
                while ((readAsciiByte()) != FRAME_START) {
                    // Nothing to do
                }

                //2. Read to FRAME_END
                synchronized (inBuffer) {
                    byteInputOutputStream.reset();
                    while ((in = readAsciiByte()) != FRAME_END) {
                        if (in == -1) {
                            throw new IOException("I/O exception - Serial port timeout");
                        }
                        byteInputOutputStream.writeByte(in);
                    }
                    //check LRC
                    if (inBuffer[byteInputOutputStream.size() - 1] != calculateLRC(inBuffer, 0, byteInputOutputStream.size(), 1)) {
                        continue;
                    }
                    byteInputStream.reset(inBuffer, byteInputOutputStream.size());
                    int unitID = byteInputStream.readUnsignedByte();

                    int functionCode = byteInputStream.readUnsignedByte();
                    //create request
                    request = ModbusRequest.createModbusRequest(functionCode);
                    request.setHeadless();
                    //read message
                    byteInputStream.reset(inBuffer, byteInputOutputStream.size());
                    request.readFrom(byteInputStream);
                }
                done = true;
            } while (!done);
            return request;
        }
        catch (Exception ex) {
            logger.debug(ex.getMessage());
            throw new ModbusIOException("I/O exception - failed to read");
        }

    }

    @Override
    protected ModbusResponse readResponseIn() throws ModbusIOException {
        boolean done = false;
        ModbusResponse response = null;
        int in;

        try {
            do {
                //1. Skip to FRAME_START
                while ((in = readAsciiByte()) != FRAME_START) {
                    if (in == -1) {
                        throw new IOException("I/O exception - Serial port timeout");
                    }
                }
                //2. Read to FRAME_END
                synchronized (inBuffer) {
                    byteInputOutputStream.reset();
                    while ((in = readAsciiByte()) != FRAME_END) {
                        if (in == -1) {
                            throw new IOException("I/O exception - Serial port timeout");
                        }
                        byteInputOutputStream.writeByte(in);
                    }
                    int len = byteInputOutputStream.size();
                    if (logger.isDebugEnabled()) {
                        logger.debug("Received: {}", ModbusUtil.toHex(inBuffer, 0, len));
                    }
                    //check LRC
                    if (inBuffer[len - 1] != calculateLRC(inBuffer, 0, len, 1)) {
                        continue;
                    }

                    byteInputStream.reset(inBuffer, byteInputOutputStream.size());
                    byteInputStream.readUnsignedByte();
                    // JDC: To check slave unit identifier in a response we need to know
                    // the slave id in the request.  This is not tracked since slaves
                    // only respond when a master request is made and there is only one
                    // master.  We are the only master, so we can assume that this
                    // response message is from the slave responding to the last request.
                    in = byteInputStream.readUnsignedByte();
                    //create request
                    response = ModbusResponse.createModbusResponse(in);
                    response.setHeadless();
                    //read message
                    byteInputStream.reset(inBuffer, byteInputOutputStream.size());
                    response.readFrom(byteInputStream);
                }
                done = true;
            } while (!done);
            return response;
        }
        catch (Exception ex) {
            logger.debug(ex.getMessage());
            throw new ModbusIOException("I/O exception - failed to read");
        }
    }

    /**
     * Calculates a LRC checksum
     *
     * @param data   Data to use
     * @param off    Offset into byte array
     * @param length Number of bytes to use
     * @return Checksum
     */
    private static int calculateLRC(byte[] data, int off, int length) {
        return calculateLRC(data, off, length, 0);
    }

    /**
     * Calculates a LRC checksum
     *
     * @param data     Data to use
     * @param off      Offset into byte array
     * @param length   Number of bytes to use
     * @param tailskip Bytes to skip at tail
     * @return Checksum
     */
    private static byte calculateLRC(byte[] data, int off, int length, int tailskip) {
        int lrc = 0;
        for (int i = off; i < length - tailskip; i++) {
            lrc += ((int) data[i]) & 0xFF;
        }
        return (byte) ((-lrc) & 0xff);
    }

}
