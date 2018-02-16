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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;

/**
 * Class implementing a <tt>Read MEI Data</tt> request.
 *
 * @author jfhaugh (jfh@ghgande.com)
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public final class ReadMEIRequest extends ModbusRequest {

    // instance attributes
    private int subCode;
    private int fieldLevel;
    private int fieldId;

    /**
     * Constructs a new <tt>Read MEI Data request</tt> instance.
     */
    public ReadMEIRequest() {
        super();

        setFunctionCode(Modbus.READ_MEI);
        subCode = 0x0E;

        // 3 bytes (unit id and function code is excluded)
        setDataLength(3);
    }

    /**
     * Constructs a new <tt>Read MEI Data request</tt> instance with a given
     * reference and count of coils (i.e. bits) to be read.
     * <p>
     *
     * @param level the reference number of the register to read from.
     * @param id    the number of bits to be read.
     */
    public ReadMEIRequest(int level, int id) {
        super();

        setFunctionCode(Modbus.READ_MEI);
        subCode = 0x0E;

        // 3 bytes (unit id and function code is excluded)
        setDataLength(3);
        setLevel(level);
        setFieldId(id);
    }

    /**
     * Returns the response
     *
     * @return Response
     */
    public ModbusResponse getResponse() {
        ReadMEIResponse response;

        // Any other sub-function is an error.
        if (getSubCode() != 0x0E) {
            IllegalFunctionExceptionResponse error = new IllegalFunctionExceptionResponse();

            error.setUnitID(getUnitID());
            error.setFunctionCode(getFunctionCode());

            return error;
        }

        response = new ReadMEIResponse();

        // transfer header data
        if (!isHeadless()) {
            response.setTransactionID(getTransactionID());
            response.setProtocolID(getProtocolID());
        }
        else {
            response.setHeadless();
        }
        response.setUnitID(getUnitID());
        response.setFunctionCode(Modbus.READ_MEI);

        return response;
    }

    @Override
    public ModbusResponse createResponse(AbstractModbusListener listener) {
        return createExceptionResponse(Modbus.ILLEGAL_FUNCTION_EXCEPTION);
    }

    /**
     * Gets the MEI subcode associated with this request.
     * @return The MEI sub code
     */
    public int getSubCode() {
        return subCode;
    }

    /**
     * Returns the reference of the register to to start reading from with this
     * <tt>ReadCoilsRequest</tt>.
     * <p>
     *
     * @return the reference of the register to start reading from as
     * <tt>int</tt>.
     */
    public int getLevel() {
        return fieldLevel;
    }

    /**
     * Sets the reference of the register to start reading from with this
     * <tt>ReadCoilsRequest</tt>.
     * <p>
     *
     * @param level the reference of the register to start reading from.
     */
    public void setLevel(int level) {
        fieldLevel = level;
    }

    /**
     * Returns the number of bits (i.e. coils) to be read with this
     * <tt>ReadCoilsRequest</tt>.
     * <p>
     *
     * @return the number of bits to be read.
     */
    public int getFieldId() {
        return fieldId;
    }

    /**
     * Sets the number of bits (i.e. coils) to be read with this
     * <tt>ReadCoilsRequest</tt>.
     * <p>
     *
     * @param id the number of bits to be read.
     */
    public void setFieldId(int id) {
        fieldId = id;
    }

    public void writeData(DataOutput dout) throws IOException {
        byte results[] = new byte[3];

        results[0] = (byte)subCode;
        results[1] = (byte)fieldLevel;
        results[2] = (byte)fieldId;

        dout.write(results);
    }

    public void readData(DataInput din) throws IOException {
        subCode = din.readUnsignedByte();

        if (subCode != 0xE) {
            try {
                while (din.readByte() >= 0) {
                }
            }
            catch (EOFException x) {
                // do nothing.
            }
            return;
        }
        fieldLevel = din.readUnsignedByte();
        fieldId = din.readUnsignedByte();
    }

    public byte[] getMessage() {
        byte results[] = new byte[3];

        results[0] = (byte)subCode;
        results[1] = (byte)fieldLevel;
        results[2] = (byte)fieldId;

        return results;
    }
}