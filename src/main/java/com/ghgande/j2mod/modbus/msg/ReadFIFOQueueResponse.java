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
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.SimpleInputRegister;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

/**
 * Class implementing a <tt>ReadFIFOQueueResponse</tt>.
 *
 * @author Julie Haugh (jfh@ghgande.com)
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public final class ReadFIFOQueueResponse extends ModbusResponse {

    // Message fields.
    private int count;
    private InputRegister registers[];

    /**
     * Constructs a new <tt>ReadFIFOQueueResponse</tt> instance.
     */
    public ReadFIFOQueueResponse() {
        super();

        setFunctionCode(Modbus.READ_FIFO_QUEUE);

        count = 0;
        registers = new InputRegister[0];

        setDataLength(7);
    }

    /**
     * getWordCount -- get the queue size.
     *
     * @return Word count int
     */
    synchronized public int getWordCount() {
        return count;
    }

    /**
     * setWordCount -- set the queue size.
     *
     * @param ref Register
     */
    public synchronized void setWordCount(int ref) {
        if (ref < 0 || ref > 31) {
            throw new IllegalArgumentException();
        }
        count = ref;
    }

    synchronized public int[] getRegisters() {
        int values[] = new int[count];

        for (int i = 0; i < count; i++) {
            values[i] = getRegister(i);
        }

        return values;
    }

    /**
     * setRegisters -- set the device's status.
     *
     * @param regs Array of registers
     */
    public synchronized void setRegisters(InputRegister[] regs) {
        if (regs == null) {
            registers = null;
            count = 0;
            return;
        }

        registers = Arrays.copyOf(regs, regs.length);
        if (regs.length > 31) {
            throw new IllegalArgumentException();
        }

        count = regs.length;
    }

    public int getRegister(int index) {
        return registers[index].getValue();
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

        /*
         * Read and discard the byte count.  There's no way to indicate
         * the packet was inconsistent, other than throwing an I/O
         * exception for an invalid packet format ...
         */
        din.readShort();

        // The first register is the number of registers which
        // follow.  Save that as count, not as a register.
        count = din.readUnsignedShort();
        registers = new InputRegister[count];

        for (int i = 0; i < count; i++) {
            registers[i] = new SimpleInputRegister(din.readShort());
        }
    }

    /**
     * getMessage -- format the message into a byte array.
     * @return Byte array of message
     */
    public byte[] getMessage() {
        byte result[] = new byte[count * 2 + 4];

        int len = count * 2 + 2;
        result[0] = (byte)(len >> 8);
        result[1] = (byte)(len & 0xFF);
        result[2] = (byte)(count >> 8);
        result[3] = (byte)(count & 0xFF);

        for (int i = 0; i < count; i++) {
            byte value[] = registers[i].toBytes();
            result[i * 2 + 4] = value[0];
            result[i * 2 + 5] = value[1];
        }
        return result;
    }
}
