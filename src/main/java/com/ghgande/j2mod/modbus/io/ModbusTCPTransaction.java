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
import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.ModbusSlaveException;
import com.ghgande.j2mod.modbus.msg.ExceptionResponse;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import com.ghgande.j2mod.modbus.util.ModbusUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class implementing the <tt>ModbusTransaction</tt> interface.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class ModbusTCPTransaction extends ModbusTransaction {

    private static final Logger logger = LoggerFactory.getLogger(ModbusTCPTransaction.class);

    // instance attributes and associations
    private TCPMasterConnection connection;
    protected boolean reconnecting = Modbus.DEFAULT_RECONNECTING;

    /**
     * Constructs a new <tt>ModbusTCPTransaction</tt> instance.
     */
    public ModbusTCPTransaction() {
    }

    /**
     * Constructs a new <tt>ModbusTCPTransaction</tt> instance with a given
     * <tt>ModbusRequest</tt> to be send when the transaction is executed.
     * <p>
     *
     * @param request a <tt>ModbusRequest</tt> instance.
     */
    public ModbusTCPTransaction(ModbusRequest request) {
        setRequest(request);
    }

    /**
     * Constructs a new <tt>ModbusTCPTransaction</tt> instance with a given
     * <tt>TCPMasterConnection</tt> to be used for transactions.
     * <p>
     *
     * @param con a <tt>TCPMasterConnection</tt> instance.
     */
    public ModbusTCPTransaction(TCPMasterConnection con) {
        setConnection(con);
        transport = con.getModbusTransport();
    }

    /**
     * Sets the connection on which this <tt>ModbusTransaction</tt> should be
     * executed.
     * <p>
     * An implementation should be able to handle open and closed connections.
     * <br>
     * <p>
     *
     * @param con a <tt>TCPMasterConnection</tt>.
     */
    public synchronized void setConnection(TCPMasterConnection con) {
        connection = con;
        transport = con.getModbusTransport();
    }

    /**
     * Tests if the connection will be opened and closed for <b>each</b>
     * execution.
     * <p>
     *
     * @return true if reconnecting, false otherwise.
     */
    public boolean isReconnecting() {
        return reconnecting;
    }

    /**
     * Sets the flag that controls whether a connection is opened and closed
     * for <b>each</b> execution or not.
     * <p>
     *
     * @param b true if reconnecting, false otherwise.
     */
    public void setReconnecting(boolean b) {
        reconnecting = b;
    }

    @Override
    public synchronized void execute() throws ModbusException {

        if (request == null || connection == null) {
            throw new ModbusException("Invalid request or connection");
        }

        // Try sending the message up to retries time. Note that the message
        // is read immediately after being written, with no flushing of buffers.
        int retryCounter = 0;
        int retryLimit = (retries > 0 ? retries : Modbus.DEFAULT_RETRIES);
        boolean keepTrying = true;

        // While we haven't exhausted all the retry attempts
        while (keepTrying) {

            // Automatically connect if we aren't already connected
            if (!connection.isConnected()) {
                try {
                    logger.debug("Connecting to: {}:{}", connection.getAddress().toString(), connection.getPort());
                    connection.connect();
                    transport = connection.getModbusTransport();
                }
                catch (Exception ex) {
                    throw new ModbusIOException("Connection failed for %s:%d", connection.getAddress().toString(), connection.getPort(), ex.getMessage());
                }
            }

            // Make sure the timeout is set
            transport.setTimeout(connection.getTimeout());

            try {

                // Write the message to the endpoint
                logger.debug("Writing request: {} (try: {}) request transaction ID = {} to {}:{}", request.getHexMessage(), retryCounter, request.getTransactionID(), connection.getAddress().toString(), connection.getPort());
                transport.writeRequest(request);

                // Read the response
                response = transport.readResponse();
                logger.debug("Read response: {} (try: {}) response transaction ID = {} from {}:{}", response.getHexMessage(), retryCounter, response.getTransactionID(), connection.getAddress().toString(), connection.getPort());
                keepTrying = false;

                // The slave may have returned an exception -- check for that.
                if (response instanceof ExceptionResponse) {
                    throw new ModbusSlaveException(((ExceptionResponse)response).getExceptionCode());
                }

                // We need to keep retrying if;
                //   a) the response is empty OR
                //   b) we have been told to check the validity and the request/response transaction IDs don't match AND
                //   c) we haven't exceeded the maximum retry count
                if (responseIsInValid()) {
                    retryCounter++;
                    if (retryCounter >= retryLimit) {
                        throw new ModbusIOException("Executing transaction failed (tried %d times)", retryLimit);
                    }
                    keepTrying = true;
                    long sleepTime = getRandomSleepTime(retryCounter);
                    if (response == null) {
                        logger.debug("Failed to get any response (try: {}) - retrying after {} milliseconds", retryCounter, sleepTime);
                    }
                    else {
                        logger.debug("Failed to get a valid response, transaction IDs do not match (try: {}) - retrying after {} milliseconds", retryCounter, sleepTime);
                    }
                    ModbusUtil.sleep(sleepTime);
                }
            }
            catch (ModbusIOException ex) {

                // Up the retry counter and check if we are exhausted
                retryCounter++;
                if (retryCounter >= retryLimit) {
                    throw new ModbusIOException("Executing transaction %s failed (tried %d times) %s", request.getHexMessage(), retryLimit, ex.getMessage());
                }
                else {
                    long sleepTime = getRandomSleepTime(retryCounter);
                    logger.debug("Failed transaction Request: {} (try: {}) - retrying after {} milliseconds", request.getHexMessage(), retryCounter, sleepTime);
                    ModbusUtil.sleep(sleepTime);
                }

                // If this has happened, then we should close and re-open the connection before re-trying
                logger.debug("Failed request {} (try: {}) request transaction ID = {} - {} closing and re-opening connection {}:{}", request.getHexMessage(), retryCounter, request.getTransactionID(), ex.getMessage(), connection.getAddress().toString(), connection.getPort());
                connection.close();
            }

            // Increment the transaction ID if we are still trying
            if (keepTrying) {
                incrementTransactionID();
            }
        }

        // Close the connection if it isn't supposed to stick around.
        if (isReconnecting()) {
            connection.close();
        }
        incrementTransactionID();
    }

    /**
     * Returns true if the response is not valid
     * This can be if the response is null or the transaction ID of the request
     * doesn't match the reponse
     *
     * @return True if invalid
     */
    private boolean responseIsInValid() {
        if (response == null) {
            return true;
        }
        else if (!response.isHeadless() && validityCheck) {
            return request.getTransactionID() != response.getTransactionID();
        }
        else {
            return false;
        }
    }

    /**
     * incrementTransactionID -- Increment the transaction ID for the next
     * transaction. Note that the caller must get the new transaction ID with
     * getTransactionID(). This is only done validity checking is enabled so
     * that dumb slaves don't cause problems. The original request will have its
     * transaction ID incremented as well so that sending the same transaction
     * again won't cause problems.
     */
    private synchronized void incrementTransactionID() {
        if (isCheckingValidity()) {
            if (transactionID >= Modbus.MAX_TRANSACTION_ID) {
                transactionID = Modbus.DEFAULT_TRANSACTION_ID;
            }
            else {
                transactionID++;
            }
        }
        request.setTransactionID(getTransactionID());
    }

}
