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
public final class ReadCommEventLogResponse extends ModbusResponse {

    // Message fields.
    private int byteCount;
    private int status;
    private int eventCount;
    private int messageCount;
    private byte[] events;

    /**
     * Constructs a new <tt>ReadCommEventLogResponse</tt> instance.
     */
    public ReadCommEventLogResponse() {
        super();

        setFunctionCode(Modbus.READ_COMM_EVENT_LOG);
        setDataLength(7);
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
     * @param status Status to set
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * getEvents -- get device's event counter.
     * @return Number of events
     */
    public int getEventCount() {
        return eventCount;
    }

    /**
     * setEventCount -- set the device's event counter.
     * @param count Set the event count
     */
    public void setEventCount(int count) {
        eventCount = count;
    }

    /**
     * getMessageCount -- get device's message counter.
     */
    public int getMessageCount() {
        return messageCount;
    }

    /**
     * setMessageCount -- set device's message counter.
     * @param count Number of messages
     */
    public void setMessageCount(int count) {
        messageCount = count;
    }

    /**
     * getEvent -- get an event from the event log.
     * @param index Index of the event
     * @return Event ID
     */
    public int getEvent(int index) {
        if (events == null || index < 0 || index >= events.length) {
            throw new IndexOutOfBoundsException("index = " + index + ", limit = " + (events == null ? "null" : events.length));
        }

        return events[index] & 0xFF;
    }

    public byte[] getEvents() {
        if (events == null) {
            return null;
        }

        byte[] result = new byte[events.length];
        System.arraycopy(events, 0, result, 0, events.length);

        return result;
    }

    public void setEvents(byte[] events) {
        if (events.length > 64) {
            throw new IllegalArgumentException("events list too big (> 64 bytes)");
        }

        events = new byte[events.length];
        if (events.length > 0) {
            System.arraycopy(events, 0, events, 0, events.length);
        }
    }

    public void setEvents(int count) {
        if (count < 0 || count > 64) {
            throw new IllegalArgumentException("invalid event list size (0 <= count <= 64)");
        }

        events = new byte[count];
    }

    /**
     * setEvent -- store an event number in the event log
     * @param index
     * @param event
     */
    public void setEvent(int index, int event) {
        if (events == null || index < 0 || index >= events.length) {
            throw new IndexOutOfBoundsException("index = " + index + ", limit = " + (events == null ? "null" : events.length));
        }

        events[index] = (byte)event;
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
        byteCount = din.readByte();
        status = din.readUnsignedShort();
        eventCount = din.readUnsignedShort();
        messageCount = din.readUnsignedShort();

        events = new byte[byteCount - 6];

        if (events.length > 0) {
            din.readFully(events, 0, events.length);
        }
    }

    /**
     * getMessage -- format the message into a byte array.
     * @return Response as byte array
     */
    public byte[] getMessage() {
        byte result[] = new byte[events.length + 7];

        result[0] = (byte)(byteCount = events.length + 6);
        result[1] = (byte)(status >> 8);
        result[2] = (byte)(status & 0xFF);
        result[3] = (byte)(eventCount >> 8);
        result[4] = (byte)(eventCount & 0xFF);
        result[5] = (byte)(messageCount >> 8);
        result[6] = (byte)(messageCount & 0xFF);

        System.arraycopy(events, 0, result, 7, events.length);

        return result;
    }
}
