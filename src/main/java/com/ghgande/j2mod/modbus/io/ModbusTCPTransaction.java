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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class implementing the <tt>ModbusTransaction</tt> interface.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4energy)
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

        // Automatically re-connect if disconnected.
        if (!connection.isConnected()) {
            try {
                connection.connect();
                transport = connection.getModbusTransport();
            }
            catch (Exception ex) {
                throw new ModbusIOException("Connection failed", ex);
            }
        }

        // Try sending the message up to retries time. Note that the message
        // is read immediately after being written, with no flushing of buffers.
        int retryCounter = 0;
        int retryLimit = (retries > 0 ? retries : 1);

        while (retryCounter < retryLimit) {
            try {
                logger.debug("request transaction ID = {}", request.getTransactionID());
                transport.writeMessage(request);
                response = null;
                do {
                    response = transport.readResponse();
                    if (logger.isDebugEnabled()) {
                        logger.debug("response transaction ID = {}", response.getTransactionID());
                        if (response.getTransactionID() != request.getTransactionID()) {
                            logger.debug("expected {}, got {}", request.getTransactionID(), response.getTransactionID());
                        }
                    }
                } while (response != null &&
                        (!isCheckingValidity() || (request.getTransactionID() != 0 && request.getTransactionID() != response.getTransactionID())) &&
                        ++retryCounter < retryLimit);

                if (retryCounter >= retryLimit) {
                    throw new ModbusIOException("Executing transaction failed (tried " + retries + " times)");
                }

                // Both methods were successful, so the transaction must
                // have been executed.
                break;
            }
            catch (ModbusIOException ex) {
                if (!connection.isConnected()) {
                    try {
                        connection.connect();
                    }
                    catch (Exception e) {
                        // Nope, fail this transaction.
                        throw new ModbusIOException("Connection lost", e);
                    }
                }
                retryCounter++;
                if (retryCounter >= retryLimit) {
                    throw new ModbusIOException("Executing transaction failed (tried " + retries + " times)", ex);
                }
            }
        }

        // The slave may have returned an exception -- check for that.
        if (response instanceof ExceptionResponse) {
            throw new ModbusSlaveException(((ExceptionResponse)response).getExceptionCode());
        }

        // Close the connection if it isn't supposed to stick around.
        if (isReconnecting()) {
            connection.close();
        }

        // See if packets require validity checking.
        if (isCheckingValidity()) {
            checkValidity();
        }
        incrementTransactionID();
    }

    /**
     * checkValidity -- Verify the transaction IDs match or are zero.
     *
     * @throws ModbusException if the transaction was not valid.
     */
    private void checkValidity() throws ModbusException {
        if (request.getTransactionID() == 0
                || response.getTransactionID() == 0) {
            return;
        }

        if (request.getTransactionID() != response.getTransactionID()) {
            throw new ModbusException("Transaction ID mismatch");
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
                transactionID = 1;
            }
            else {
                transactionID++;
            }
        }
        request.setTransactionID(getTransactionID());
    }

}
