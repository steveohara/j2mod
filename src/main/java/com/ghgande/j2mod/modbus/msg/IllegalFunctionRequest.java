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
 * <p>
 * Class implementing a <tt>ModbusRequest</tt> which is created for illegal or
 * non implemented function codes.
 *
 * <p>
 * This is just a helper class to keep the implementation patterns the same for
 * all cases.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public class IllegalFunctionRequest extends ModbusRequest {

    /**
     * Constructs a new <tt>IllegalFunctionRequest</tt> instance for a given
     * function code.
     *
     * <p>Used to implement slave devices when an illegal function code
     * has been requested.
     *
     * @param function the function code as <tt>int</tt>.
     */
    public IllegalFunctionRequest(int function) {
        setFunctionCode(function);
    }

    /**
     * Constructs a new <tt>IllegalFunctionRequest</tt> instance for a given
     * function code.
     *
     * <p>Used to implement slave devices when an illegal function code
     * has been requested.
     *
     * @param unit
     * @param function the function code as <tt>int</tt>.
     */
    public IllegalFunctionRequest(int unit, int function) {
        setUnitID(unit);
        setFunctionCode(function);
    }

    /**
     * There is no unit number associated with this exception.
     * @return Modbus excepion response
     */
    public ModbusResponse getResponse() {
        IllegalFunctionExceptionResponse response = new IllegalFunctionExceptionResponse(getFunctionCode());

        response.setUnitID(getUnitID());
        return response;
    }

    @Override
    public ModbusResponse createResponse(AbstractModbusListener listener) {
        return createExceptionResponse(Modbus.ILLEGAL_FUNCTION_EXCEPTION);
    }

    public void writeData(DataOutput dout) throws IOException {
        throw new RuntimeException();
    }

    /**
     * Read all of the data that can be read.  This is an unsupported
     * function, so it may not be possible to know exactly how much data
     * needs to be read.
     * @throws java.io.IOException
     */
    public void readData(DataInput din) throws IOException {
        // skip all following bytes
        int length = getDataLength();
        for (int i = 0; i < length; i++) {
            din.readByte();
        }
    }

    public byte[] getMessage() {
        return null;
    }
}
