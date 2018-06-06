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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.msg.ModbusMessage;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.net.AbstractModbusListener;
import com.ghgande.j2mod.modbus.util.ModbusUtil;

/**
 * Class that implements the ModbusRTU transport flavor.
 *
 * @author John Charlton
 * @author Dieter Wimberger
 * @author Julie Haugh
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class ModbusRTUTransport extends ModbusSerialTransport {

    private static final Logger logger = LoggerFactory.getLogger(ModbusRTUTransport.class);

    private final byte[] inBuffer = new byte[Modbus.MAX_MESSAGE_LENGTH];
    private final BytesInputStream byteInputStream = new BytesInputStream(inBuffer); // to read message from
    private final BytesOutputStream byteInputOutputStream = new BytesOutputStream(inBuffer); // to buffer message to
    private final BytesOutputStream byteOutputStream = new BytesOutputStream(Modbus.MAX_MESSAGE_LENGTH); // write frames
    private byte[] lastRequest = null;

    /**
     * Read the data for a request of a given fixed size
     *
     * @param byteCount Byte count excluding the 2 byte CRC
     * @param out       Output buffer to populate
     *
     * @throws IOException If data cannot be read from the port
     */
    private void readRequestData(int byteCount, BytesOutputStream out) throws IOException {
        byteCount += 2;
        byte inpBuf[] = new byte[byteCount];
        readBytes(inpBuf, byteCount);
        out.write(inpBuf, 0, byteCount);
    }

    /**
     * getRequest - Read a request, after the unit and function code
     *
     * @param function - Modbus function code
     * @param out      - Byte stream buffer to hold actual message
     */
    private void getRequest(int function, BytesOutputStream out) throws IOException {
        int byteCount;
        byte inpBuf[] = new byte[256];
        try {
            if ((function & 0x80) == 0) {
                switch (function) {
                    case Modbus.READ_EXCEPTION_STATUS:
                    case Modbus.READ_COMM_EVENT_COUNTER:
                    case Modbus.READ_COMM_EVENT_LOG:
                    case Modbus.REPORT_SLAVE_ID:
                        readRequestData(0, out);
                        break;

                    case Modbus.READ_FIFO_QUEUE:
                        readRequestData(2, out);
                        break;

                    case Modbus.READ_MEI:
                        readRequestData(3, out);
                        break;

                    case Modbus.READ_COILS:
                    case Modbus.READ_INPUT_DISCRETES:
                    case Modbus.READ_MULTIPLE_REGISTERS:
                    case Modbus.READ_INPUT_REGISTERS:
                    case Modbus.WRITE_COIL:
                    case Modbus.WRITE_SINGLE_REGISTER:
                        readRequestData(4, out);
                        break;
                    case Modbus.MASK_WRITE_REGISTER:
                        readRequestData(6, out);
                        break;

                    case Modbus.READ_FILE_RECORD:
                    case Modbus.WRITE_FILE_RECORD:
                        byteCount = readByte();
                        out.write(byteCount);
                        readRequestData(byteCount, out);
                        break;

                    case Modbus.WRITE_MULTIPLE_COILS:
                    case Modbus.WRITE_MULTIPLE_REGISTERS:
                        readBytes(inpBuf, 4);
                        out.write(inpBuf, 0, 4);
                        byteCount = readByte();
                        out.write(byteCount);
                        readRequestData(byteCount, out);
                        break;

                    case Modbus.READ_WRITE_MULTIPLE:
                        readRequestData(8, out);
                        byteCount = readByte();
                        out.write(byteCount);
                        readRequestData(byteCount, out);
                        break;

                    default:
                        throw new IOException(String.format("getResponse unrecognised function code [%s]", function));
                }
            }
        }
        catch (IOException e) {
            throw new IOException("getResponse serial port exception");
        }
    }

    /**
     * getResponse - Read a <tt>ModbusResponse</tt> from a slave.
     *
     * @param function The function code of the request
     * @param out      The output buffer to put the result
     *
     * @throws IOException If data cannot be read from the port
     */
    private void getResponse(int function, BytesOutputStream out) throws IOException {
        byte inpBuf[] = new byte[256];
        try {
            if ((function & 0x80) == 0) {
                switch (function) {
                    case Modbus.READ_COILS:
                    case Modbus.READ_INPUT_DISCRETES:
                    case Modbus.READ_MULTIPLE_REGISTERS:
                    case Modbus.READ_INPUT_REGISTERS:
                    case Modbus.READ_COMM_EVENT_LOG:
                    case Modbus.REPORT_SLAVE_ID:
                    case Modbus.READ_FILE_RECORD:
                    case Modbus.WRITE_FILE_RECORD:
                    case Modbus.READ_WRITE_MULTIPLE:
                        // Read the data payload byte count. There will be two
                        // additional CRC bytes afterwards.
                        int cnt = readByte();
                        out.write(cnt);
                        readRequestData(cnt, out);
                        break;

                    case Modbus.WRITE_COIL:
                    case Modbus.WRITE_SINGLE_REGISTER:
                    case Modbus.READ_COMM_EVENT_COUNTER:
                    case Modbus.WRITE_MULTIPLE_COILS:
                    case Modbus.WRITE_MULTIPLE_REGISTERS:
                    case Modbus.READ_SERIAL_DIAGNOSTICS:
                        // read status: only the CRC remains after the two data
                        // words.
                        readRequestData(4, out);
                        break;

                    case Modbus.READ_EXCEPTION_STATUS:
                        // read status: only the CRC remains after exception status
                        // byte.
                        readRequestData(1, out);
                        break;

                    case Modbus.MASK_WRITE_REGISTER:
                        // eight bytes in addition to the address and function codes
                        readRequestData(6, out);
                        break;

                    case Modbus.READ_FIFO_QUEUE:
                        int b1, b2;
                        b1 = (byte)(readByte() & 0xFF);
                        out.write(b1);
                        b2 = (byte)(readByte() & 0xFF);
                        out.write(b2);
                        int byteCount = ModbusUtil.makeWord(b1, b2);
                        readRequestData(byteCount, out);
                        break;

                    case Modbus.READ_MEI:
                        // read the subcode. We only support 0x0e.
                        int sc = readByte();
                        if (sc != 0x0e) {
                            throw new IOException("Invalid subfunction code");
                        }
                        out.write(sc);
                        // next few bytes are just copied.
                        int id, fieldCount;
                        readBytes(inpBuf, 5);
                        out.write(inpBuf, 0, 5);
                        fieldCount = (int)inpBuf[4];
                        for (int i = 0; i < fieldCount; i++) {
                            id = readByte();
                            out.write(id);
                            int len = readByte();
                            out.write(len);
                            readBytes(inpBuf, len);
                            out.write(inpBuf, 0, len);
                        }
                        if (fieldCount == 0) {
                            int err = readByte();
                            out.write(err);
                        }
                        // now get the 2 CRC bytes
                        readRequestData(0, out);
                        break;

                    default:
                        throw new IOException(String.format("getResponse unrecognised function code [%s]", function));

                }
            }
            else {
                // read the exception code, plus two CRC bytes.
                readRequestData(1, out);

            }
        }
        catch (IOException e) {
            throw new IOException(String.format("getResponse serial port exception - %s", e.getMessage()));
        }
    }

    /**
     * Writes the Modbus message to the comms port
     *
     * @param msg a <code>ModbusMessage</code> value
     *
     * @throws ModbusIOException If an error occurred bundling the message
     */
    protected void writeMessageOut(ModbusMessage msg) throws ModbusIOException {
        try {
            int len;
            synchronized (byteOutputStream) {
                // first clear any input from the receive buffer to prepare
                // for the reply since RTU doesn't have message delimiters
                clearInput();
                // write message to byte out
                byteOutputStream.reset();
                msg.setHeadless();
                msg.writeTo(byteOutputStream);
                len = byteOutputStream.size();
                int[] crc = ModbusUtil.calculateCRC(byteOutputStream.getBuffer(), 0, len);
                byteOutputStream.writeByte(crc[0]);
                byteOutputStream.writeByte(crc[1]);
                // write message
                writeBytes(byteOutputStream.getBuffer(), byteOutputStream.size());
                if (logger.isDebugEnabled()) {
                    logger.debug("Sent: {}", ModbusUtil.toHex(byteOutputStream.getBuffer(), 0, byteOutputStream.size()));
                }
                // clears out the echoed message
                // for RS485
                if (echo) {
                    readEcho(len + 2);
                }
                lastRequest = new byte[len];
                System.arraycopy(byteOutputStream.getBuffer(), 0, lastRequest, 0, len);
            }
        }
        catch (IOException ex) {
            throw new ModbusIOException("I/O failed to write");
        }
    }

    @Override
    protected ModbusRequest readRequestIn(AbstractModbusListener listener) throws ModbusIOException {
        ModbusRequest request = null;
        
        try {
            while (request == null) {
                synchronized (byteInputStream) {
                    int uid = readByte();
                    
                    byteInputOutputStream.reset();
                    byteInputOutputStream.writeByte(uid);
                    
                    if (listener.getProcessImage(uid) != null) {
                        // Read a proper request

                        int fc = readByte();
                        byteInputOutputStream.writeByte(fc);
                        
                        // create request to acquire length of message
                        request = ModbusRequest.createModbusRequest(fc);
                        request.setHeadless();

                        /*
                         * With Modbus RTU, there is no end frame. Either we
                         * assume the message is complete as is or we must do
                         * function specific processing to know the correct
                         * length. To avoid moving frame timing to the serial
                         * input functions, we set the timeout and to message
                         * specific parsing to read a response.
                         */
                        getRequest(fc, byteInputOutputStream);
                        int dlength = byteInputOutputStream.size() - 2; // less the crc
                        if (logger.isDebugEnabled()) {
                            logger.debug("Request: {}", ModbusUtil.toHex(byteInputOutputStream.getBuffer(), 0, dlength + 2));
                        }

                        byteInputStream.reset(inBuffer, dlength);

                        // check CRC
                        int[] crc = ModbusUtil.calculateCRC(inBuffer, 0, dlength); // does not include CRC
                        if (ModbusUtil.unsignedByteToInt(inBuffer[dlength]) != crc[0] || ModbusUtil.unsignedByteToInt(inBuffer[dlength + 1]) != crc[1]) {
                            logger.debug("CRC should be {}, {}", Integer.toHexString(crc[0]), Integer.toHexString(crc[1]));
                            
                            // Drain the input in case the frame was misread and more
                            // was to follow.
                            clearInput();
                            throw new IOException("CRC Error in received frame: " + dlength + " bytes: " + ModbusUtil.toHex(byteInputStream.getBuffer(), 0, dlength));
                        }
                        
                        // read request
                        byteInputStream.reset(inBuffer, dlength);
                        request.readFrom(byteInputStream);
                        
                        return request;
                        
                    } else {
                        // This message is not for us, read and wait for the 3.5t delay
                        
                        // Wait for max 1.5t for data to be available
                        while (true) {
                            boolean bytesAvailable = availableBytes() > 0;
                            if (bytesAvailable == false) {
                                // Sleep the 1.5t to see if there will be more data
                                logger.debug("Waiting for {} microsec", getMaxCharDelay());
                                bytesAvailable = ModbusUtil.spinCondition(getMaxCharDelay(), () -> availableBytes() > 0);
                            }
                            
                            if (bytesAvailable) {
                                // Read the available data
                                while (availableBytes() > 0) {
                                    byteInputOutputStream.writeByte(readByte());
                                }
                            } else {
                                // Transition to wait for the 3.5t interval
                                break;
                            }
                        }
                        
                        // Wait for 2t to complete the 3.5t wait
                        // Is there is data available the interval was not respected, we should discard the message
                        logger.debug("Waiting for {} microsec", 1000);
                        if (ModbusUtil.spinCondition(1000, () -> availableBytes() > 0)) {
                            // Discard the message
                            logger.debug("Discarding message (More than 1.5t between characters!) - {}", ModbusUtil.toHex(byteInputOutputStream.getBuffer(), 0, byteInputOutputStream.size()));
                        } else {
                            // This message is complete
                            logger.debug("Read message not meant for us: {}", ModbusUtil.toHex(byteInputOutputStream.getBuffer(), 0, byteInputOutputStream.size()));
                        }
                    }
                }
            }
            
            // We will never get here
            return null;
        }
        catch (IOException ex) {
            // An exception mostly means there is no request. The master should
            // retry the request.
            
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to read response! {}", ex.getMessage());
            }
        	
            return null;
        }
    }

    /**
     * readResponse - Read the bytes for the response from the slave.
     *
     * @return a <tt>ModbusRespose</tt>
     * @throws com.ghgande.j2mod.modbus.ModbusIOException If the response cannot be read from the socket/port
     */
    protected ModbusResponse readResponseIn() throws ModbusIOException {
        boolean done;
        ModbusResponse response;
        int dlength;

        try {
            do {
                // 1. read to function code, create request and read function
                // specific bytes
                synchronized (byteInputStream) {
                    int uid = readByte();
                    
                    if (uid != -1) {
                        int fc = readByte();
                        byteInputOutputStream.reset();
                        byteInputOutputStream.writeByte(uid);
                        byteInputOutputStream.writeByte(fc);

                        // create response to acquire length of message
                        response = ModbusResponse.createModbusResponse(fc);
                        response.setHeadless();

                        /*
                         * With Modbus RTU, there is no end frame. Either we
                         * assume the message is complete as is or we must do
                         * function specific processing to know the correct
                         * length. To avoid moving frame timing to the serial
                         * input functions, we set the timeout and to message
                         * specific parsing to read a response.
                         */
                        getResponse(fc, byteInputOutputStream);
                        dlength = byteInputOutputStream.size() - 2; // less the crc
                        if (logger.isDebugEnabled()) {
                            logger.debug("Response: {}", ModbusUtil.toHex(byteInputOutputStream.getBuffer(), 0, dlength + 2));
                        }
                        byteInputStream.reset(inBuffer, dlength);

                        // check CRC
                        int[] crc = ModbusUtil.calculateCRC(inBuffer, 0, dlength); // does not include CRC
                        if (ModbusUtil.unsignedByteToInt(inBuffer[dlength]) != crc[0] || ModbusUtil.unsignedByteToInt(inBuffer[dlength + 1]) != crc[1]) {
                            logger.debug("CRC should be {}, {}", crc[0], crc[1]);
                            throw new IOException("CRC Error in received frame: " + dlength + " bytes: " + ModbusUtil.toHex(byteInputStream.getBuffer(), 0, dlength));
                        }
                    }
                    else {
                        throw new IOException("Error reading response");
                    }

                    // read response
                    byteInputStream.reset(inBuffer, dlength);
                    response.readFrom(byteInputStream);
                    done = true;
                }
            } while (!done);
            return response;
        }
        catch (IOException ex) {
        	// FIXME: This printout is wrong when reading response from other slave
            throw new ModbusIOException("I/O exception - failed to read response for request [%s] - %s", ModbusUtil.toHex(lastRequest), ex.getMessage());
        }
    }
}
