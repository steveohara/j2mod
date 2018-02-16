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
import com.ghgande.j2mod.modbus.procimg.DigitalIn;
import com.ghgande.j2mod.modbus.procimg.IllegalAddressException;
import com.ghgande.j2mod.modbus.procimg.ProcessImage;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Class implementing a <tt>ReadInputDiscretesRequest</tt>. The implementation
 * directly correlates with the class 1 function <i>read input discretes (FC
 * 2)</i>. It encapsulates the corresponding request message.
 * <p>
 * Input Discretes are understood as bits that cannot be manipulated (i.e. set
 * or unset).
 *
 * @author Dieter Wimberger
 * @author jfhaugh
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public final class ReadInputDiscretesRequest extends ModbusRequest {

    // instance attributes
    private int reference;
    private int bitCount;

    /**
     * Constructs a new <tt>ReadInputDiscretesRequest</tt> instance.
     */
    public ReadInputDiscretesRequest() {
        super();

        setFunctionCode(Modbus.READ_INPUT_DISCRETES);

        // Two bytes for count, two bytes for offset.
        setDataLength(4);
    }

    /**
     * Constructs a new <tt>ReadInputDiscretesRequest</tt> instance with a given
     * reference and count of input discretes (i.e. bits) to be read.
     * <p>
     *
     * @param ref   the reference number of the register to read from.
     * @param count the number of bits to be read.
     */
    public ReadInputDiscretesRequest(int ref, int count) {
        super();

        setFunctionCode(Modbus.READ_INPUT_DISCRETES);
        // 4 bytes (unit id and function code is excluded)
        setDataLength(4);
        setReference(ref);
        setBitCount(count);
    }

    /**
     * Constructs a response to match this request.
     *
     * <p>Used by slave implementations to construct the appropriate
     * response.
     *
     * @return Discretes response
     */
    public ReadInputDiscretesResponse getResponse() {
        ReadInputDiscretesResponse response = new ReadInputDiscretesResponse(getBitCount());

        response.setUnitID(getUnitID());
        response.setFunctionCode(getFunctionCode());

        response.setHeadless(isHeadless());
        if (!isHeadless()) {
            response.setTransactionID(getTransactionID());
            response.setProtocolID(getProtocolID());
        }
        return response;
    }

    @Override
    public ModbusResponse createResponse(AbstractModbusListener listener) {
        ReadInputDiscretesResponse response;
        DigitalIn[] dins;

        // 1. get process image
        ProcessImage procimg = listener.getProcessImage(getUnitID());
        // 2. get input discretes range
        try {
            dins = procimg.getDigitalInRange(getReference(), getBitCount());
        }
        catch (IllegalAddressException e) {
            return createExceptionResponse(Modbus.ILLEGAL_ADDRESS_EXCEPTION);
        }
        response = getResponse();

        // Populate the discrete values from the process image.
        for (int i = 0; i < dins.length; i++) {
            response.setDiscreteStatus(i, dins[i].isSet());
        }

        return response;
    }

    /**
     * Returns the reference of the discrete to to start reading from with
     * this <tt>ReadInputDiscretesRequest</tt>.
     *
     * @return the reference of the discrete to start reading from as
     * <tt>int</tt>.
     */
    public int getReference() {
        return reference;
    }

    /**
     * Sets the reference of the register to start reading from with this
     * <tt>ReadInputDiscretesRequest</tt>.
     * <p>
     *
     * @param ref the reference of the register to start reading from.
     */
    public void setReference(int ref) {
        if (ref < 0 || bitCount + ref >= 65536) {
            throw new IllegalArgumentException();
        }

        reference = ref;
    }

    /**
     * Returns the number of bits (i.e. input discretes) to be read with this
     * <tt>ReadInputDiscretesRequest</tt>.
     * <p>
     *
     * @return the number of bits to be read.
     */
    public int getBitCount() {
        return bitCount;
    }

    /**
     * Sets the number of bits (i.e. input discretes) to be read with this
     * <tt>ReadInputDiscretesRequest</tt>.
     *
     * @param count the number of bits to be read.
     */
    public void setBitCount(int count) {
        if (count < 0 || count > 2000 || count + reference >= 65536) {
            throw new IllegalArgumentException();
        }

        bitCount = count;
    }

    public void writeData(DataOutput dout) throws IOException {
        dout.writeShort(reference);
        dout.writeShort(bitCount);
    }

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
