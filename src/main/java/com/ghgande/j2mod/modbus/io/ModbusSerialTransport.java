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

import com.fazecast.jSerialComm.SerialPort;
import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.msg.ModbusMessage;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.net.AbstractModbusListener;
import com.ghgande.j2mod.modbus.net.AbstractSerialConnection;
import com.ghgande.j2mod.modbus.util.ModbusUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Abstract base class for serial <tt>ModbusTransport</tt>
 * implementations.
 *
 * @author Dieter Wimberger
 * @author John Charlton
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public abstract class ModbusSerialTransport extends AbstractModbusTransport {

    private static final Logger logger = LoggerFactory.getLogger(ModbusSerialTransport.class);

    /**
     * Defines a virtual number for the FRAME START token (COLON).
     */
    static final int FRAME_START = 1000;
    /**
     * Defines a virtual number for the FRAME_END token (CR LF).
     */
    static final int FRAME_END = 2000;

    /**
     * The number of nanoseconds there is in a millisecond
     */
    private static final int NS_IN_A_MS = 1000000;
    private static final String CANNOT_READ_FROM_SERIAL_PORT = "Cannot read from serial port";
    private static final String COMM_PORT_IS_NOT_VALID_OR_NOT_OPEN = "Comm port is not valid or not open";
    private AbstractSerialConnection commPort;
    boolean echo = false;     // require RS-485 echo processing
    private final Set<AbstractSerialTransportListener> listeners = Collections.synchronizedSet(new HashSet<AbstractSerialTransportListener>());

    /**
     * Creates a new transaction suitable for the serial port
     *
     * @return SerialTransaction
     */
    @Override
    public ModbusTransaction createTransaction() {
        ModbusSerialTransaction transaction = new ModbusSerialTransaction();
        transaction.setTransport(this);
        return transaction;
    }

    @Override
    public void writeResponse(ModbusResponse msg) throws ModbusIOException {
        // If this isn't a Slave ID missmatch message
        if (msg.getAuxiliaryType().equals(ModbusResponse.AuxiliaryMessageTypes.UNIT_ID_MISSMATCH)) {
            logger.debug("Ignoring response not meant for us");
        }
        else {
            // We need to pause before sending the response
            waitBetweenFrames();

            // Send the response
            writeMessage(msg);
        }
    }

    @Override
    public void writeRequest(ModbusRequest msg) throws ModbusIOException {
        writeMessage(msg);
    }

    /**
     * Writes the request/response message to the port
     *
     * @param msg Message to write
     * @throws ModbusIOException If the port throws an error
     */
    private void writeMessage(ModbusMessage msg) throws ModbusIOException {
        open();
        notifyListenersBeforeWrite(msg);
        try {
            writeMessageOut(msg);
            long startTime = System.nanoTime();

            // Wait here for the message to have been sent

            double bytesPerSec = ((double)commPort.getBaudRate()) / (((commPort.getNumDataBits() == 0) ? 8 : commPort.getNumDataBits()) + ((commPort.getNumStopBits() == 0) ? 1 : commPort.getNumStopBits()) + ((commPort.getParity() == SerialPort.NO_PARITY) ? 0 : 1));
            double delay = 1000000000.0 * msg.getOutputLength() / bytesPerSec;
            double delayMilliSeconds = Math.floor(delay / 1000000);
            double delayNanoSeconds = delay % 1000000;
            try {

                // For delays less than a millisecond, we need to chew CPU cycles unfortunately
                // There are some fiddle factors here to allow for some oddities in the hardware

                if (delayMilliSeconds == 0.0) {
                    int priority = Thread.currentThread().getPriority();
                    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                    long end = startTime + ((int) (delayNanoSeconds * 1.3));
                    while (System.nanoTime() < end) {
                        // noop
                    }
                    Thread.currentThread().setPriority(priority);
                }
                else {
                    Thread.sleep((int) (delayMilliSeconds * 1.7), (int) (delayNanoSeconds * 1.5));
                }
            }
            catch (Exception e) {
                logger.debug("nothing to do");
            }
        }
        finally {
            notifyListenersAfterWrite(msg);
        }
    }

    @Override
    public ModbusRequest readRequest(AbstractModbusListener listener) throws ModbusIOException {
        open();
        notifyListenersBeforeRequest();
        ModbusRequest req = readRequestIn(listener);
        notifyListenersAfterRequest(req);
        return req;
    }

    @Override
    public ModbusResponse readResponse() throws ModbusIOException {
        notifyListenersBeforeResponse();
        ModbusResponse res = readResponseIn();
        notifyListenersAfterResponse(res);
        return res;
    }

    /**
     * Opens the port if it isn't already open
     *
     * @throws ModbusIOException If a problem with the port
     */
    private void open() throws ModbusIOException {
        if (commPort != null && !commPort.isOpen()) {
            setTimeout(timeout);
            try {
                commPort.open();
            }
            catch (IOException e) {
                throw new ModbusIOException(String.format("Cannot open port %s - %s", commPort.getDescriptivePortName(), e.getMessage()));
            }
        }
    }

    @Override
    public void setTimeout(int time) {
        super.setTimeout(time);
        if (commPort != null) {
            commPort.setComPortTimeouts(AbstractSerialConnection.TIMEOUT_READ_BLOCKING, timeout, timeout);
        }
    }

    /**
     * The <code>writeRequest</code> method writes a modbus serial message to
     * its serial output stream to a specified slave unit ID.
     *
     * @param msg a <code>ModbusMessage</code> value
     * @throws ModbusIOException if an error occurs
     */
    protected abstract void writeMessageOut(ModbusMessage msg) throws ModbusIOException;

    /**
     * The <code>readRequest</code> method listens continuously on the serial
     * input stream for master request messages and replies if the request slave
     * ID matches its own set in process image
     *
     * @param listener Listener that received this request
     * @return a <code>ModbusRequest</code> value
     *
     * @throws ModbusIOException if an error occurs
     */
    protected abstract ModbusRequest readRequestIn(AbstractModbusListener listener) throws ModbusIOException;

    /**
     * <code>readResponse</code> reads a response message from the slave
     * responding to a master writeRequest request.
     *
     * @return a <code>ModbusResponse</code> value
     *
     * @throws ModbusIOException if an error occurs
     */
    protected abstract ModbusResponse readResponseIn() throws ModbusIOException;

    /**
     * Adds a listener to the transport to be called when an event occurs
     *
     * @param listener Listner callback
     */
    public void addListener(AbstractSerialTransportListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a listener from the event callback chain
     *
     * @param listener Listener to remove
     */
    public void removeListener(AbstractSerialTransportListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    /**
     * Clears the list of listeners
     */
    public void clearListeners() {
        listeners.clear();
    }

    /**
     * Calls any listeners with the given event and current port
     */
    private void notifyListenersBeforeRequest() {
        synchronized (listeners) {
            for (AbstractSerialTransportListener listener : listeners) {
                listener.beforeRequestRead(commPort);
            }
        }
    }

    /**
     * Calls any listeners with the given event and current port
     *
     * @param req Request received
     */
    private void notifyListenersAfterRequest(ModbusRequest req) {
        synchronized (listeners) {
            for (AbstractSerialTransportListener listener : listeners) {
                listener.afterRequestRead(commPort, req);
            }
        }
    }

    /**
     * Calls any listeners with the given event and current port
     */
    private void notifyListenersBeforeResponse() {
        synchronized (listeners) {
            for (AbstractSerialTransportListener listener : listeners) {
                listener.beforeResponseRead(commPort);
            }
        }
    }

    /**
     * Calls any listeners with the given event and current port
     *
     * @param res Response received
     */
    private void notifyListenersAfterResponse(ModbusResponse res) {
        synchronized (listeners) {
            for (AbstractSerialTransportListener listener : listeners) {
                listener.afterResponseRead(commPort, res);
            }
        }
    }

    /**
     * Calls any listeners with the given event and current port
     *
     * @param msg Message to be sent
     */
    private void notifyListenersBeforeWrite(ModbusMessage msg) {
        synchronized (listeners) {
            for (AbstractSerialTransportListener listener : listeners) {
                listener.beforeMessageWrite(commPort, msg);
            }
        }
    }

    /**
     * Calls any listeners with the given event and current port
     *
     * @param msg Message sent
     */
    private void notifyListenersAfterWrite(ModbusMessage msg) {
        synchronized (listeners) {
            for (AbstractSerialTransportListener listener : listeners) {
                listener.afterMessageWrite(commPort, msg);
            }
        }
    }

    /**
     * Calls any listeners with the given event and current port
     */
    public void notifyListenersDisconnected() {
        synchronized (listeners) {
            for (AbstractSerialTransportListener listener : listeners) {
                listener.disconnected(commPort);
            }
        }
    }
    
    /**
     * <code>setCommPort</code> sets the comm port member and prepares the input
     * and output streams to be used for reading from and writing to.
     *
     * @param cp the comm port to read from/write to.
     * @throws IOException if an I/O related error occurs.
     */
    public void setCommPort(AbstractSerialConnection cp) throws IOException {
        commPort = cp;
        setTimeout(timeout);
    }

    /**
     * Returns the comms port being used for this transport
     *
     * @return Comms port
     */
    public AbstractSerialConnection getCommPort() {
        return commPort;
    }

    /**
     * <code>isEcho</code> method returns the output echo state.
     *
     * @return a <code>boolean</code> value
     */
    public boolean isEcho() {
        return echo;
    }

    /**
     * <code>setEcho</code> method sets the output echo state.
     *
     * @param b a <code>boolean</code> value
     */
    public void setEcho(boolean b) {
        this.echo = b;
    }

    /**
     * Reads the own message echo produced in RS485 Echo Mode
     * within the given time frame.
     *
     * @param len is the length of the echo to read.  Timeout will occur if the
     *            echo is not received in the time specified in the SerialConnection.
     * @throws IOException if a I/O error occurred.
     */
    protected void readEcho(int len) throws IOException {
        byte[] echoBuf = new byte[len];
        int echoLen = commPort.readBytes(echoBuf, len);
        if (logger.isDebugEnabled()) {
            logger.debug("Echo: {}", ModbusUtil.toHex(echoBuf, 0, echoLen));
        }
        if (echoLen != len) {
            logger.debug("Error: Transmit echo not received");
            throw new IOException("Echo not received");
        }
    }

    protected int availableBytes() {
        return commPort.bytesAvailable();
    }

    /**
     * Reads a byte from the comms port
     *
     * @return Value of the byte
     *
     * @throws IOException If it cannot read or times out
     */
    protected int readByte() throws IOException {
        if (commPort != null && commPort.isOpen()) {
            byte[] buffer = new byte[1];
            int cnt = commPort.readBytes(buffer, 1);
            if (cnt != 1) {
                throw new IOException(CANNOT_READ_FROM_SERIAL_PORT);
            }
            else {
                return buffer[0] & 0xff;
            }
        }
        else {
            throw new IOException(COMM_PORT_IS_NOT_VALID_OR_NOT_OPEN);
        }
    }

    /**
     * Reads the specified number of bytes from the input stream
     *
     * @param buffer      Buffer to put data into
     * @param bytesToRead Number of bytes to read
     * @throws IOException If the port is invalid or if the number of bytes returned is not equal to that asked for
     */
    void readBytes(byte[] buffer, int bytesToRead) throws IOException {
        if (commPort != null && commPort.isOpen()) {
            int cnt = commPort.readBytes(buffer, bytesToRead);
            if (cnt != bytesToRead) {
                throw new IOException("Cannot read from serial port - truncated");
            }
        }
        else {
            throw new IOException(COMM_PORT_IS_NOT_VALID_OR_NOT_OPEN);
        }
    }

    /**
     * Writes the bytes to the output stream
     *
     * @param buffer       Buffer to write
     * @param bytesToWrite Number of bytes to write
     * @return Number of bytes written
     *
     * @throws java.io.IOException if writing to invalid port
     */
    final int writeBytes(byte[] buffer, int bytesToWrite) throws IOException {
        if (commPort != null && commPort.isOpen()) {
            return commPort.writeBytes(buffer, bytesToWrite);
        }
        else {
            throw new IOException(COMM_PORT_IS_NOT_VALID_OR_NOT_OPEN);
        }
    }

    /**
     * Reads an ascii byte from the input stream
     * It handles the special start and end frame markers
     *
     * @return Byte value of the next ASCII couplet
     *
     * @throws IOException If a problem with the port
     */
    int readAsciiByte() throws IOException {
        if (commPort != null && commPort.isOpen()) {
            byte[] buffer = new byte[1];
            int cnt = commPort.readBytes(buffer, 1);
            if (cnt != 1) {
                throw new IOException(CANNOT_READ_FROM_SERIAL_PORT);
            }
            else if (buffer[0] == ':') {
                return FRAME_START;
            }
            else if (buffer[0] == '\r' || buffer[0] == '\n') {
                return FRAME_END;
            }
            else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Read From buffer: {} ({})", buffer[0], String.format("%02X", buffer[0]));
                }
                byte firstValue = buffer[0];
                cnt = commPort.readBytes(buffer, 1);
                if (cnt != 1) {
                    throw new IOException(CANNOT_READ_FROM_SERIAL_PORT);
                }
                else {
                    int combinedValue = (Character.digit(firstValue, 16) << 4) + Character.digit(buffer[0], 16);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Returning combined value of: {}", String.format("%02X", combinedValue));
                    }
                    return combinedValue;
                }
            }
        }
        else {
            throw new IOException(COMM_PORT_IS_NOT_VALID_OR_NOT_OPEN);
        }
    }

    /**
     * Writes out a byte value as an ascii character
     * If the value is the special start/end characters, then
     * allowance is made for these
     *
     * @param value Value to write
     * @return Number of bytes written
     *
     * @throws IOException If a problem with the port
     */
    final int writeAsciiByte(int value) throws IOException {
        if (commPort != null && commPort.isOpen()) {
            byte[] buffer;

            if (value == FRAME_START) {
                buffer = new byte[]{58};
                logger.debug("Wrote FRAME_START");
            }
            else if (value == FRAME_END) {
                buffer = new byte[]{13, 10};
                logger.debug("Wrote FRAME_END");
            }
            else {
                buffer = ModbusUtil.toHex(value);
                if (logger.isDebugEnabled()) {
                    logger.debug("Wrote byte {}={}", value, ModbusUtil.toHex(value));
                }
            }
            if (buffer != null) {
                return commPort.writeBytes(buffer, buffer.length);
            }
            else {
                throw new IOException("Message to send is empty");
            }
        }
        else {
            throw new IOException(COMM_PORT_IS_NOT_VALID_OR_NOT_OPEN);
        }
    }

    /**
     * Writes an array of bytes out as a stream of ascii characters
     *
     * @param buffer       Buffer of bytes to write
     * @param bytesToWrite Number of characters to write
     * @return Number of bytes written
     *
     * @throws IOException If a problem with the port
     */
    int writeAsciiBytes(byte[] buffer, long bytesToWrite) throws IOException {
        if (commPort != null && commPort.isOpen()) {
            int cnt = 0;
            for (int i = 0; i < bytesToWrite; i++) {
                if (writeAsciiByte(buffer[i]) != 2) {
                    return cnt;
                }
                cnt++;
            }
            return cnt;
        }
        else {
            throw new IOException(COMM_PORT_IS_NOT_VALID_OR_NOT_OPEN);
        }
    }

    /**
     * clearInput - Clear the input if characters are found in the input stream.
     *
     * @throws IOException If a problem with the port
     */
    void clearInput() throws IOException {
        if (commPort.bytesAvailable() > 0) {
            int len = commPort.bytesAvailable();
            byte[] buf = new byte[len];
            readBytes(buf, len);
            if (logger.isDebugEnabled()) {
                logger.debug("Clear input: {}", ModbusUtil.toHex(buf, 0, len));
            }
        }
    }

    /**
     * Closes the comms port and any streams associated with it
     *
     * @throws IOException Comm port close failed
     */
    @Override
    public void close() throws IOException {
        commPort.close();
    }

    /**
     * Injects a delay dependent on the baud rate
     */
    private void waitBetweenFrames() {
        waitBetweenFrames(0, 0);
    }

    /**
     * Injects a delay dependent on the last time we received a response or
     * if a fixed delay has been specified
     *
     * @param transDelayMS             Fixed transaction delay (milliseconds)
     * @param lastTransactionTimestamp Timestamp of last transaction
     */
    void waitBetweenFrames(int transDelayMS, long lastTransactionTimestamp) {

        // If a fixed delay has been set
        if (transDelayMS > 0) {
            ModbusUtil.sleep(transDelayMS);
        }
        else {
            // Make use we have a gap of 3.5 characters between adjacent requests
            // We have to do the calculations here because it is possible that the caller may have changed
            // the connection characteristics if they provided the connection instance
            int delay = getInterFrameDelay() / 1000;

            // How long since the last message we received
            long gapSinceLastMessage = (System.nanoTime() - lastTransactionTimestamp) / NS_IN_A_MS;
            if (delay > gapSinceLastMessage) {
                long sleepTime = delay - gapSinceLastMessage;

                ModbusUtil.sleep(sleepTime);

                if (logger.isDebugEnabled()) {
                    logger.debug("Waited between frames for {} ms", sleepTime);
                }
            }
        }
    }

    /**
     * In microseconds
     *
     * @return Delay between frames
     */
    int getInterFrameDelay() {
        if (commPort.getBaudRate() > 19200) {
            return 1750;
        }
        else {
            return Math.max(getCharInterval(Modbus.INTER_MESSAGE_GAP), Modbus.MINIMUM_TRANSMIT_DELAY);
        }
    }

    /**
     * The maximum delay between characters in microseconds
     *
     * @return microseconds
     */
    long getMaxCharDelay() {
        if (commPort.getBaudRate() > 19200) {
            return 1750;
        }
        else {
            return getCharIntervalMicro(Modbus.INTER_CHARACTER_GAP);
        }
    }

    /**
     * Calculates an interval based on a set number of characters.
     * Used for message timings.
     *
     * @param chars Number of characters
     * @return char interval in milliseconds
     */
    int getCharInterval(double chars) {
        return (int) (getCharIntervalMicro(chars) / 1000);
    }

    /**
     * Calculates an interval based on a set number of characters.
     * Used for message timings.
     *
     * @param chars Number of caracters
     * @return microseconds
     */
    long getCharIntervalMicro(double chars) {
        // Make use we have a gap of 3.5 characters between adjacent requests
        // We have to do the calculations here because it is possible that the caller may have changed
        // the connection characteristics if they provided the connection instance
        return (long) chars * NS_IN_A_MS * (1 + commPort.getNumDataBits() + commPort.getNumStopBits() + (commPort.getParity() == AbstractSerialConnection.NO_PARITY ? 0 : 1)) / commPort.getBaudRate();
    }

    /**
     * Spins until the timeout or the condition is met.
     * This method will repeatedly poll the available bytes, so it should not have any side effects.
     *
     * @param waitTimeMicroSec The time to wait for the condition to be true in microseconds
     * @return true if the condition ended the spin, false if the tim
     */
    boolean spinUntilBytesAvailable(long waitTimeMicroSec) {
        long start = System.nanoTime();
        while (availableBytes() < 1) {
            long delta = System.nanoTime() - start;
            if (delta > waitTimeMicroSec * 1000) {
                return false;
            }
        }
        return true;
    }
}
