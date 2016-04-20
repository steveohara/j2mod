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
import com.ghgande.j2mod.modbus.ModbusCoupler;
import com.ghgande.j2mod.modbus.procimg.IllegalAddressException;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.ProcessImage;
import com.ghgande.j2mod.modbus.procimg.Register;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Class implementing a <tt>Read FIFO Queue</tt> request.
 *
 * @author Julie Haugh (jfh@ghgande.com)
 * @author jfhaugh (jfh@ghgande.com)
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public final class ReadFIFOQueueRequest extends ModbusRequest {

    private int reference;

    /**
     * Constructs a new <tt>Read FIFO Queue</tt> request instance.
     */
    public ReadFIFOQueueRequest() {
        super();

        setFunctionCode(Modbus.READ_FIFO_QUEUE);
        setDataLength(2);
    }

    /**
     * getReference -- get the queue register number.
     *
     * @return int
     */
    public int getReference() {
        return reference;
    }

    /**
     * setReference -- set the queue register number.
     *
     * @param ref Register
     */
    public void setReference(int ref) {
        reference = ref;
    }

    /**
     * getResponse -- create an empty response for this request.
     */
    public ModbusResponse getResponse() {
        ReadFIFOQueueResponse response;

        response = new ReadFIFOQueueResponse();

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

    /**
     * Create a response using the named register as the queue length count.
     */
    public ModbusResponse createResponse() {
        ReadFIFOQueueResponse response;
        InputRegister[] registers;

        // Get the process image.
        ProcessImage procimg = ModbusCoupler.getReference().getProcessImage(getUnitID());

        try {
            // Get the FIFO queue location and read the count of available
            // registers.
            Register queue = procimg.getRegister(reference);
            int count = queue.getValue();
            if (count < 0 || count > 31) {
                return createExceptionResponse(Modbus.ILLEGAL_VALUE_EXCEPTION);
            }

            registers = procimg.getRegisterRange(reference + 1, count);
        }
        catch (IllegalAddressException e) {
            return createExceptionResponse(Modbus.ILLEGAL_ADDRESS_EXCEPTION);
        }
        response = (ReadFIFOQueueResponse)getResponse();
        response.setRegisters(registers);

        return response;
    }

    /**
     * writeData -- output this Modbus message to dout.
     */
    public void writeData(DataOutput dout) throws IOException {
        dout.write(getMessage());
    }

    /**
     * readData -- read the reference word.
     */
    public void readData(DataInput din) throws IOException {
        reference = din.readShort();
    }

    /**
     * getMessage -- return an empty array as there is no data for this request.
     */
    public byte[] getMessage() {
        byte results[] = new byte[2];

        results[0] = (byte)(reference >> 8);
        results[1] = (byte)(reference & 0xFF);

        return results;
    }
}
