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
 * Class implementing a <tt>ReadCommEventCounterResponse</tt>.
 *
 * @author Julie Haugh (jfh@ghgande.com)
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public final class ReadCommEventCounterResponse extends ModbusResponse {

    // Message fields.
    private int status;
    private int events;

    /**
     * Constructs a new <tt>ReportSlaveIDResponse</tt> instance.
     */
    public ReadCommEventCounterResponse() {
        super();

        setFunctionCode(Modbus.READ_COMM_EVENT_COUNTER);
        setDataLength(4);
    }

    /**
     * getStatus -- get the device's status.
     *
     * @return int
     */
    public int getStatus() {
        return status;
    }

    /**
     * setStatus -- set the device's status.
     *
     * @param status int
     */
    public void setStatus(int status) {
        if (status != 0 && status != 0xFFFF) {
            throw new IllegalArgumentException("Illegal status value: " + status);
        }

        this.status = status;
    }

    /**
     * getEvents -- get device's event counter.
     * @return Event count
     */
    public int getEventCount() {
        return events;
    }

    /**
     * setEvents -- set the device's event counter.
     * @param count
     */
    public void setEventCount(int count) {
        events = count;
    }

    /**
     * writeData -- output the completed Modbus message to dout
     * @throws java.io.IOException
     */
    public void writeData(DataOutput dout) throws IOException {
        dout.write(getMessage());
    }

    /**
     * readData -- input the Modbus message from din. If there was a header,
     * such as for Modbus/TCP, it will have been read already.
     * @throws java.io.IOException
     */
    public void readData(DataInput din) throws IOException {
        status = din.readUnsignedShort();
        events = din.readUnsignedShort();
    }

    /**
     * getMessage -- format the message into a byte array.
     * @return Response as byte array
     */
    public byte[] getMessage() {
        byte result[] = new byte[4];

        result[0] = (byte)(status >> 8);
        result[1] = (byte)(status & 0xFF);
        result[2] = (byte)(events >> 8);
        result[3] = (byte)(events & 0xFF);

        return result;
    }
}
