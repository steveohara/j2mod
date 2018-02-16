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
 * Class implementing a <tt>WriteCoilRequest</tt>. The implementation directly
 * correlates with the class 0 function <i>write coil (FC 5)</i>. It
 * encapsulates the corresponding request message.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public final class WriteCoilRequest extends ModbusRequest {

    // instance attributes
    private int reference;
    private boolean coil;

    /**
     * Constructs a new <tt>WriteCoilRequest</tt> instance.
     */
    public WriteCoilRequest() {
        super();

        setFunctionCode(Modbus.WRITE_COIL);
        setDataLength(4);
    }

    /**
     * Constructs a new <tt>WriteCoilRequest</tt> instance with a given
     * reference and state to be written.
     *
     * @param ref the reference number of the register to read from.
     * @param b   true if the coil should be set of false if it should be unset.
     */
    public WriteCoilRequest(int ref, boolean b) {
        super();

        setFunctionCode(Modbus.WRITE_COIL);
        setDataLength(4);

        setReference(ref);
        setCoil(b);
    }

    public ModbusResponse getResponse() {
        WriteCoilResponse response = new WriteCoilResponse();

        response.setHeadless(isHeadless());
        if (!isHeadless()) {
            response.setProtocolID(getProtocolID());
            response.setTransactionID(getTransactionID());
        }
        response.setFunctionCode(getFunctionCode());
        response.setUnitID(getUnitID());

        return response;
    }

    @Override
    public ModbusResponse createResponse(AbstractModbusListener listener) {
        WriteCoilResponse response;
        DigitalOut dout;

        // 1. get process image
        ProcessImage procimg = listener.getProcessImage(getUnitID());
        // 2. get coil
        try {
            dout = procimg.getDigitalOut(getReference());
            // 3. set coil
            dout.set(getCoil());
        }
        catch (IllegalAddressException iaex) {
            return createExceptionResponse(Modbus.ILLEGAL_ADDRESS_EXCEPTION);
        }
        response = (WriteCoilResponse)getResponse();
        response.setReference(getReference());
        response.setCoil(getCoil());

        return response;
    }

    /**
     * Returns the reference of the register of the coil that should be written
     * to with this <tt>ReadCoilsRequest</tt>.
     *
     * @return the reference of the coil's register.
     */
    public int getReference() {
        return reference;
    }

    /**
     * Sets the reference of the register of the coil that should be written to
     * with this <tt>ReadCoilsRequest</tt>.
     * <p>
     *
     * @param ref the reference of the coil's register.
     */
    public void setReference(int ref) {
        reference = ref;
    }

    /**
     * Returns the state that should be written with this
     * <tt>WriteCoilRequest</tt>.
     *
     * @return true if the coil should be set of false if it should be unset.
     */
    public boolean getCoil() {
        return coil;
    }

    /**
     * Sets the state that should be written with this <tt>WriteCoilRequest</tt>.
     *
     * @param b true if the coil should be set of false if it should be unset.
     */
    public void setCoil(boolean b) {
        coil = b;
    }

    public void writeData(DataOutput dout) throws IOException {
        dout.writeShort(reference);

        if (coil) {
            dout.write(Modbus.COIL_ON_BYTES, 0, 2);
        }
        else {
            dout.write(Modbus.COIL_OFF_BYTES, 0, 2);
        }
    }

    public void readData(DataInput din) throws IOException {
        reference = din.readUnsignedShort();

        if (din.readByte() == Modbus.COIL_ON) {
            coil = true;
        }
        else {
            coil = false;
        }

        // discard the next byte.
        din.readByte();
    }

    public byte[] getMessage() {
        byte result[] = new byte[4];

        result[0] = (byte)((reference >> 8) & 0xff);
        result[1] = (byte)(reference & 0xff);
        if (coil) {
            result[2] = Modbus.COIL_ON_BYTES[0];
            result[3] = Modbus.COIL_ON_BYTES[1];
        }
        else {
            result[2] = Modbus.COIL_OFF_BYTES[0];
            result[3] = Modbus.COIL_OFF_BYTES[1];
        }
        return result;
    }
}