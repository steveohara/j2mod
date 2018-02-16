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
 * Class implementing a <tt>ReadFileRecordResponse</tt>.
 *
 * @author Julie (jfh@ghgande.com)
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public final class ReadFileRecordResponse extends ModbusResponse {

    private RecordResponse[] records = null;

    /**
     * Constructs a new <tt>ReadFileRecordResponse</tt> instance.
     */
    public ReadFileRecordResponse() {
        super();

        setFunctionCode(Modbus.READ_FILE_RECORD);
    }

    /**
     * Returns the number of bytes needed for the response.
     *
     * The response is 1 byte for the total response size, plus
     * the sum of the sizes of all the records in the response.
     *
     * @return the number of bytes in the response.
     */
    public int getByteCount() {
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
     * getRecordCount -- return the number of records in the response.
     *
     * @return count of records in response.
     */
    public int getRecordCount() {
        if (records == null) {
            return 0;
        }

        return records.length;
    }

    /**
     * getRecord
     * @param index
     * @return the record response indicated by the reference
     */
    public RecordResponse getRecord(int index) {
        return records[index];
    }

    /**
     * addResponse -- add a new record response.
     * @param response
     */
    public void addResponse(RecordResponse response) {
        if (records == null) {
            records = new RecordResponse[1];
        }
        else {
            RecordResponse old[] = records;
            records = new RecordResponse[old.length + 1];

            System.arraycopy(old, 0, records, 0, old.length);
        }
        records[records.length - 1] = response;
    }

    public void writeData(DataOutput dout) throws IOException {
        dout.writeByte(getByteCount() - 1);

        if (records == null) {
            return;
        }

        for (RecordResponse record : records) {
            dout.write(record.getResponse());
        }
    }

    public void readData(DataInput din) throws IOException {
        int byteCount = (din.readUnsignedByte() & 0xFF);

        int remainder = byteCount;
        while (remainder > 0) {
            int length = din.readUnsignedByte();
            remainder--;

            int function = din.readByte();
            remainder--;

            if (function != 6 || (length - 1) > remainder) {
                throw new IOException("Invalid response format");
            }
            short[] data = new short[(length - 1) / 2];
            for (int i = 0; i < data.length; i++) {
                data[i] = din.readShort();
                remainder -= 2;
            }
            RecordResponse response = new RecordResponse(data);
            addResponse(response);
        }
        setDataLength(byteCount + 1);
    }

    public byte[] getMessage() {
        byte result[];

        result = new byte[getByteCount()];

        int offset = 0;
        result[offset++] = (byte)(result.length - 1);

        for (RecordResponse record : records) {
            record.getResponse(result, offset);
            offset += record.getWordCount() * 2;
        }
        return result;
    }

    public static class RecordResponse {
        private int wordCount;
        private byte[] data;

        public RecordResponse(short data[]) {
            wordCount = data.length;
            this.data = new byte[wordCount * 2];

            int offset = 0;
            for (int i = 0; i < wordCount; i++) {
                this.data[offset++] = (byte)(data[i] >> 8);
                this.data[offset++] = (byte)(data[i] & 0xFF);
            }
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
         *
         * The response is a byte count, a function code, then wordCount
         * words (2 bytes).
         * @return the size of the response in bytes
         */
        public int getResponseSize() {
            return 2 + (wordCount * 2);
        }

        /**
         * getResponse - return the response data for this record
         *
         * The response data is the byte size of the response, minus this
         * byte, the function code (6), then the raw byte data for the
         * registers (wordCount * 2 bytes).
         *
         * @param request Request message
         * @param offset  Offset into buffer
         */
        public void getResponse(byte[] request, int offset) {
            request[offset] = (byte)(1 + (wordCount * 2));
            request[offset + 1] = 6;
            System.arraycopy(data, 0, request, offset + 2, data.length);
        }

        public byte[] getResponse() {
            byte[] request = new byte[getResponseSize()];
            getResponse(request, 0);
            return request;
        }
    }
}