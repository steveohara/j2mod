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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public final class ReadMEIResponse extends ModbusResponse {

    private static final Logger logger = LoggerFactory.getLogger(ReadMEIResponse.class);

    //instance attributes
    private int fieldLevel = 0;
    private int conformity = 1;
    private int fieldCount = 0;
    private String fields[] = new String[64];
    private int fieldIds[] = new int[64];
    private boolean moreFollows = false;
    private int nextFieldId;

    /**
     * Constructs a new <tt>ReadMEIResponse</tt>
     * instance.
     */
    public ReadMEIResponse() {
        super();
        setFunctionCode(Modbus.READ_MEI);
    }

    /**
     * Returns the number of fields
     * read with the request.
     * <p>
     *
     * @return the number of fields that have been read.
     */
    public int getFieldCount() {
        if (fields == null) {
            return 0;
        }
        else {
            return fields.length;
        }
    }

    /**
     * Returns the array of strings that were read
     * @return Array of the fields read
     */
    public synchronized String[] getFields() {
        String[] dest = new String[fields.length];
        System.arraycopy(fields, 0, dest, 0, dest.length);
        return dest;
    }

    /**
     * Convenience method that returns the field
     * at the requested index
     * <p>
     *
     * @param index the index of the field which
     *              should be returned.
     *
     * @return requested field
     *
     * @throws IndexOutOfBoundsException if the
     *                                   index is out of bounds
     */
    public String getField(int index) throws IndexOutOfBoundsException {
        return fields[index];
    }

    /**
     * Convenience method that returns the field
     * ID at the given index.
     * <p>
     *
     * @param index the index of the field for which
     *              the ID should be returned.
     *
     * @return field ID
     *
     * @throws IndexOutOfBoundsException if the
     *                                   index is out of bounds
     */
    public int getFieldId(int index) throws IndexOutOfBoundsException {
        return fieldIds[index];
    }

    public void setFieldLevel(int level) {
        fieldLevel = level;
    }

    public void addField(int id, String text) {
        fieldIds[fieldCount] = id;
        fields[fieldCount] = text;
        fieldCount++;
    }

    public void writeData(DataOutput dout) throws IOException {
        dout.write(getMessage());
    }

    public void readData(DataInput din) throws IOException {
        int byteCount;

        int subCode = din.readUnsignedByte();
        if (subCode != 0xE) {
            throw new IOException("Invalid sub code");
        }

        fieldLevel = din.readUnsignedByte();
        conformity = din.readUnsignedByte();
        moreFollows = din.readUnsignedByte() == 0xFF;
        nextFieldId = din.readUnsignedByte();

        fieldCount = din.readUnsignedByte();

        byteCount = 6;

        if (fieldCount > 0) {
            fields = new String[fieldCount];
            fieldIds = new int[fieldCount];

            for (int i = 0; i < fieldCount; i++) {
                fieldIds[i] = din.readUnsignedByte();
                int len = din.readUnsignedByte();
                byte data[] = new byte[len];
                din.readFully(data);
                fields[i] = new String(data, "UTF-8");

                byteCount += 2 + len;
            }
            setDataLength(byteCount);
        }
        else {
            setDataLength(byteCount);
        }
    }

    public byte[] getMessage() {
        int size = 6;

        for (int i = 0; i < fieldCount; i++) {
            // Add the field ID
            size++;

            // Add the string length byte and the
            // actual string length.
            size++;
            size += fields[i].length();
        }

        byte result[] = new byte[size];
        int offset = 0;

        result[offset++] = 0x0E;
        result[offset++] = (byte)fieldLevel;
        result[offset++] = (byte)conformity;
        result[offset++] = (byte)(moreFollows ? 0xFF : 0);
        result[offset++] = (byte)nextFieldId;
        result[offset++] = (byte)fieldCount;

        for (int i = 0; i < fieldCount; i++) {
            result[offset++] = (byte)fieldIds[i];
            result[offset++] = (byte)fields[i].length();
            try {
                System.arraycopy(fields[i].getBytes("US-ASCII"), 0, result, offset, fields[i].length());
            }
            catch (Exception e) {
                logger.debug("Problem converting bytes to string - {}", e.getMessage());
            }
            offset += fields[i].length();
        }
        return result;
    }

}