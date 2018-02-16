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
import com.ghgande.j2mod.modbus.net.AbstractModbusListener;
import com.ghgande.j2mod.modbus.procimg.IllegalAddressException;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.ProcessImage;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Class implementing a <tt>ReadInputRegistersRequest</tt>. The implementation
 * directly correlates with the class 0 function <i>read multiple registers (FC
 * 4)</i>. It encapsulates the corresponding request message.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public final class ReadInputRegistersRequest extends ModbusRequest {

    // instance attributes
    private int reference;
    private int wordCount;

    /**
     * Constructs a new <tt>ReadInputRegistersRequest</tt> instance.
     */
    public ReadInputRegistersRequest() {
        super();

        setFunctionCode(Modbus.READ_INPUT_REGISTERS);
        // 4 bytes (unit id and function code is excluded)
        setDataLength(4);
    }

    /**
     * Constructs a new <tt>ReadInputRegistersRequest</tt> instance with a given
     * reference and count of words to be read.
     * <p>
     *
     * @param ref   the reference number of the register to read from.
     * @param count the number of words to be read.
     */
    public ReadInputRegistersRequest(int ref, int count) {
        super();

        setFunctionCode(Modbus.READ_INPUT_REGISTERS);
        // 4 bytes (unit id and function code is excluded)
        setDataLength(4);

        setReference(ref);
        setWordCount(count);
    }

    public ReadInputRegistersResponse getResponse() {
        ReadInputRegistersResponse response = new ReadInputRegistersResponse();

        response.setUnitID(getUnitID());
        response.setHeadless(isHeadless());
        response.setWordCount(getWordCount());

        if (!isHeadless()) {
            response.setProtocolID(getProtocolID());
            response.setTransactionID(getTransactionID());
        }
        return response;
    }

    @Override
    public ModbusResponse createResponse(AbstractModbusListener listener) {
        ReadInputRegistersResponse response;
        InputRegister[] inpregs;

        // 1. get process image
        ProcessImage procimg = listener.getProcessImage(getUnitID());
        // 2. get input registers range
        try {
            inpregs = procimg.getInputRegisterRange(getReference(), getWordCount());
        }
        catch (IllegalAddressException iaex) {
            return createExceptionResponse(Modbus.ILLEGAL_ADDRESS_EXCEPTION);
        }
        response = getResponse();
        response.setRegisters(inpregs);

        return response;
    }

    /**
     * Returns the reference of the register to to start reading from with this
     * <tt>ReadInputRegistersRequest</tt>.
     * <p>
     *
     * @return the reference of the register to start reading from as
     * <tt>int</tt>.
     */
    public int getReference() {
        return reference;
    }

    /**
     * Sets the reference of the register to start reading from with this
     * <tt>ReadInputRegistersRequest</tt>.
     * <p>
     *
     * @param ref the reference of the register to start reading from.
     */
    public void setReference(int ref) {
        reference = ref;
    }

    /**
     * Returns the number of words to be read with this
     * <tt>ReadInputRegistersRequest</tt>.
     * <p>
     *
     * @return the number of words to be read as <tt>int</tt>.
     */
    public int getWordCount() {
        return wordCount;
    }

    /**
     * Sets the number of words to be read with this
     * <tt>ReadInputRegistersRequest</tt>.
     * <p>
     *
     * @param count the number of words to be read.
     */
    public void setWordCount(int count) {
        wordCount = count;
    }

    public void writeData(DataOutput dout) throws IOException {
        dout.writeShort(reference);
        dout.writeShort(wordCount);
    }

    public void readData(DataInput din) throws IOException {
        reference = din.readUnsignedShort();
        wordCount = din.readUnsignedShort();
    }

    public byte[] getMessage() {
        byte result[] = new byte[4];
        result[0] = (byte)((reference >> 8) & 0xff);
        result[1] = (byte)(reference & 0xff);
        result[2] = (byte)((wordCount >> 8) & 0xff);
        result[3] = (byte)(wordCount & 0xff);

        return result;
    }
}