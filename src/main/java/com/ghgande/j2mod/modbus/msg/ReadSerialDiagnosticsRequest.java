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
import java.io.IOException;

/**
 * Class implementing a <tt>ReadSerialDiagnosticsRequest</tt>.
 *
 * @author Julie Haugh (jfh@ghgande.com)
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public final class ReadSerialDiagnosticsRequest extends ModbusRequest {

    // Message fields.
    private int function;
    private short data;

    /**
     * Constructs a new <tt>Diagnostics</tt> request
     * instance.
     */
    public ReadSerialDiagnosticsRequest() {
        super();

        setFunctionCode(Modbus.READ_SERIAL_DIAGNOSTICS);
        setDataLength(4);
    }

    /**
     * getFunction -- Get the DIAGNOSTICS sub-function.
     *
     * @return int
     */
    public int getFunction() {
        return function;
    }

    /**
     * setFunction - Set the DIAGNOSTICS sub-function.
     *
     * @param function - DIAGNOSTICS command sub-function.
     */
    public void setFunction(int function) {
        this.function = function;
        data = 0;
    }

    /**
     * getWordCount -- get the number of words in data.
     * @return Number of words in the data
     */
    public int getWordCount() {
        return 1;
    }

    /**
     * getData
     * @return the first data item
     */
    public int getData() {
        return data;
    }

    /**
     * setData -- Set the optional data value
     * @param value
     */
    public void setData(int value) {
        data = (short)value;
    }

    /**
     * getData -- Get the data item at the index.
     *
     * @param index - Unused, must be 0.
     * @return Data at index 0
     *
     * @deprecated
     */
    public int getData(int index) {
        if (index != 0) {
            throw new IndexOutOfBoundsException();
        }
        return data;
    }

    /**
     * setData -- Set the data item at the index
     *
     * @param index - Unused, must be 0.
     * @param value - Optional data value for function.
     *
     * @deprecated
     */
    public void setData(int index, int value) {
        if (index != 0) {
            throw new IndexOutOfBoundsException();
        }
        data = (short)value;
    }

    /**
     * createResponse -- create an empty response for this request.
     * @return Empty response
     */
    public ModbusResponse getResponse() {
        ReadSerialDiagnosticsResponse response;

        response = new ReadSerialDiagnosticsResponse();

        // Copy any header data from the request.
        response.setHeadless(isHeadless());
        if (!isHeadless()) {
            response.setTransactionID(getTransactionID());
            response.setProtocolID(getProtocolID());
        }

        // Copy the unit ID and function code.
        response.setUnitID(getUnitID());
        response.setFunctionCode(getFunctionCode());

        // Copy the sub-function code.
        response.setFunction(getFunction());

        return response;
    }

    @Override
    public ModbusResponse createResponse(AbstractModbusListener listener) {
        return createExceptionResponse(Modbus.ILLEGAL_FUNCTION_EXCEPTION);
    }

    /**
     * writeData -- output the completed Modbus message to dout
     * @throws java.io.IOException
     */
    public void writeData(DataOutput dout) throws IOException {
        dout.write(getMessage());
    }

    /**
     * readData -- Read the function code and data value
     * @throws java.io.IOException
     */
    public void readData(DataInput din) throws IOException {
        function = din.readUnsignedShort();
        data = (short)(din.readShort() & 0xFFFF);
    }

    /**
     * getMessage -- Create the DIAGNOSTICS message paylaod.
     * @return Response as byte array
     */
    public byte[] getMessage() {
        byte result[] = new byte[4];

        result[0] = (byte)(function >> 8);
        result[1] = (byte)(function & 0xFF);
        result[2] = (byte)(data >> 8);
        result[3] = (byte)(data & 0xFF);

        return result;
    }
}
