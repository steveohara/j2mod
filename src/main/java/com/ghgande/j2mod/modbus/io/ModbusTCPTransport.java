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
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import com.ghgande.j2mod.modbus.util.ModbusUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Class that implements the Modbus transport flavor.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class ModbusTCPTransport extends AbstractModbusTransport {

    private static final Logger logger = LoggerFactory.getLogger(ModbusTCPTransport.class);

    // instance attributes
    private DataInputStream dataInputStream; // input stream
    private DataOutputStream dataOutputStream; // output stream
    private final BytesInputStream byteInputStream = new BytesInputStream(Modbus.MAX_MESSAGE_LENGTH + 6);
    private final BytesOutputStream byteOutputStream = new BytesOutputStream(Modbus.MAX_MESSAGE_LENGTH + 6); // write frames
    protected Socket socket = null;
    protected TCPMasterConnection master = null;
    private boolean headless = false; // Some TCP implementations are.

    /**
     * Default constructor
     */
    public ModbusTCPTransport() {
    }

    /**
     * Constructs a new <tt>ModbusTransport</tt> instance, for a given
     * <tt>Socket</tt>.
     * <p>
     *
     * @param socket the <tt>Socket</tt> used for message transport.
     */
    public ModbusTCPTransport(Socket socket) {
        try {
            setSocket(socket);
            socket.setSoTimeout(timeout);
        }
        catch (IOException ex) {
            logger.debug("ModbusTCPTransport::Socket invalid");

            throw new IllegalStateException("Socket invalid", ex);
        }
    }

    /**
     * Sets the <tt>Socket</tt> used for message transport and prepares the
     * streams used for the actual I/O.
     *
     * @param socket the <tt>Socket</tt> used for message transport.
     *
     * @throws IOException if an I/O related error occurs.
     */
    public void setSocket(Socket socket) throws IOException {
        if (this.socket != null) {
            this.socket.close();
            this.socket = null;
        }
        this.socket = socket;
        setTimeout(timeout);
        prepareStreams(socket);
    }

    /**
     * Set the transport to be headless
     */
    public void setHeadless() {
        headless = true;
    }

    /**
     * Set the transport to be headless
     *
     * @param headless True if headless
     */
    public void setHeadless(boolean headless) {
        this.headless = headless;
    }

    /**
     * Sets the master connection for the transport to use
     *
     * @param master Master
     */
    public void setMaster(TCPMasterConnection master) {
        this.master = master;
    }

    @Override
    public void setTimeout(int time) {
        super.setTimeout(time);
        if (socket != null) {
            try {
                socket.setSoTimeout(time);
            }
            catch (SocketException e) {
                logger.warn("Socket exception occurred while setting timeout to " + time, e);
            }
        }
    }

    @Override
    public void close() throws IOException {
        dataInputStream.close();
        dataOutputStream.close();
        socket.close();
    }

    @Override
    public ModbusTransaction createTransaction() {
        if (master == null) {
            master = new TCPMasterConnection(socket.getInetAddress());
            master.setPort(socket.getPort());
            master.setModbusTransport(this);
        }
        return new ModbusTCPTransaction(master);
    }

    @Override
    public void writeResponse(ModbusResponse msg) throws ModbusIOException {
        writeMessage(msg, false);
    }

    @Override
    public void writeRequest(ModbusRequest msg) throws ModbusIOException {
        writeMessage(msg, false);
    }

    @Override
    public ModbusRequest readRequest(AbstractModbusListener listener) throws ModbusIOException {
        ModbusRequest req;
        try {
            byteInputStream.reset();

            synchronized (byteInputStream) {
                byte[] buffer = byteInputStream.getBuffer();

                if (!headless) {
                    dataInputStream.readFully(buffer, 0, 6);

                    // The transaction ID must be treated as an unsigned short in
                    // order for validation to work correctly.

                    int transaction = ModbusUtil.registerToShort(buffer, 0) & 0x0000FFFF;
                    int protocol = ModbusUtil.registerToShort(buffer, 2);
                    int count = ModbusUtil.registerToShort(buffer, 4);

                    dataInputStream.readFully(buffer, 6, count);

                    if (logger.isDebugEnabled()) {
                        logger.debug("Read: {}", ModbusUtil.toHex(buffer, 0, count + 6));
                    }

                    byteInputStream.reset(buffer, (6 + count));
                    byteInputStream.skip(6);

                    int unit = byteInputStream.readByte();
                    int functionCode = byteInputStream.readUnsignedByte();

                    byteInputStream.reset();
                    req = ModbusRequest.createModbusRequest(functionCode);
                    req.setUnitID(unit);
                    req.setHeadless(false);

                    req.setTransactionID(transaction);
                    req.setProtocolID(protocol);
                    req.setDataLength(count);

                    req.readFrom(byteInputStream);
                }
                else {

                    // This is a headless request.

                    int unit = dataInputStream.readByte();
                    int function = dataInputStream.readByte();

                    req = ModbusRequest.createModbusRequest(function);
                    req.setUnitID(unit);
                    req.setHeadless(true);
                    req.readData(dataInputStream);

                    // Discard the CRC. This is a TCP/IP connection, which has
                    // proper error correction and recovery.

                    dataInputStream.readShort();
                    if (logger.isDebugEnabled()) {
                        logger.debug("Read: {}", req.getHexMessage());
                    }
                }
            }
            return req;
        }
        catch (EOFException eoex) {
            throw new ModbusIOException("End of File", true);
        }
        catch (SocketTimeoutException x) {
            throw new ModbusIOException("Timeout reading request", x);
        }
        catch (SocketException sockex) {
            throw new ModbusIOException("Socket Exception", sockex);
        }
        catch (IOException ex) {
            throw new ModbusIOException("I/O exception - failed to read", ex);
        }
    }

    @Override
    public ModbusResponse readResponse() throws ModbusIOException {
        try {
            ModbusResponse response;

            synchronized (byteInputStream) {
                // use same buffer
                byte[] buffer = byteInputStream.getBuffer();
                logger.debug("Reading response...");
                if (!headless) {
                    // All Modbus TCP transactions start with 6 bytes. Get them.
                    dataInputStream.readFully(buffer, 0, 6);

                    /*
                     * The transaction ID is the first word (offset 0) in the
                     * data that was just read. It will be echoed back to the
                     * requester.
                     *
                     * The protocol ID is the second word (offset 2) in the
                     * data. It should always be 0, but I don't check.
                     *
                     * The length of the payload is the third word (offset 4) in
                     * the data that was just read. That's what I need in order
                     * to read the rest of the response.
                     */
                    int transaction = ModbusUtil.registerToShort(buffer, 0) & 0x0000FFFF;
                    int protocol = ModbusUtil.registerToShort(buffer, 2);
                    int count = ModbusUtil.registerToShort(buffer, 4);

                    dataInputStream.readFully(buffer, 6, count);
                    byteInputStream.reset(buffer, (6 + count));
                    byteInputStream.reset();
                    byteInputStream.skip(7);
                    int function = byteInputStream.readUnsignedByte();
                    response = ModbusResponse.createModbusResponse(function);

                    // Rewind the input buffer, then read the data into the
                    // response.
                    byteInputStream.reset();
                    response.readFrom(byteInputStream);

                    response.setTransactionID(transaction);
                    response.setProtocolID(protocol);
                }
                else {
                    // This is a headless response. It has the same format as a
                    // RTU over Serial response.
                    int unit = dataInputStream.readByte();
                    int function = dataInputStream.readByte();

                    response = ModbusResponse.createModbusResponse(function);
                    response.setUnitID(unit);
                    response.setHeadless();
                    response.readData(dataInputStream);

                    // Now discard the CRC. Which hopefully wasn't needed
                    // because this is a TCP transport.
                    dataInputStream.readShort();
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully read: {}", response.getHexMessage());
            }
            return response;
        }
        catch (EOFException ex1) {
            throw new ModbusIOException("Premature end of stream (Message truncated) - %s", ex1.getMessage());
        }
        catch (SocketTimeoutException ex2) {
            throw new ModbusIOException("Socket timeout reading response - %s", ex2.getMessage());
        }
        catch (Exception ex3) {
            throw new ModbusIOException("General exception - failed to read - %s", ex3.getMessage());
        }
    }

    /**
     * Prepares the input and output streams of this <tt>ModbusTCPTransport</tt>
     * instance based on the given socket.
     *
     * @param socket the socket used for communications.
     *
     * @throws IOException if an I/O related error occurs.
     */
    private void prepareStreams(Socket socket) throws IOException {

        // Close any open streams if I'm being called because a new socket was
        // set to handle this transport.
        try {
            if (dataInputStream != null) {
                dataInputStream.close();
            }
            if (dataOutputStream != null) {
                dataOutputStream.close();
            }
        }
        catch (IOException x) {
            // Do nothing.
        }

        dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    /**
     * Writes a <tt>ModbusMessage</tt> to the
     * output stream of this <tt>ModbusTransport</tt>.
     * <p>
     *
     * @param msg           a <tt>ModbusMessage</tt>.
     * @param useRtuOverTcp True if the RTU protocol should be used over TCP
     *
     * @throws ModbusIOException data cannot be
     *                           written properly to the raw output stream of
     *                           this <tt>ModbusTransport</tt>.
     */
    void writeMessage(ModbusMessage msg, boolean useRtuOverTcp) throws ModbusIOException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Sending: {}", msg.getHexMessage());
            }
            byte message[] = msg.getMessage();

            byteOutputStream.reset();
            if (!headless) {
                byteOutputStream.writeShort(msg.getTransactionID());
                byteOutputStream.writeShort(msg.getProtocolID());
                byteOutputStream.writeShort((message != null ? message.length : 0) + 2);
            }
            byteOutputStream.writeByte(msg.getUnitID());
            byteOutputStream.writeByte(msg.getFunctionCode());
            if (message != null && message.length > 0) {
                byteOutputStream.write(message);
            }

            // Add CRC for RTU over TCP
            if (useRtuOverTcp) {
                int len = byteOutputStream.size();
                int[] crc = ModbusUtil.calculateCRC(byteOutputStream.getBuffer(), 0, len);
                byteOutputStream.writeByte(crc[0]);
                byteOutputStream.writeByte(crc[1]);
            }

            dataOutputStream.write(byteOutputStream.toByteArray());
            dataOutputStream.flush();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully sent: {}", ModbusUtil.toHex(byteOutputStream.toByteArray()));
            }
            // write more sophisticated exception handling
        }
        catch (SocketException ex1) {
            if (master != null && !master.isConnected()) {
                try {
                    master.connect(useRtuOverTcp);
                }
                catch (Exception e) {
                    // Do nothing.
                }
            }
            throw new ModbusIOException("I/O socket exception - failed to write - %s", ex1.getMessage());
        }
        catch (Exception ex2) {
            throw new ModbusIOException("General exception - failed to write - %s", ex2.getMessage());
        }
    }

}
