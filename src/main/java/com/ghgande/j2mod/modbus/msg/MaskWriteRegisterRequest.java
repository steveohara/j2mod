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
import com.ghgande.j2mod.modbus.procimg.ProcessImage;
import com.ghgande.j2mod.modbus.procimg.Register;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Class implementing a <tt>Mask Write Register</tt> request.
 *
 * @author Julie Haugh (jfh@ghgande.com)
 * @author jfhaugh (jfh@ghgande.com)
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public final class MaskWriteRegisterRequest extends ModbusRequest {
    private int reference;
    private int andMask;
    private int orMask;

    /**
     * Constructs a new <tt>Mask Write Register</tt> request.
     *
     * @param ref     Register
     * @param andMask AND Mask to use
     * @param orMask  OR Mask to use
     */
    public MaskWriteRegisterRequest(int ref, int andMask, int orMask) {
        super();

        setFunctionCode(Modbus.MASK_WRITE_REGISTER);
        setReference(ref);
        setAndMask(andMask);
        setOrMask(orMask);

        setDataLength(6);
    }

    /**
     * Constructs a new <tt>Mask Write Register</tt> request.
     * instance.
     */
    public MaskWriteRegisterRequest() {
        super();

        setFunctionCode(Modbus.MASK_WRITE_REGISTER);
        setDataLength(6);
    }

    /**
     * getReference
     * @return the reference field
     */
    public int getReference() {
        return reference;
    }

    /**
     * setReference -- set the reference field.
     * @param ref the reference field
     */
    public void setReference(int ref) {
        reference = ref;
    }

    /**
     * getAndMask -- return the AND mask value;
     *
     * @return int
     */
    public int getAndMask() {
        return andMask;
    }

    /**
     * setAndMask -- set AND mask
     * @param mask AND mask
     */
    public void setAndMask(int mask) {
        andMask = mask;
    }

    /**
     * getOrMask -- return the OR mask value;
     *
     * @return int
     */
    public int getOrMask() {
        return orMask;
    }

    /**
     * setOrMask -- set OR mask
     * @param mask OR mask
     */
    public void setOrMask(int mask) {
        orMask = mask;
    }

    /**
     * getResponse -- create an empty response for this request.
     * @return empty response for this request
     */
    public ModbusResponse getResponse() {
        MaskWriteRegisterResponse response;

        response = new MaskWriteRegisterResponse();

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
        MaskWriteRegisterResponse response;

        // Get the process image.
        ProcessImage procimg = listener.getProcessImage(getUnitID());
        try {
            Register register = procimg.getRegister(reference);

            /*
             * Get the original value.  The AND mask will first be
             * applied to clear any bits, then the OR mask will be
             * applied to set them.
             */
            int value = register.getValue();

            value = (value & andMask) | (orMask & ~andMask);

            // Store the modified value back where it came from.
            register.setValue(value);
        }
        catch (IllegalAddressException iaex) {
            return createExceptionResponse(Modbus.ILLEGAL_ADDRESS_EXCEPTION);
        }
        response = (MaskWriteRegisterResponse)getResponse();

        response.setReference(reference);
        response.setAndMask(andMask);
        response.setOrMask(orMask);

        return response;
    }

    /**
     * writeData -- output this Modbus message to dout.
     * @throws java.io.IOException
     */
    public void writeData(DataOutput dout) throws IOException {
        dout.write(getMessage());
    }

    /**
     * readData -- dummy function.  There is no data with the request.
     * @throws java.io.IOException
     */
    public void readData(DataInput din) throws IOException {
        reference = din.readUnsignedShort();
        andMask = din.readUnsignedShort();
        orMask = din.readUnsignedShort();
    }

    /**
     * getMessage -- return an empty array as there is no data for
     * this request.
     * @return message payload
     */
    public byte[] getMessage() {
        byte results[] = new byte[6];

        results[0] = (byte)(reference >> 8);
        results[1] = (byte)(reference & 0xFF);
        results[2] = (byte)(andMask >> 8);
        results[3] = (byte)(andMask & 0xFF);
        results[4] = (byte)(orMask >> 8);
        results[5] = (byte)(orMask & 0xFF);

        return results;
    }
}
