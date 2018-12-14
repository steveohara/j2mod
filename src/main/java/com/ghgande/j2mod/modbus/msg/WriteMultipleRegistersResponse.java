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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Class implementing a <tt>WriteMultipleRegistersResponse</tt>. The
 * implementation directly correlates with the class 0 function <i>preset multiple
 * registers (FC 16)</i>. It encapsulates the corresponding response message.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class WriteMultipleRegistersResponse extends ModbusResponse {
    // instance attributes
    private int wordCount;
    private int reference;

    /**
     * Constructs a new <tt>WriteMultipleRegistersResponse</tt> instance.
     */
    public WriteMultipleRegistersResponse() {
        super();

        setFunctionCode(Modbus.WRITE_MULTIPLE_REGISTERS);
        setDataLength(4);
    }

    /**
     * Constructs a new <tt>WriteMultipleRegistersResponse</tt> instance.
     *
     * @param reference the offset to start writing from.
     * @param wordCount the number of words (registers) to be written.
     */
    public WriteMultipleRegistersResponse(int reference, int wordCount) {
        super();

        setFunctionCode(Modbus.WRITE_MULTIPLE_REGISTERS);
        setDataLength(4);

        this.reference = reference;
        this.wordCount = wordCount;
    }

    /**
     * Returns the reference of the register to start writing to with this
     * <tt>WriteMultipleRegistersResponse</tt>.
     * <p>
     *
     * @return the reference of the register to start writing to as <tt>int</tt>
     * .
     */
    public int getReference() {
        return reference;
    }

    /**
     * Sets the reference of the register to start writing to with this
     * <tt>WriteMultipleRegistersResponse</tt>.
     * <p>
     *
     * @param ref the reference of the register to start writing to as
     *            <tt>int</tt>.
     */
    public void setReference(int ref) {
        reference = ref;
    }

    /**
     * Returns the number of bytes that have been written.
     *
     * @return the number of bytes that have been written as <tt>int</tt>.
     */
    public int getByteCount() {
        return wordCount * 2;
    }

    /**
     * Returns the number of words that have been written. The returned value
     * should be half of the byte count of the response.
     * <p>
     *
     * @return the number of words that have been written as <tt>int</tt>.
     */
    public int getWordCount() {
        return wordCount;
    }

    /**
     * Sets the number of words that have been returned.
     *
     * @param count the number of words as <tt>int</tt>.
     */
    public void setWordCount(int count) {
        wordCount = count;
    }

    public void writeData(DataOutput dout) throws IOException {
        dout.write(getMessage());
    }

    public void readData(DataInput din) throws IOException {
        setReference(din.readUnsignedShort());
        setWordCount(din.readUnsignedShort());

        setDataLength(4);
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