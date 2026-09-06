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
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Interface defining a ModbusTransaction.
 * <p>
 * A transaction is defined by the sequence of
 * sending a request message and receiving a
 * related response message.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public abstract class ModbusTransaction {

    protected AbstractModbusTransport transport;
    protected ModbusRequest request;
    protected ModbusResponse response;
    boolean validityCheck = Modbus.DEFAULT_VALIDITYCHECK;
    int retries = Modbus.DEFAULT_RETRIES;
    private final Random random = new Random(System.nanoTime());
    static final AtomicInteger transactionID = new AtomicInteger(Modbus.DEFAULT_TRANSACTION_ID);

    /**
     * Returns the <tt>ModbusRequest</tt> instance
     * associated with this <tt>ModbusTransaction</tt>.
     * <p>
     *
     * @return the associated <tt>ModbusRequest</tt> instance.
     */
    public ModbusRequest getRequest() {
        return request;
    }

    /**
     * Sets the <tt>ModbusRequest</tt> for this
     * <tt>ModbusTransaction</tt>.<p>
     * The related <tt>ModbusResponse</tt> is acquired
     * from the passed in <tt>ModbusRequest</tt> instance.<br>
     * <p>
     *
     * @param req a <tt>ModbusRequest</tt>.
     */
    public void setRequest(ModbusRequest req) {
        request = req;
        if (req != null) {
            request.setTransactionID(nextTransactionID());
        }
    }

    /**
     * Returns the <tt>ModbusResponse</tt> instance
     * associated with this <tt>ModbusTransaction</tt>.
     * <p>
     *
     * @return the associated <tt>ModbusRequest</tt> instance.
     */
    public ModbusResponse getResponse() {
        return response;
    }

    /**
     * Returns the amount of retries for opening
     * the connection for executing the transaction.
     * <p>
     *
     * @return the amount of retries as <tt>int</tt>.
     */
    int getRetries() {
        return retries;
    }

    /**
     * Set the amount of retries for opening
     * the connection for executing the transaction.
     * <p>
     *
     * @param retries the amount of retries as <tt>int</tt>.
     */
    public void setRetries(int retries) {
        this.retries = retries;
    }

    /**
     * Tests whether the validity of a transaction
     * will be checked.
     * <p>
     *
     * @return true if checking validity, false otherwise.
     */
    public boolean isCheckingValidity() {
        return validityCheck;
    }

    /**
     * Sets the flag that controls whether the
     * validity of a transaction will be checked.
     * <p>
     *
     * @param b true if checking validity, false otherwise.
     */
    public void setCheckingValidity(boolean b) {
        validityCheck = b;
    }

    /**
     * Retrieves the current transaction ID.
     *
     * @return The current transaction ID, guaranteed to be within the valid range.
     */
    public int getTransactionID() {
        return transactionID.get();
    }

    /**
     * Atomically increments the global transaction ID counter and returns the
     * new value. The value wraps around to {@link Modbus#DEFAULT_TRANSACTION_ID}
     * when it reaches {@link Modbus#MAX_TRANSACTION_ID}.
     *
     * <p>This method is safe to call from multiple threads concurrently.
     *
     * @return the next unique transaction ID
     */
    static int nextTransactionID() {
        return transactionID.updateAndGet(current -> {
            if (current < Modbus.DEFAULT_TRANSACTION_ID || current >= Modbus.MAX_TRANSACTION_ID) {
                return Modbus.DEFAULT_TRANSACTION_ID;
            }
            return current + 1;
        });
    }

    /**
     * A useful method for getting a random sleep time based on an increment of the retry count and retry sleep time
     *
     * @param count Retry count
     * @return Random sleep time in milliseconds
     */
    long getRandomSleepTime(int count) {
        return (Modbus.RETRY_SLEEP_TIME / 2) + (long) (random.nextDouble() * Modbus.RETRY_SLEEP_TIME * count);
    }

    /**
     * Checks the validity of the transaction, by
     * checking if the values of the response correspond
     * to the values of the request.
     *
     * @throws ModbusException if the transaction is not valid.
     */
    void checkValidity() throws ModbusException {
        if (request != null && response != null) {
            if (request.getUnitID() != response.getUnitID()) {
                throw new ModbusIOException("Unit ID mismatch - Request [%s] Response [%s]", request.getHexMessage(), response.getHexMessage());
            }
            if (request.getFunctionCode() != response.getFunctionCode()) {
                throw new ModbusIOException("Function code mismatch - Request [%s] Response [%s]", request.getHexMessage(), response.getHexMessage());
            }
        }
    }

    /**
     * Executes this <tt>ModbusTransaction</tt>.
     * Locks the <tt>ModbusTransport</tt> for sending
     * the <tt>ModbusRequest</tt> and reading the
     * related <tt>ModbusResponse</tt>.
     * If reconnecting is activated the connection will
     * be opened for the transaction and closed afterwards.
     * <p>
     *
     * @throws ModbusException if an I/O error occurs,
     *                         or the response is a modbus protocol exception.
     */
    public abstract void execute() throws ModbusException;

}