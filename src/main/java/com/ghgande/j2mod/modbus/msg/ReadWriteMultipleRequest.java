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
package com.ghgande.j2mod.modbus.msg;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.io.NonWordDataHandler;
import com.ghgande.j2mod.modbus.net.AbstractModbusListener;
import com.ghgande.j2mod.modbus.procimg.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

/**
 * Class implementing a <tt>Read / Write Multiple Registers</tt> request.
 *
 * @author Julie Haugh
 * @author Julie Haugh
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public final class ReadWriteMultipleRequest extends ModbusRequest {
    private NonWordDataHandler nonWordDataHandler;
    private int readReference;
    private int readCount;
    private int writeReference;
    private int writeCount;
    private Register registers[];

    /**
     * Constructs a new <tt>Read/Write Multiple Registers Request</tt> instance.
     * @param unit Unit ID
     * @param readRef Register to read
     * @param writeCount Number of registers to write
     * @param writeRef Starting register to write
     * @param readCount Number of registers to read
     */
    public ReadWriteMultipleRequest(int unit, int readRef, int readCount, int writeRef, int writeCount) {
        super();

        setUnitID(unit);
        setFunctionCode(Modbus.READ_WRITE_MULTIPLE);

        // There is no additional data in this request.
        setDataLength(9 + writeCount * 2);

        readReference = readRef;
        this.readCount = readCount;
        writeReference = writeRef;
        this.writeCount = writeCount;
        registers = new Register[writeCount];
        for (int i = 0; i < writeCount; i++) {
            registers[i] = new SimpleRegister(0);
        }
    }

    /**
     * Constructs a new <tt>Read/Write Multiple Registers Request</tt> instance.
     * @param unit Unit ID
     */
    public ReadWriteMultipleRequest(int unit) {
        super();

        setUnitID(unit);
        setFunctionCode(Modbus.READ_WRITE_MULTIPLE);

        // There is no additional data in this request.
        setDataLength(9);
    }

    /**
     * Constructs a new <tt>Read/Write Multiple Registers Request</tt> instance.
     */
    public ReadWriteMultipleRequest() {
        super();

        setFunctionCode(Modbus.READ_WRITE_MULTIPLE);

        // There is no additional data in this request.
        setDataLength(9);
    }

    /**
     * createResponse -- create an empty response for this request.
     * @return Empty response object
     */
    public ModbusResponse getResponse() {
        ReadWriteMultipleResponse response;

        response = new ReadWriteMultipleResponse();

        // Copy any header data from the request.
        response.setHeadless(isHeadless());
        if (!isHeadless()) {
            response.setTransactionID(getTransactionID());
            response.setProtocolID(getProtocolID());
        }

        // Copy the unit ID and function code.
        response.setUnitID(getUnitID());
        response.setFunctionCode(getFunctionCode());

        return response;
    }

    @Override
    public ModbusResponse createResponse(AbstractModbusListener listener) {
        ReadWriteMultipleResponse response;
        InputRegister[] readRegs;
        Register[] writeRegs;

        // 1. get process image
        ProcessImage procimg = listener.getProcessImage(getUnitID());

        // 2. get input registers range
        try {
            // First the write
            writeRegs = procimg.getRegisterRange(getWriteReference(), getWriteWordCount());
            for (int i = 0; i < writeRegs.length; i++) {
                writeRegs[i].setValue(getRegister(i).getValue());
            }

            // And then the read
            readRegs = procimg.getRegisterRange(getReadReference(), getReadWordCount());
            InputRegister[] dummy = new InputRegister[readRegs.length];
            for (int i = 0; i < readRegs.length; i++) {
                dummy[i] = new SimpleInputRegister(readRegs[i].getValue());
            }
            readRegs = dummy;
        }
        catch (IllegalAddressException e) {
            return createExceptionResponse(Modbus.ILLEGAL_ADDRESS_EXCEPTION);
        }
        response = (ReadWriteMultipleResponse)getResponse();
        response.setRegisters(readRegs);

        return response;
    }
    /**
     * getReadReference - Returns the reference of the register to start writing
     * to with this <tt>ReadWriteMultipleRequest</tt>.
     * <p>
     *
     * @return the reference of the register to start writing to as <tt>int</tt>
     * .
     */
    public int getReadReference() {
        return readReference;
    }

    /**
     * setReadReference - Sets the reference of the register to writing to with
     * this <tt>ReadWriteMultipleRequest</tt>.
     * <p>
     *
     * @param ref the reference of the register to start writing to as
     *            <tt>int</tt>.
     */
    public void setReadReference(int ref) {
        readReference = ref;
    }

    /**
     * getWriteReference - Returns the reference of the register to start
     * writing to with this <tt>ReadWriteMultipleRequest</tt>.
     * <p>
     *
     * @return the reference of the register to start writing to as <tt>int</tt>
     * .
     */
    public int getWriteReference() {
        return writeReference;
    }

    /**
     * setWriteReference - Sets the reference of the register to write to with
     * this <tt>ReadWriteMultipleRequest</tt>.
     * <p>
     *
     * @param ref the reference of the register to start writing to as
     *            <tt>int</tt>.
     */
    public void setWriteReference(int ref) {
        writeReference = ref;
    }

    /**
     * getRegisters - Returns the registers to be written with this
     * <tt>ReadWriteMultipleRequest</tt>.
     * <p>
     *
     * @return the registers to be read as <tt>Register[]</tt>.
     */
    public synchronized Register[] getRegisters() {
        Register[] dest = new Register[registers.length];
        System.arraycopy(registers, 0, dest, 0, dest.length);
        return dest;
    }

    /**
     * setRegisters - Sets the registers to be written with this
     * <tt>ReadWriteMultipleRequest</tt>.
     * <p>
     *
     * @param registers the registers to be written as <tt>Register[]</tt>.
     */
    public void setRegisters(Register[] registers) {
        writeCount = registers != null ? registers.length : 0;
        this.registers = registers != null ? Arrays.copyOf(registers, registers.length) : null;
    }

    /**
     * getRegister - Returns the specified <tt>Register</tt>.
     *
     * @param index the index of the <tt>Register</tt>.
     *
     * @return the register as <tt>Register</tt>.
     *
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    public Register getRegister(int index) throws IndexOutOfBoundsException {
        if (index < 0) {
            throw new IndexOutOfBoundsException(index + " < 0");
        }

        if (index >= getWriteWordCount()) {
            throw new IndexOutOfBoundsException(index + " > " + getWriteWordCount());
        }

        return registers[index];
    }

    /**
     * getReadRegisterValue - Returns the value of the specified register
     * interpreted as unsigned short.
     *
     * @param index the relative index of the register for which the value should
     *              be retrieved.
     *
     * @return the value as <tt>int</tt>.
     *
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    public int getReadRegisterValue(int index) throws IndexOutOfBoundsException {
        return getRegister(index).toUnsignedShort();
    }

    /**
     * getByteCount - Returns the number of bytes representing the values to be
     * written.
     *
     * @return the number of bytes to be written as <tt>int</tt>.
     */
    public int getByteCount() {
        return getWriteWordCount() * 2;
    }

    /**
     * getWriteWordCount - Returns the number of words to be written.
     *
     * @return the number of words to be written as <tt>int</tt>.
     */
    public int getWriteWordCount() {
        return writeCount;
    }

    /**
     * setWriteWordCount - Sets the number of words to be written.
     *
     * @param count the number of words to be written as <tt>int</tt>.
     */
    public void setWriteWordCount(int count) {
        writeCount = count;
    }

    /**
     * getReadWordCount - Returns the number of words to be read.
     *
     * @return the number of words to be read as <tt>int</tt>.
     */
    public int getReadWordCount() {
        return readCount;
    }

    /**
     * setReadWordCount - Sets the number of words to be read.
     *
     * @param count the number of words to be read as <tt>int</tt>.
     */
    public void setReadWordCount(int count) {
        readCount = count;
    }

    /**
     * getNonWordDataHandler - Returns the actual non word data handler.
     *
     * @return the actual <tt>NonWordDataHandler</tt>.
     */
    public NonWordDataHandler getNonWordDataHandler() {
        return nonWordDataHandler;
    }

    /**
     * setNonWordDataHandler - Sets a non word data handler. A non-word data
     * handler is responsible for converting words from a Modbus packet into the
     * non-word values associated with the actual device's registers.
     *
     * @param dhandler a <tt>NonWordDataHandler</tt> instance.
     */
    public void setNonWordDataHandler(NonWordDataHandler dhandler) {
        nonWordDataHandler = dhandler;
    }

    /**
     * writeData -- output this Modbus message to dout.
     */
    public void writeData(DataOutput dout) throws IOException {
        dout.write(getMessage());
    }

    /**
     * readData -- read the values of the registers to be written, along with
     * the reference and count for the registers to be read.
     */
    public void readData(DataInput input) throws IOException {
        readReference = input.readUnsignedShort();
        readCount = input.readUnsignedShort();
        writeReference = input.readUnsignedShort();
        writeCount = input.readUnsignedShort();
        int byteCount = input.readUnsignedByte();

        if (nonWordDataHandler == null) {
            byte buffer[] = new byte[byteCount];
            input.readFully(buffer, 0, byteCount);

            int offset = 0;
            registers = new Register[writeCount];

            for (int register = 0; register < writeCount; register++) {
                registers[register] = new SimpleRegister(buffer[offset], buffer[offset + 1]);
                offset += 2;
            }
        }
        else {
            nonWordDataHandler.readData(input, writeReference, writeCount);
        }
    }

    /**
     * getMessage -- return a prepared message.
     * @return prepared message
     */
    public byte[] getMessage() {
        byte results[] = new byte[9 + 2 * getWriteWordCount()];

        results[0] = (byte)(readReference >> 8);
        results[1] = (byte)(readReference & 0xFF);
        results[2] = (byte)(readCount >> 8);
        results[3] = (byte)(readCount & 0xFF);
        results[4] = (byte)(writeReference >> 8);
        results[5] = (byte)(writeReference & 0xFF);
        results[6] = (byte)(writeCount >> 8);
        results[7] = (byte)(writeCount & 0xFF);
        results[8] = (byte)(writeCount * 2);

        int offset = 9;
        for (int i = 0; i < writeCount; i++) {
            Register reg = getRegister(i);
            byte[] bytes = reg.toBytes();

            results[offset++] = bytes[0];
            results[offset++] = bytes[1];
        }
        return results;
    }
}
