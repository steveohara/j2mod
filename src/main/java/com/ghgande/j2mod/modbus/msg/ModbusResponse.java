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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import static com.ghgande.j2mod.modbus.msg.ModbusResponse.AuxiliaryMessageTypes.NONE;

/**
 * Abstract class implementing a <tt>ModbusResponse</tt>. This class provides
 * specialised implementations with the functionality they have in common.
 *
 * @author Dieter Wimberger
 * @author Julie Haugh
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public abstract class ModbusResponse extends ModbusMessageImpl {

    private static final Logger logger = LoggerFactory.getLogger(ModbusResponse.class);

    public enum AuxiliaryMessageTypes {
        NONE, UNIT_ID_MISSMATCH
    }
    private AuxiliaryMessageTypes auxiliaryType = NONE;

    /**
     * Factory method creating the required specialized <tt>ModbusResponse</tt>
     * instance.
     *
     * @param functionCode the function code of the response as <tt>int</tt>.
     *
     * @return a ModbusResponse instance specific for the given function code.
     */
    public static ModbusResponse createModbusResponse(int functionCode) {
        ModbusResponse response;

        switch (functionCode) {
            case Modbus.READ_COILS:
                response = new ReadCoilsResponse();
                break;
            case Modbus.READ_INPUT_DISCRETES:
                response = new ReadInputDiscretesResponse();
                break;
            case Modbus.READ_MULTIPLE_REGISTERS:
                response = new ReadMultipleRegistersResponse();
                break;
            case Modbus.READ_INPUT_REGISTERS:
                response = new ReadInputRegistersResponse();
                break;
            case Modbus.WRITE_COIL:
                response = new WriteCoilResponse();
                break;
            case Modbus.WRITE_SINGLE_REGISTER:
                response = new WriteSingleRegisterResponse();
                break;
            case Modbus.WRITE_MULTIPLE_COILS:
                response = new WriteMultipleCoilsResponse();
                break;
            case Modbus.WRITE_MULTIPLE_REGISTERS:
                response = new WriteMultipleRegistersResponse();
                break;
            case Modbus.READ_EXCEPTION_STATUS:
                response = new ReadExceptionStatusResponse();
                break;
            case Modbus.READ_SERIAL_DIAGNOSTICS:
                response = new ReadSerialDiagnosticsResponse();
                break;
            case Modbus.READ_COMM_EVENT_COUNTER:
                response = new ReadCommEventCounterResponse();
                break;
            case Modbus.READ_COMM_EVENT_LOG:
                response = new ReadCommEventLogResponse();
                break;
            case Modbus.REPORT_SLAVE_ID:
                response = new ReportSlaveIDResponse();
                break;
            case Modbus.READ_FILE_RECORD:
                response = new ReadFileRecordResponse();
                break;
            case Modbus.WRITE_FILE_RECORD:
                response = new WriteFileRecordResponse();
                break;
            case Modbus.MASK_WRITE_REGISTER:
                response = new MaskWriteRegisterResponse();
                break;
            case Modbus.READ_WRITE_MULTIPLE:
                response = new ReadWriteMultipleResponse();
                break;
            case Modbus.READ_FIFO_QUEUE:
                response = new ReadFIFOQueueResponse();
                break;
            case Modbus.READ_MEI:
                response = new ReadMEIResponse();
                break;
            default:
                if ((functionCode & 0x80) != 0) {
                    response = new ExceptionResponse(functionCode);
                }
                else {
                    response = new ExceptionResponse();
                }
                break;
        }
        return response;
    }

    /**
     * Utility method to set the raw data of the message. Should not be used
     * except under rare circumstances.
     * <p>
     *
     * @param msg the <tt>byte[]</tt> resembling the raw modbus response
     *            message.
     */
    protected void setMessage(byte[] msg) {
        try {
            readData(new DataInputStream(new ByteArrayInputStream(msg)));
        }
        catch (IOException ex) {
            logger.error("Problem setting response message - {}", ex.getMessage());
        }
    }

    /**
     * Returns the auxiliary type of this response message
     * Useful for adding extra information to the message that can be used by downstream processing
     *
     * @return Auxiliary type
     */
    public AuxiliaryMessageTypes getAuxiliaryType() {
        return auxiliaryType;
    }

    /**
     * Sets the auxiliary type of this response
     *
     * @param auxiliaryType Type
     */
    public void setAuxiliaryType(AuxiliaryMessageTypes auxiliaryType) {
        this.auxiliaryType = auxiliaryType;
    }
}