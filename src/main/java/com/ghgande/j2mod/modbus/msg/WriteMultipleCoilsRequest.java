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
import com.ghgande.j2mod.modbus.util.BitVector;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Class implementing a <tt>WriteMultipleCoilsRequest</tt>. The implementation
 * directly correlates with the class 1 function <i>write multiple coils (FC
 * 15)</i>. It encapsulates the corresponding request message.
 *
 * <p>
 * Coils are understood as bits that can be manipulated (i.e. set or cleared).
 * </p>
 *
 * @author Dieter Wimberger
 * @author Julie Haugh
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public final class WriteMultipleCoilsRequest extends ModbusRequest {

    // instance attributes
    private int reference;
    private BitVector coils;

    /**
     * Constructs a new <tt>WriteMultipleCoilsRequest</tt> instance with the
     * given reference and coil values.
     *
     * @param ref the index of the first coil to be written.
     * @param bv  the coil values to be written.
     */
    public WriteMultipleCoilsRequest(int ref, BitVector bv) {
        super();

        setFunctionCode(Modbus.WRITE_MULTIPLE_COILS);
        setDataLength(bv.byteSize() + 5);

        setReference(ref);
        coils = bv;
    }

    /**
     * Constructs a new <tt>WriteMultipleCoilsRequest</tt> instance with a given
     * reference and count of coils to be written, followed by the actual byte
     * count, and then <i>count</i> number of bytes.
     *
     * @param ref   the index of the first coil to be written.
     * @param count the number of coils to be written.
     */
    public WriteMultipleCoilsRequest(int ref, int count) {
        super();

        setFunctionCode(Modbus.WRITE_MULTIPLE_COILS);
        setDataLength((count + 7) / 8 + 5);

        setReference(ref);
        coils = new BitVector(count);
    }

    /**
     * Constructs a new <tt>WriteMultipleCoilsRequest</tt> instance.
     *
     * <p>
     * A minimal message contains the reference to the first coil as a
     * <tt>short</tt>, the number of coils as a <tt>short</tt>, and not less
     * than one <tt>byte</tt> of coil data.
     */
    public WriteMultipleCoilsRequest() {
        super();

        setFunctionCode(Modbus.WRITE_MULTIPLE_COILS);
        setDataLength(5);

        coils = new BitVector(1);
    }

    public ModbusResponse getResponse() {
        WriteMultipleCoilsResponse response = new WriteMultipleCoilsResponse();

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
        WriteMultipleCoilsResponse response;
        DigitalOut douts[];

        // 1. get process image
        ProcessImage procimg = listener.getProcessImage(getUnitID());
        // 2. get coil range
        try {
            douts = procimg.getDigitalOutRange(reference, coils.size());
            // 3. set coils
            for (int i = 0; i < douts.length; i++) {
                douts[i].set(coils.getBit(i));
            }
        }
        catch (IllegalAddressException iaex) {
            return createExceptionResponse(Modbus.ILLEGAL_ADDRESS_EXCEPTION);
        }
        response = (WriteMultipleCoilsResponse)getResponse();

        response.setBitCount(coils.size());
        response.setReference(reference);

        return response;
    }

    /**
     * getReference - Returns the reference of the coil to to start writing to
     * with this <tt>WriteMultipleCoilsRequest</tt>.
     *
     * @return the reference of the coil to start writing to as an <tt>int</tt>.
     */
    public int getReference() {
        return reference;
    }

    /**
     * setReference - Sets the reference of the coil to start writing to with
     * this <tt>WriteMultipleCoilsRequest</tt>.
     *
     * @param ref the reference of the coil to start writing to.
     */
    public void setReference(int ref) {
        reference = ref;
    }

    /**
     * getBitCount - Returns the number of coils written with the request.
     *
     * @return the number of coils that have been written.
     */
    public int getBitCount() {
        if (coils == null) {
            return 0;
        }
        else {
            return coils.size();
        }
    }

    /**
     * getByteCount - Returns the number of bytes required for packing the
     * coils.
     *
     * @return the number of bytes required for packing the coils.
     */
    public int getByteCount() {
        return coils.byteSize();
    }

    /**
     * getCoilStatus - Returns the status of the specified coil.
     *
     * @param index the index of the coil to be tested.
     *
     * @return true if set, false otherwise.
     *
     * @throws IndexOutOfBoundsException if the given index is out of bounds.
     */
    public boolean getCoilStatus(int index) throws IndexOutOfBoundsException {
        return coils.getBit(index);
    }

    /**
     * setCoilStatus - Sets the status of the specified coil.
     *
     * @param index the index of the coil to be set/reset.
     * @param b     true if to be set, false for reset.
     *
     * @throws IndexOutOfBoundsException if the given index is out of bounds.
     */
    public void setCoilStatus(int index, boolean b) throws IndexOutOfBoundsException {
        coils.setBit(index, b);
    }

    /**
     * getCoils - Returns the <tt>BitVector</tt> instance holding coil status
     * information.
     *
     * @return the coils status as a <tt>BitVector</tt> instance.
     */
    public BitVector getCoils() {
        return coils;
    }

    /**
     * setCoils - Sets the <tt>BitVector</tt> instance holding coil status
     * information.
     *
     * @param bv a <tt>BitVector</tt> instance holding coil status info.
     */
    public void setCoils(BitVector bv) {
        coils = bv;
    }

    public void writeData(DataOutput dout) throws IOException {
        dout.writeShort(reference);
        dout.writeShort(coils.size());

        dout.writeByte(coils.byteSize());
        dout.write(coils.getBytes());
    }

    public void readData(DataInput din) throws IOException {
        reference = din.readUnsignedShort();
        int bitcount = din.readUnsignedShort();
        int coilBytes = din.readUnsignedByte();
        byte[] data = new byte[coilBytes];

        for (int k = 0; k < coilBytes; k++) {
            data[k] = din.readByte();
        }

        // decode bytes into BitVector, sets data and bitcount
        coils = BitVector.createBitVector(data, bitcount);

        // update data length
        setDataLength(coilBytes + 5);
    }

    public byte[] getMessage() {
        int len = coils.byteSize() + 5;
        byte result[] = new byte[len];

        result[0] = (byte)((reference >> 8) & 0xff);
        result[1] = (byte)(reference & 0xff);

        result[2] = (byte)((coils.size() >> 8) & 0xff);
        result[3] = (byte)(coils.size() & 0xff);

        result[4] = (byte)coils.byteSize();

        System.arraycopy(coils.getBytes(), 0, result, 5, coils.byteSize());

        return result;
    }
}
