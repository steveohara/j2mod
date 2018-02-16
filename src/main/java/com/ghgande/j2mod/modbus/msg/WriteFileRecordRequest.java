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
import com.ghgande.j2mod.modbus.msg.WriteFileRecordResponse.RecordResponse;
import com.ghgande.j2mod.modbus.net.AbstractModbusListener;
import com.ghgande.j2mod.modbus.procimg.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Class implementing a <tt>Write File Record</tt> request.
 *
 * @author Julie Haugh (jfh@ghgande.com)
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public final class WriteFileRecordRequest extends ModbusRequest {
    private RecordRequest[] records;

    /**
     * Constructs a new <tt>Write File Record</tt> request
     * instance.
     */
    public WriteFileRecordRequest() {
        super();

        setFunctionCode(Modbus.WRITE_FILE_RECORD);

        // Set up space for the initial header.
        setDataLength(1);
    }

    /**
     * getRequestSize -- return the total request size.  This is useful
     * for determining if a new record can be added.
     *
     * @return size in bytes of response.
     */
    public int getRequestSize() {
        if (records == null) {
            return 1;
        }

        int size = 1;
        for (RecordRequest record : records) {
            size += record.getRequestSize();
        }

        return size;
    }

    /**
     * getRequestCount -- return the number of record requests in this
     * message.
     * @return number of record requests in this message
     */
    public int getRequestCount() {
        if (records == null) {
            return 0;
        }

        return records.length;
    }

    /**
     * getRecord -- return the record request indicated by the reference
     * @param reference Register reference
     * @return the record request indicated by the reference
     */
    public RecordRequest getRecord(int reference) {
        return records[reference];
    }

    /**
     * addRequest -- add a new record request.
     * @param request Request record
     */
    public void addRequest(RecordRequest request) {
        if (request.getRequestSize() + getRequestSize() > 248) {
            throw new IllegalArgumentException();
        }

        if (records == null) {
            records = new RecordRequest[1];
        }
        else {
            RecordRequest old[] = records;
            records = new RecordRequest[old.length + 1];

            System.arraycopy(old, 0, records, 0, old.length);
        }
        records[records.length - 1] = request;

        setDataLength(getRequestSize());
    }

    /**
     * createResponse -- create an empty response for this request.
     * @return Byte array of message
     */
    public ModbusResponse getResponse() {
        WriteFileRecordResponse response;

        response = new WriteFileRecordResponse();

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

    @Override
    public ModbusResponse createResponse(AbstractModbusListener listener) {
        WriteFileRecordResponse response;
        response = (WriteFileRecordResponse)getResponse();

        // Get the process image.
        ProcessImage procimg = listener.getProcessImage(getUnitID());

        // There is a list of requests to be resolved.
        try {
            for (int i = 0; i < getRequestCount(); i++) {
                RecordRequest recordRequest = getRecord(i);
                if (recordRequest.getFileNumber() < 0 ||
                        recordRequest.getFileNumber() >= procimg.getFileCount()) {
                    return createExceptionResponse(Modbus.ILLEGAL_ADDRESS_EXCEPTION);
                }

                File file = procimg.getFileByNumber(recordRequest.getFileNumber());

                if (recordRequest.getRecordNumber() < 0 ||
                        recordRequest.getRecordNumber() >= file.getRecordCount()) {
                    return createExceptionResponse(Modbus.ILLEGAL_ADDRESS_EXCEPTION);
                }

                Record record = file.getRecord(recordRequest.getRecordNumber());
                int registers = recordRequest.getWordCount();
                if (record == null && registers != 0) {
                    return createExceptionResponse(Modbus.ILLEGAL_ADDRESS_EXCEPTION);
                }

                short data[] = new short[registers];
                for (int j = 0; j < registers; j++) {
                    Register register = record.getRegister(j);
                    if (register == null) {
                        return createExceptionResponse(Modbus.ILLEGAL_ADDRESS_EXCEPTION);
                    }

                    register.setValue(recordRequest.getRegister(j).getValue());
                    data[j] = recordRequest.getRegister(j).toShort();
                }
                RecordResponse recordResponse = new RecordResponse(file.getFileNumber(), record == null ? 0 : record.getRecordNumber(), data);
                response.addResponse(recordResponse);
            }
        }
        catch (IllegalAddressException e) {
            return createExceptionResponse(Modbus.ILLEGAL_ADDRESS_EXCEPTION);
        }
        return response;
    }

    /**
     * writeData -- output this Modbus message to dout.
     * @throws java.io.IOException
     */
    public void writeData(DataOutput dout) throws IOException {
        dout.write(getMessage());
    }

    /**
     * readData -- convert the byte stream into a request.
     * @throws java.io.IOException
     */
    public void readData(DataInput din) throws IOException {
        int byteCount = din.readUnsignedByte();

        records = new RecordRequest[0];

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
            RecordRequest dummy[] = new RecordRequest[records.length + 1];
            if (records.length > 0) {
                System.arraycopy(records, 0, dummy, 0, records.length);
            }

            records = dummy;
            records[records.length - 1] = new RecordRequest(file, record, registers);
        }
    }

    /**
     * getMessage -- return the raw binary message.
     * @return the raw binary message
     */
    public byte[] getMessage() {
        byte results[] = new byte[getRequestSize()];

        results[0] = (byte)(getRequestSize() - 1);

        int offset = 1;
        for (RecordRequest record : records) {
            record.getRequest(results, offset);
            offset += record.getRequestSize();
        }
        return results;
    }

    public static class RecordRequest {
        private int fileNumber;
        private int recordNumber;
        private int wordCount;
        private byte data[];

        public RecordRequest(int file, int record, short[] values) {
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
                throw new IllegalAddressException("0 <= " + register + " < " + wordCount);
            }
            byte b1 = data[register * 2];
            byte b2 = data[register * 2 + 1];

            return new SimpleRegister(b1, b2);
        }

        /**
         * getRequestSize -- return the size of the response in bytes.
         * @return the size of the response in bytes
         */
        public int getRequestSize() {
            return 7 + wordCount * 2;
        }

        public void getRequest(byte[] request, int offset) {
            request[offset++] = 6;
            request[offset++] = (byte)(fileNumber >> 8);
            request[offset++] = (byte)(fileNumber & 0xFF);
            request[offset++] = (byte)(recordNumber >> 8);
            request[offset++] = (byte)(recordNumber & 0xFF);
            request[offset++] = (byte)(wordCount >> 8);
            request[offset++] = (byte)(wordCount & 0xFF);

            System.arraycopy(data, 0, request, offset, data.length);
        }

        public byte[] getRequest() {
            byte[] request = new byte[7 + 2 * wordCount];

            getRequest(request, 0);

            return request;
        }
    }
}
