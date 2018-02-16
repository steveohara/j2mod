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
import com.ghgande.j2mod.modbus.procimg.DigitalOut;
import com.ghgande.j2mod.modbus.procimg.IllegalAddressException;
import com.ghgande.j2mod.modbus.procimg.ProcessImage;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Class implementing a <tt>ReadCoilsRequest</tt>. The implementation directly
 * correlates with the class 1 function <i>read coils (FC 1)</i>. It
 * encapsulates the corresponding request message.
 *
 * <p>
 * Coils are understood as bits that can be manipulated (i.e. set or unset).
 *
 * @author Dieter Wimberger
 * @author jfhaugh
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public final class ReadCoilsRequest extends ModbusRequest {

    // instance attributes
    private int reference;
    private int bitCount;

    /**
     * Constructs a new <tt>ReadCoilsRequest</tt> instance.
     */
    public ReadCoilsRequest() {
        super();

        setFunctionCode(Modbus.READ_COILS);
        setDataLength(4);
    }

    /**
     * Constructs a new <tt>ReadCoilsRequest</tt> instance with a given
     * reference and count of coils (i.e. bits) to be read.
     * <p>
     *
     * @param ref   the reference number of the register to read from.
     * @param count the number of bits to be read.
     */
    public ReadCoilsRequest(int ref, int count) {
        super();

        setFunctionCode(Modbus.READ_COILS);
        setDataLength(4);

        setReference(ref);
        setBitCount(count);
    }

    @Override
    public ReadCoilsResponse getResponse() {
        ReadCoilsResponse response;
        response = new ReadCoilsResponse(bitCount);

        // transfer header data
        if (!isHeadless()) {
            response.setTransactionID(getTransactionID());
            response.setProtocolID(getProtocolID());
        }
        else {
            response.setHeadless();
        }
        response.setUnitID(getUnitID());

        return response;
    }

    @Override
    public ModbusResponse createResponse(AbstractModbusListener listener) {
        ModbusResponse response;
        DigitalOut[] douts;

        // 1. get process image
        ProcessImage procimg = listener.getProcessImage(getUnitID());
        // 2. get input discretes range
        try {
            douts = procimg.getDigitalOutRange(getReference(), getBitCount());
        }
        catch (IllegalAddressException e) {
            response = new IllegalAddressExceptionResponse();
            response.setUnitID(getUnitID());
            response.setFunctionCode(getFunctionCode());

            return response;
        }
        response = getResponse();

        // Populate the discrete values from the process image.
        for (int i = 0; i < douts.length; i++) {
            ((ReadCoilsResponse)response).setCoilStatus(i, douts[i].isSet());
        }

        return response;
    }

    /**
     * Returns the reference of the register to to start reading from with this
     * <tt>ReadCoilsRequest</tt>.
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
     * <tt>ReadCoilsRequest</tt>.
     * <p>
     *
     * @param ref the reference of the register to start reading from.
     */
    public void setReference(int ref) {
        reference = ref;
    }

    /**
     * Returns the number of bits (i.e. coils) to be read with this
     * <tt>ReadCoilsRequest</tt>.
     * <p>
     *
     * @return the number of bits to be read.
     */
    public int getBitCount() {
        return bitCount;
    }

    /**
     * Sets the number of bits (i.e. coils) to be read with this
     * <tt>ReadCoilsRequest</tt>.
     * <p>
     *
     * @param count the number of bits to be read.
     */
    public void setBitCount(int count) {
        if (count > Modbus.MAX_BITS) {
            throw new IllegalArgumentException("Maximum bitcount exceeded");
        }
        else {
            bitCount = count;
        }
    }

    @Override
    public void writeData(DataOutput dout) throws IOException {
        dout.write(getMessage());
    }

    @Override
    public void readData(DataInput din) throws IOException {
        reference = din.readUnsignedShort();
        bitCount = din.readUnsignedShort();
    }

    public byte[] getMessage() {
        byte result[] = new byte[4];

        result[0] = (byte)((reference >> 8) & 0xff);
        result[1] = (byte)((reference & 0xff));
        result[2] = (byte)((bitCount >> 8) & 0xff);
        result[3] = (byte)((bitCount & 0xff));

        return result;
    }
}
