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
import com.ghgande.j2mod.modbus.util.BitVector;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Class implementing a <tt>ReadInputDiscretesResponse</tt>.
 * The implementation directly correlates with the class 1
 * function <i>read input discretes (FC 2)</i>. It encapsulates
 * the corresponding response message.
 * <p>
 * Input Discretes are understood as bits that cannot be
 * manipulated (i.e. set or unset).
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public final class ReadInputDiscretesResponse
        extends ModbusResponse {

    //instance attributes
    private int bitCount;
    private BitVector discretes;

    /**
     * Constructs a new <tt>ReadInputDiscretesResponse</tt>
     * instance.
     */
    public ReadInputDiscretesResponse() {
        super();
        setFunctionCode(Modbus.READ_INPUT_DISCRETES);
    }

    /**
     * Constructs a new <tt>ReadInputDiscretesResponse</tt>
     * instance with a given count of input discretes
     * (i.e. bits).
     *
     * @param count the number of bits to be read.
     */
    public ReadInputDiscretesResponse(int count) {
        super();
        setFunctionCode(Modbus.READ_INPUT_DISCRETES);
        setBitCount(count);
    }

    /**
     * Returns the number of bits (i.e. input discretes)
     * read with the request.
     *
     * @return the number of bits that have been read.
     */
    public int getBitCount() {
        return bitCount;
    }

    /**
     * Sets the number of bits in this response.
     *
     * @param count the number of response bits as int.
     */
    public void setBitCount(int count) {
        bitCount = count;
        discretes = new BitVector(count);
        //set correct length, without counting unitid and fc
        setDataLength(discretes.byteSize() + 1);
    }

    /**
     * Returns the <tt>BitVector</tt> that stores
     * the collection of bits that have been read.
     * <p>
     *
     * @return the <tt>BitVector</tt> holding the
     * bits that have been read.
     */
    public BitVector getDiscretes() {
        return discretes;
    }

    /**
     * Convenience method that returns the state
     * of the bit at the given index.
     * <p>
     *
     * @param index the index of the input discrete
     *              for which the status should be returned.
     *
     * @return true if set, false otherwise.
     *
     * @throws IndexOutOfBoundsException if the
     *                                   index is out of bounds
     */
    public boolean getDiscreteStatus(int index) throws IndexOutOfBoundsException {

        return discretes.getBit(index);
    }

    /**
     * Sets the status of the given input discrete.
     *
     * @param index the index of the input discrete to be set.
     * @param b     true if to be set, false if to be reset.
     *
     * @throws IndexOutOfBoundsException if the given index exceeds bounds.
     */
    public void setDiscreteStatus(int index, boolean b) throws IndexOutOfBoundsException {
        discretes.setBit(index, b);
    }

    public void writeData(DataOutput dout) throws IOException {
        dout.writeByte(discretes.byteSize());
        dout.write(discretes.getBytes(), 0, discretes.byteSize());
    }

    public void readData(DataInput din) throws IOException {

        int count = din.readUnsignedByte();
        byte[] data = new byte[count];
        for (int k = 0; k < count; k++) {
            data[k] = din.readByte();
        }

        //decode bytes into bitvector
        discretes = BitVector.createBitVector(data);
        if (discretes != null) {
            bitCount = discretes.size();
        }

        //update data length
        setDataLength(count + 1);
    }

    public byte[] getMessage() {
        byte result[];
        int len = 1 + discretes.byteSize();

        result = new byte[len];
        result[0] = (byte)discretes.byteSize();
        System.arraycopy(discretes.getBytes(), 0, result, 1, discretes.byteSize());

        return result;
    }

}