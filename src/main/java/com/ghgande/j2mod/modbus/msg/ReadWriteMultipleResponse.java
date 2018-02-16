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
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

/**
 * Class implementing a <tt>ReadWriteMultipleResponse</tt>.
 *
 * @author Julie (jfh@ghgande.com)
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public final class ReadWriteMultipleResponse extends ModbusResponse {

    private int byteCount;
    private InputRegister[] registers;

    /**
     * Constructs a new <tt>ReadWriteMultipleResponse</tt> instance.
     *
     * @param registers the Register[] holding response registers.
     */
    public ReadWriteMultipleResponse(InputRegister[] registers) {
        super();

        setFunctionCode(Modbus.READ_WRITE_MULTIPLE);
        setDataLength(registers.length * 2 + 1);

        this.registers = Arrays.copyOf(registers, registers.length);
        byteCount = registers.length * 2;
    }

    /**
     * Constructs a new <tt>ReadWriteMultipleResponse</tt> instance.
     *
     * @param count the number of Register[] holding response registers.
     */
    public ReadWriteMultipleResponse(int count) {
        super();

        setFunctionCode(Modbus.READ_WRITE_MULTIPLE);
        setDataLength(count * 2 + 1);

        registers = new InputRegister[count];
        byteCount = count * 2;
    }

    /**
     * Constructs a new <tt>ReadWriteMultipleResponse</tt> instance.
     */
    public ReadWriteMultipleResponse() {
        super();

        setFunctionCode(Modbus.READ_WRITE_MULTIPLE);
    }

    /**
     * Returns the number of bytes that have been read.
     *
     * @return the number of bytes that have been read as <tt>int</tt>.
     */
    public int getByteCount() {
        return byteCount;
    }

    /**
     * Returns the number of words that have been read. The returned value
     * should be half of the the byte count of this
     * <tt>ReadWriteMultipleResponse</tt>.
     *
     * @return the number of words that have been read as <tt>int</tt>.
     */
    public int getWordCount() {
        return byteCount / 2;
    }

    /**
     * Returns the <tt>Register</tt> at the given position (relative to the
     * reference used in the request).
     *
     * @param index the relative index of the <tt>InputRegister</tt>.
     *
     * @return the register as <tt>InputRegister</tt>.
     *
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    public InputRegister getRegister(int index) {
        if (registers == null) {
            throw new IndexOutOfBoundsException("No registers defined!");
        }

        if (index < 0) {
            throw new IndexOutOfBoundsException("Negative index: " + index);
        }

        if (index >= getWordCount()) {
            throw new IndexOutOfBoundsException(index + " > " + getWordCount());
        }

        return registers[index];
    }

    /**
     * Returns the value of the register at the given position (relative to the
     * reference used in the request) interpreted as unsigned short.
     *
     * @param index the relative index of the register for which the value should
     *              be retrieved.
     *
     * @return the value as <tt>int</tt>.
     *
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    public int getRegisterValue(int index) throws IndexOutOfBoundsException {
        return getRegister(index).toUnsignedShort();
    }

    /**
     * Returns the reference to the array of registers read.
     *
     * @return a <tt>InputRegister[]</tt> instance.
     */
    public synchronized InputRegister[] getRegisters() {
        InputRegister[] dest = new InputRegister[registers.length];
        System.arraycopy(registers, 0, dest, 0, dest.length);
        return dest;
    }

    /**
     * Sets the entire block of registers for this response
     * @param registers Array of registers
     */
    public void setRegisters(InputRegister[] registers) {
        byteCount = registers.length * 2;
        setDataLength(byteCount + 1);

        this.registers = Arrays.copyOf(registers, registers.length);
    }

    public void writeData(DataOutput dout) throws IOException {
        dout.writeByte(byteCount);

        for (int k = 0; k < getWordCount(); k++) {
            dout.write(registers[k].toBytes());
        }
    }

    public void readData(DataInput din) throws IOException {
        byteCount = din.readUnsignedByte();

        registers = new Register[getWordCount()];

        for (int k = 0; k < getWordCount(); k++) {
            registers[k] = new SimpleRegister(din.readByte(), din.readByte());
        }

        setDataLength(byteCount + 1);
    }

    public byte[] getMessage() {
        byte result[];

        result = new byte[getWordCount() * 2 + 1];

        int offset = 0;
        result[offset++] = (byte)byteCount;

        for (InputRegister register : registers) {
            byte[] data = register.toBytes();

            result[offset++] = data[0];
            result[offset++] = data[1];
        }
        return result;
    }
}