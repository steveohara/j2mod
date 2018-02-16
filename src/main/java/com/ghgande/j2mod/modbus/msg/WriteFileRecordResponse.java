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
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Class implementing a <tt>WriteFileRecordResponse</tt>.
 *
 * @author Julie
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public final class WriteFileRecordResponse extends ModbusResponse {
    private RecordResponse[] records;

    /**
     * Constructs a new <tt>WriteFileRecordResponse</tt> instance.
     */
    public WriteFileRecordResponse() {
        super();

        setFunctionCode(Modbus.WRITE_FILE_RECORD);
        setDataLength(7);
    }

    /**
     * getRequestSize -- return the total request size.  This is useful
     * for determining if a new record can be added.
     *
     * @return size in bytes of response.
     */
    public int getResponseSize() {
        if (records == null) {
            return 1;
        }

        int size = 1;
        for (RecordResponse record : records) {
            size += record.getResponseSize();
        }

        return size;
    }

    /**
     * getRequestCount -- return the number of record requests in this
     * message.
     * @return the number of record requests in this message
     */
    public int getRequestCount() {
        if (records == null) {
            return 0;
        }

        return records.length;
    }

    /**
     * getRecord -- return the record request indicated by the reference
     * @param index
     * @return the record request indicated by the reference
     */
    public RecordResponse getRecord(int index) {
        return records[index];
    }

    /**
     * addResponse -- add a new record response.
     * @param response
     */
    public void addResponse(RecordResponse response) {
        if (response.getResponseSize() + getResponseSize() > 248) {
            throw new IllegalArgumentException();
        }

        if (records == null) {
            records = new RecordResponse[1];
        }
        else {
            RecordResponse old[] = records;
            records = new RecordResponse[old.length + 1];

            System.arraycopy(old, 0, records, 0, old.length);
        }
        records[records.length - 1] = response;

        setDataLength(getResponseSize());
    }

    public void writeData(DataOutput dout) throws IOException {
        dout.write(getMessage());
    }

    public void readData(DataInput din) throws IOException {
        int byteCount = din.readUnsignedByte();

        records = new RecordResponse[0];

        for (int offset = 1; offset + 7 < byteCount; ) {
            int function = din.readUnsignedByte();
            int file = din.readUnsignedShort();
            int record = din.readUnsignedShort();
            int count = din.readUnsignedShort();

            offset += 7;

            if (function != 6) {
                throw new IOException();
            }

            if (record < 0 || record >= 10000) {
                throw new IOException();
            }

            if (count < 0 || count >= 126) {
                throw new IOException();
            }

            short registers[] = new short[count];
            for (int j = 0; j < count; j++) {
                registers[j] = din.readShort();
                offset += 2;
            }
            RecordResponse dummy[] = new RecordResponse[records.length + 1];
            if (records.length > 0) {
                System.arraycopy(records, 0, dummy, 0, records.length);
            }

            records = dummy;
            records[records.length - 1] = new RecordResponse(file, record, registers);
        }
    }

    public byte[] getMessage() {
        byte results[] = new byte[getResponseSize()];

        results[0] = (byte)(getResponseSize() - 1);

        int offset = 1;
        for (RecordResponse record : records) {
            record.getResponse(results, offset);
            offset += record.getResponseSize();
        }
        return results;
    }

    public static class RecordResponse {
        private int fileNumber;
        private int recordNumber;
        private int wordCount;
        private byte data[];

        public RecordResponse(int file, int record, short[] values) {
            fileNumber = file;
            recordNumber = record;
            wordCount = values.length;
            data = new byte[wordCount * 2];

            int offset = 0;
            for (int i = 0; i < wordCount; i++) {
                data[offset++] = (byte)(values[i] >> 8);
                data[offset++] = (byte)(values[i] & 0xFF);
            }
        }

        public int getFileNumber() {
            return fileNumber;
        }

        public int getRecordNumber() {
            return recordNumber;
        }

        public int getWordCount() {
            return wordCount;
        }

        public SimpleRegister getRegister(int register) {
            if (register < 0 || register >= wordCount) {
                throw new IndexOutOfBoundsException("0 <= " + register + " < " + wordCount);
            }
            byte b1 = data[register * 2];
            byte b2 = data[register * 2 + 1];

            return new SimpleRegister(b1, b2);
        }

        /**
         * getResponseSize -- return the size of the response in bytes.
         * @return the size of the response in bytes
         */
        public int getResponseSize() {
            return 7 + wordCount * 2;
        }

        public void getResponse(byte[] response, int offset) {
            response[offset++] = 6;
            response[offset++] = (byte)(fileNumber >> 8);
            response[offset++] = (byte)(fileNumber & 0xFF);
            response[offset++] = (byte)(recordNumber >> 8);
            response[offset++] = (byte)(recordNumber & 0xFF);
            response[offset++] = (byte)(wordCount >> 8);
            response[offset++] = (byte)(wordCount & 0xFF);

            System.arraycopy(data, 0, response, offset, data.length);
        }

        public byte[] getResponse() {
            byte[] response = new byte[7 + 2 * wordCount];

            getResponse(response, 0);

            return response;
        }
    }
}