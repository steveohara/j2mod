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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Class implementing a <tt>ReadMEIResponse</tt>.
 *
 * Derived from similar class for Read Coils response.
 *
 * @author Julie Haugh (jfh@ghgande.com)
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public final class MaskWriteRegisterResponse
        extends ModbusResponse {

    // Message fields.
    private int reference;
    private int andMask;
    private int orMask;

    /**
     * Constructs a new <tt>ReportSlaveIDResponse</tt>
     * instance.
     */
    public MaskWriteRegisterResponse() {
        super();
        setFunctionCode(Modbus.MASK_WRITE_REGISTER);
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
     * @param mask
     */
    public void setOrMask(int mask) {
        orMask = mask;
    }

    /**
     * writeData -- output the completed Modbus message to dout
     * @throws java.io.IOException
     */
    public void writeData(DataOutput dout) throws IOException {
        dout.write(getMessage());
    }

    /**
     * readData -- input the Modbus message from din.  If there was a
     * header, such as for Modbus/TCP, it will have been read
     * already.
     * @throws java.io.IOException
     */
    public void readData(DataInput din) throws IOException {
        reference = din.readUnsignedShort();
        andMask = din.readUnsignedShort();
        orMask = din.readUnsignedShort();
    }

    /**
     * getMessage -- format the message into a byte array.
     * @return Byte array of the message
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
