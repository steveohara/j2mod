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
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;

import java.util.Random;

/**
 * Interface defining a ModbusTransaction.
 * <p>
 * A transaction is defined by the sequence of
 * sending a request message and receiving a
 * related response message.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public abstract class ModbusTransaction {

    protected AbstractModbusTransport transport;
    protected ModbusRequest request;
    protected ModbusResponse response;
    boolean validityCheck = Modbus.DEFAULT_VALIDITYCHECK;
    int retries = Modbus.DEFAULT_RETRIES;
    private Random random = new Random(System.nanoTime());
    static int transactionID = Modbus.DEFAULT_TRANSACTION_ID;

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
     * getTransactionID -- get the next transaction ID to use.
     * @return next transaction ID to use
     */
    synchronized public int getTransactionID() {
        /*
         * Ensure that the transaction ID is in the valid range between
         * 1 and MAX_TRANSACTION_ID (65534).  If not, the value will be forced
         * to 1.
         */
        if (transactionID <= 0 && isCheckingValidity()) {
            transactionID = 1;
        }
        if (transactionID >= Modbus.MAX_TRANSACTION_ID) {
            transactionID = 1;
        }
        return transactionID;
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