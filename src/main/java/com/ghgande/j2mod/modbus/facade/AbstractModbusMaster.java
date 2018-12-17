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
package com.ghgande.j2mod.modbus.facade;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.io.AbstractModbusTransport;
import com.ghgande.j2mod.modbus.io.ModbusTransaction;
import com.ghgande.j2mod.modbus.msg.*;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.util.BitVector;

/**
 * Modbus/TCP Master facade - common methods for all the facade implementations
 * The emphasis is in making callas to Modbus devices as simple as possible
 * for the most common Function Codes.
 * This class makes sure that no NPE is raised and that the methods are thread-safe.
 *
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
abstract public class AbstractModbusMaster {

    private static final int DEFAULT_UNIT_ID = 1;

    protected ModbusTransaction transaction;
    private ReadCoilsRequest readCoilsRequest;
    private ReadInputDiscretesRequest readInputDiscretesRequest;
    private WriteCoilRequest writeCoilRequest;
    private WriteMultipleCoilsRequest writeMultipleCoilsRequest;
    private ReadInputRegistersRequest readInputRegistersRequest;
    private ReadMultipleRegistersRequest readMultipleRegistersRequest;
    private WriteSingleRegisterRequest writeSingleRegisterRequest;
    private WriteMultipleRegistersRequest writeMultipleRegistersRequest;
    private MaskWriteRegisterRequest maskWriteRegisterRequest;
    protected int timeout = Modbus.DEFAULT_TIMEOUT;

    /**
     * Sets the transaction to use
     *
     * @param transaction Transaction to use
     */
    protected synchronized void setTransaction(ModbusTransaction transaction) {
        this.transaction = transaction;
    }

    /**
     * Connects this <tt>ModbusTCPMaster</tt> with the slave.
     *
     * @throws Exception if the connection cannot be established.
     */
    abstract public void connect() throws Exception;

    /**
     * Disconnects this <tt>ModbusTCPMaster</tt> from the slave.
     */
    abstract public void disconnect();

    /**
     * Reads a given number of coil states from the slave.
     *
     * Note that the number of bits in the bit vector will be
     * forced to the number originally requested.
     *
     * @param unitId the slave unit id.
     * @param ref    the offset of the coil to start reading from.
     * @param count  the number of coil states to be read.
     *
     * @return a <tt>BitVector</tt> instance holding the
     * received coil states.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public BitVector readCoils(int unitId, int ref, int count) throws ModbusException {
        checkTransaction();
        if (readCoilsRequest == null) {
            readCoilsRequest = new ReadCoilsRequest();
        }
        readCoilsRequest.setUnitID(unitId);
        readCoilsRequest.setReference(ref);
        readCoilsRequest.setBitCount(count);
        transaction.setRequest(readCoilsRequest);
        transaction.execute();
        BitVector bv = ((ReadCoilsResponse) getAndCheckResponse()).getCoils();
        bv.forceSize(count);
        return bv;
    }

    /**
     * Writes a coil state to the slave.
     *
     * @param unitId the slave unit id.
     * @param ref    the offset of the coil to be written.
     * @param state  the coil state to be written.
     *
     * @return the state of the coil as returned from the slave.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public boolean writeCoil(int unitId, int ref, boolean state) throws ModbusException {
        checkTransaction();
        if (writeCoilRequest == null) {
            writeCoilRequest = new WriteCoilRequest();
        }
        writeCoilRequest.setUnitID(unitId);
        writeCoilRequest.setReference(ref);
        writeCoilRequest.setCoil(state);
        transaction.setRequest(writeCoilRequest);
        transaction.execute();
        return ((WriteCoilResponse) getAndCheckResponse()).getCoil();
    }

    /**
     * Writes a given number of coil states to the slave.
     *
     * Note that the number of coils to be written is given
     * implicitly, through {@link BitVector#size()}.
     *
     * @param unitId the slave unit id.
     * @param ref    the offset of the coil to start writing to.
     * @param coils  a <tt>BitVector</tt> which holds the coil states to be written.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public void writeMultipleCoils(int unitId, int ref, BitVector coils) throws ModbusException {
        checkTransaction();
        if (writeMultipleCoilsRequest == null) {
            writeMultipleCoilsRequest = new WriteMultipleCoilsRequest();
        }
        writeMultipleCoilsRequest.setUnitID(unitId);
        writeMultipleCoilsRequest.setReference(ref);
        writeMultipleCoilsRequest.setCoils(coils);
        transaction.setRequest(writeMultipleCoilsRequest);
        transaction.execute();
    }

    /**
     * Reads a given number of input discrete states from the slave.
     *
     * Note that the number of bits in the bit vector will be
     * forced to the number originally requested.
     *
     * @param unitId the slave unit id.
     * @param ref    the offset of the input discrete to start reading from.
     * @param count  the number of input discrete states to be read.
     *
     * @return a <tt>BitVector</tt> instance holding the received input discrete
     * states.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public BitVector readInputDiscretes(int unitId, int ref, int count) throws ModbusException {
        checkTransaction();
        if (readInputDiscretesRequest == null) {
            readInputDiscretesRequest = new ReadInputDiscretesRequest();
        }
        readInputDiscretesRequest.setUnitID(unitId);
        readInputDiscretesRequest.setReference(ref);
        readInputDiscretesRequest.setBitCount(count);
        transaction.setRequest(readInputDiscretesRequest);
        transaction.execute();
        BitVector bv = ((ReadInputDiscretesResponse)getAndCheckResponse()).getDiscretes();
        bv.forceSize(count);
        return bv;
    }

    /**
     * Reads a given number of input registers from the slave.
     *
     * Note that the number of input registers returned (i.e. array length)
     * will be according to the number received in the slave response.
     *
     * @param unitId the slave unit id.
     * @param ref    the offset of the input register to start reading from.
     * @param count  the number of input registers to be read.
     *
     * @return a <tt>InputRegister[]</tt> with the received input registers.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public InputRegister[] readInputRegisters(int unitId, int ref, int count) throws ModbusException {
        checkTransaction();
        if (readInputRegistersRequest == null) {
            readInputRegistersRequest = new ReadInputRegistersRequest();
        }
        readInputRegistersRequest.setUnitID(unitId);
        readInputRegistersRequest.setReference(ref);
        readInputRegistersRequest.setWordCount(count);
        transaction.setRequest(readInputRegistersRequest);
        transaction.execute();
        return ((ReadInputRegistersResponse) getAndCheckResponse()).getRegisters();
    }

    /**
     * Reads a given number of registers from the slave.
     *
     * Note that the number of registers returned (i.e. array length)
     * will be according to the number received in the slave response.
     *
     * @param unitId the slave unit id.
     * @param ref    the offset of the register to start reading from.
     * @param count  the number of registers to be read.
     *
     * @return a <tt>Register[]</tt> holding the received registers.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public Register[] readMultipleRegisters(int unitId, int ref, int count) throws ModbusException {
        checkTransaction();
        if (readMultipleRegistersRequest == null) {
            readMultipleRegistersRequest = new ReadMultipleRegistersRequest();
        }
        readMultipleRegistersRequest.setUnitID(unitId);
        readMultipleRegistersRequest.setReference(ref);
        readMultipleRegistersRequest.setWordCount(count);
        transaction.setRequest(readMultipleRegistersRequest);
        transaction.execute();
        return ((ReadMultipleRegistersResponse) getAndCheckResponse()).getRegisters();
    }

    /**
     * Writes a single register to the slave.
     *
     * @param unitId   the slave unit id.
     * @param ref      the offset of the register to be written.
     * @param register a <tt>Register</tt> holding the value of the register
     *                 to be written.
     *
     * @return the value of the register as returned from the slave.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public int writeSingleRegister(int unitId, int ref, Register register) throws ModbusException {
        checkTransaction();
        if (writeSingleRegisterRequest == null) {
            writeSingleRegisterRequest = new WriteSingleRegisterRequest();
        }
        writeSingleRegisterRequest.setUnitID(unitId);
        writeSingleRegisterRequest.setReference(ref);
        writeSingleRegisterRequest.setRegister(register);
        transaction.setRequest(writeSingleRegisterRequest);
        transaction.execute();
        return ((WriteSingleRegisterResponse) getAndCheckResponse()).getRegisterValue();
    }

    /**
     * Writes a number of registers to the slave.
     *
     * @param unitId    the slave unit id.
     * @param ref       the offset of the register to start writing to.
     * @param registers a <tt>Register[]</tt> holding the values of
     *                  the registers to be written.
     *
     * @return the number of registers that have been written.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public int writeMultipleRegisters(int unitId, int ref, Register[] registers) throws ModbusException {
        checkTransaction();
        if (writeMultipleRegistersRequest == null) {
            writeMultipleRegistersRequest = new WriteMultipleRegistersRequest();
        }
        writeMultipleRegistersRequest.setUnitID(unitId);
        writeMultipleRegistersRequest.setReference(ref);
        writeMultipleRegistersRequest.setRegisters(registers);
        transaction.setRequest(writeMultipleRegistersRequest);
        transaction.execute();
        return ((WriteMultipleRegistersResponse) transaction.getResponse()).getWordCount();
    }

    /**
     * Mask write a single register to the slave.
     *
     * @param unitId    the slave unit id.
     * @param ref       the offset of the register to start writing to.
     * @param andMask   AND mask.
     * @param orMask    OR mask.
     *
     * @return true if success, i.e. response data equals to request data, false otherwise.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public boolean maskWriteRegister(int unitId, int ref, int andMask, int orMask) throws ModbusException {
        checkTransaction();
        if (maskWriteRegisterRequest == null) {
            maskWriteRegisterRequest = new MaskWriteRegisterRequest();
        }
        maskWriteRegisterRequest.setUnitID(unitId);
        maskWriteRegisterRequest.setReference(ref);
        maskWriteRegisterRequest.setAndMask(andMask);
        maskWriteRegisterRequest.setOrMask(orMask);
        transaction.setRequest(maskWriteRegisterRequest);
        transaction.execute();

        MaskWriteRegisterResponse response = (MaskWriteRegisterResponse) getAndCheckResponse();
        return response.getReference() == maskWriteRegisterRequest.getReference() &&
               response.getAndMask() == maskWriteRegisterRequest.getAndMask() &&
               response.getOrMask() == maskWriteRegisterRequest.getOrMask();
    }

    /**
     * Reads a given number of coil states from the slave.
     *
     * Note that the number of bits in the bit vector will be
     * forced to the number originally requested.
     *
     * @param ref   the offset of the coil to start reading from.
     * @param count the number of coil states to be read.
     *
     * @return a <tt>BitVector</tt> instance holding the
     * received coil states.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public BitVector readCoils(int ref, int count) throws ModbusException {
        return readCoils(DEFAULT_UNIT_ID, ref, count);
    }

    /**
     * Writes a coil state to the slave.
     *
     * @param ref   the offset of the coil to be written.
     * @param state the coil state to be written.
     *
     * @return the state of the coil as returned from the slave.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public boolean writeCoil(int ref, boolean state) throws ModbusException {
        return writeCoil(DEFAULT_UNIT_ID, ref, state);
    }

    /**
     * Writes a given number of coil states to the slave.
     *
     * Note that the number of coils to be written is given
     * implicitly, through {@link BitVector#size()}.
     *
     * @param ref   the offset of the coil to start writing to.
     * @param coils a <tt>BitVector</tt> which holds the coil states to be written.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public void writeMultipleCoils(int ref, BitVector coils) throws ModbusException {
        writeMultipleCoils(DEFAULT_UNIT_ID, ref, coils);
    }

    /**
     * Reads a given number of input discrete states from the slave.
     *
     * Note that the number of bits in the bit vector will be
     * forced to the number originally requested.
     *
     * @param ref   the offset of the input discrete to start reading from.
     * @param count the number of input discrete states to be read.
     *
     * @return a <tt>BitVector</tt> instance holding the received input discrete
     * states.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public BitVector readInputDiscretes(int ref, int count) throws ModbusException {
        return readInputDiscretes(DEFAULT_UNIT_ID, ref, count);
    }

    /**
     * Reads a given number of input registers from the slave.
     *
     * Note that the number of input registers returned (i.e. array length)
     * will be according to the number received in the slave response.
     *
     * @param ref   the offset of the input register to start reading from.
     * @param count the number of input registers to be read.
     *
     * @return a <tt>InputRegister[]</tt> with the received input registers.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public InputRegister[] readInputRegisters(int ref, int count) throws ModbusException {
        return readInputRegisters(DEFAULT_UNIT_ID, ref, count);
    }

    /**
     * Reads a given number of registers from the slave.
     *
     * Note that the number of registers returned (i.e. array length)
     * will be according to the number received in the slave response.
     *
     * @param ref   the offset of the register to start reading from.
     * @param count the number of registers to be read.
     *
     * @return a <tt>Register[]</tt> holding the received registers.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public Register[] readMultipleRegisters(int ref, int count) throws ModbusException {
        return readMultipleRegisters(DEFAULT_UNIT_ID, ref, count);
    }

    /**
     * Writes a single register to the slave.
     *
     * @param ref      the offset of the register to be written.
     * @param register a <tt>Register</tt> holding the value of the register
     *                 to be written.
     *
     * @return the value of the register as returned from the slave.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public int writeSingleRegister(int ref, Register register) throws ModbusException {
        return writeSingleRegister(DEFAULT_UNIT_ID, ref, register);
    }

    /**
     * Writes a number of registers to the slave.
     *
     * @param ref       the offset of the register to start writing to.
     * @param registers a <tt>Register[]</tt> holding the values of
     *                  the registers to be written.
     *
     * @return the number of registers that have been written.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public int writeMultipleRegisters(int ref, Register[] registers) throws ModbusException {
        return writeMultipleRegisters(DEFAULT_UNIT_ID, ref, registers);
    }

    /**
     * Mask write a single register to the slave.
     *
     * @param ref       the offset of the register to start writing to.
     * @param andMask   AND mask.
     * @param orMask    OR mask.
     *
     * @return true if success, i.e. response data equals to request data, false otherwise.
     *
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public boolean maskWriteRegister(int ref, int andMask, int orMask) throws ModbusException {
        return maskWriteRegister(DEFAULT_UNIT_ID, ref, andMask, orMask);
    }

    /**
     * Reads the response from the transaction
     * If there is no response, then it throws an error
     *
     * @return Modbus response
     *
     * @throws ModbusException If response is null
     */
    private ModbusResponse getAndCheckResponse() throws ModbusException {
        ModbusResponse res = transaction.getResponse();
        if (res == null) {
            throw new ModbusException("No response");
        }
        return res;
    }

    /**
     * Checks to make sure there is a transaction to use
     *
     * @throws ModbusException If transaction is null
     */
    private void checkTransaction() throws ModbusException {
        if (transaction == null) {
            throw new ModbusException("No transaction created, probably not connected");
        }
    }

    /**
     * Returns the receive timeout in milliseconds
     *
     * @return Timeout in milliseconds
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Sets the receive timeout
     *
     * @param timeout Timeout in milliseconds
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * Set the amount of retries for opening
     * the connection for executing the transaction.
     *
     * @param retries the amount of retries as <tt>int</tt>.
     */
    synchronized public void setRetries(int retries) {
        if (transaction != null) {
            transaction.setRetries(retries);
        }
    }

    /**
     * Sets the flag that controls whether the
     * validity of a transaction will be checked.
     *
     * @param b true if checking validity, false otherwise.
     */
    synchronized public void setCheckingValidity(boolean b) {
        if (transaction != null) {
            transaction.setCheckingValidity(b);
        }
    }

    /**
     * Returns the transport being used by the
     *
     * @return ModbusTransport
     */
    public abstract AbstractModbusTransport getTransport();

}