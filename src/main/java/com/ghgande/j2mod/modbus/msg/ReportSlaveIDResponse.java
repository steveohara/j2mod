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
public final class ReportSlaveIDResponse extends ModbusResponse {

    // Message fields.
    int m_length;
    byte m_data[];
    int m_status;
    int m_slaveId;

    /**
     * Constructs a new <tt>ReportSlaveIDResponse</tt>
     * instance.
     */
    public ReportSlaveIDResponse() {
        super();
        setFunctionCode(Modbus.REPORT_SLAVE_ID);
    }

    /**
     * getSlaveID -- return the slave identifier field.
     * @return slave identifier field
     */
    public int getSlaveID() {
        return m_slaveId;
    }

    /**
     * setSlaveID -- initialize the slave identifier when constructing
     * a response message.
     * @param i
     */
    public void setSlaveID(int i) {
        m_slaveId = i;
    }

    /**
     * getStatus -- get the slave's "run" status.
     *
     * @return boolean
     */
    public boolean getStatus() {
        return m_status != 0;
    }

    /**
     * setStatus -- initialize the slave's "run" status when constructing
     * a response message.
     *
     * @param b
     */
    public void setStatus(boolean b) {
        m_status = b ? 0xff : 0x00;
    }

    /**
     * getData -- get the device-depending data for the slave.
     *
     * @return byte array
     */
    public byte[] getData() {
        byte[] result = new byte[m_length - 2];
        System.arraycopy(m_data, 0, result, 0, m_length - 2);

        return result;
    }

    /**
     * setData -- initialize the slave's device dependent data when
     * initializing a response.
     *
     * @param data byte array
     */
    public void setData(byte[] data) {
        // There are always two bytes of payload in the message -- the
        // slave ID and the run status indicator.
        if (data == null) {
            m_length = 2;
            m_data = new byte[0];

            return;
        }

        if (data.length > 249) {
            throw new IllegalArgumentException("data length limit exceeded");
        }

        m_length = data.length + 2;

        m_data = new byte[data.length];
        System.arraycopy(data, 0, m_data, 0, data.length);
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

        // Get the size of any device-specific data.
        m_length = din.readUnsignedByte();
        if (m_length < 2 || m_length > 255) {
            return;
        }

        // Get the run status and device identifier.
        m_slaveId = din.readUnsignedByte();
        m_status = din.readUnsignedByte();

        /*
         * The device-specific data is two bytes shorter than the
         * length read previously.  That length includes the run status
         * and slave ID.
         */
        m_data = new byte[m_length - 2];
        if (m_length > 2) {
            din.readFully(m_data, 0, m_length - 2);
        }
    }

    /**
     * getMessage -- format the message into a byte array.
     * @return Byte array of message
     */
    public byte[] getMessage() {
        byte result[] = new byte[3 + m_length];
        int offset = 0;

        result[offset++] = (byte)(m_length + 2);
        result[offset++] = (byte)m_slaveId;
        result[offset++] = (byte)m_status;
        if (m_length > 0) {
            System.arraycopy(m_data, 0, result, offset, m_length - 2);
        }

        return result;
    }
}
