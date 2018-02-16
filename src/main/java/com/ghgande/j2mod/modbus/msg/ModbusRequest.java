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
import com.ghgande.j2mod.modbus.net.AbstractModbusListener;

/**
 * Abstract class implementing a <tt>ModbusRequest</tt>. This class provides
 * specialised implementations with the functionality they have in common.
 *
 * @author Dieter Wimberger
 * @author jfhaugh (jfh@ghgande.com)
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public abstract class ModbusRequest extends ModbusMessageImpl {

    /**
     * Factory method creating the required specialized <tt>ModbusRequest</tt>
     * instance.
     *
     * @param functionCode the function code of the request as <tt>int</tt>.
     *
     * @return a ModbusRequest instance specific for the given function type.
     */
    public static ModbusRequest createModbusRequest(int functionCode) {
        ModbusRequest request;

        switch (functionCode) {
            case Modbus.READ_COILS:
                request = new ReadCoilsRequest();
                break;
            case Modbus.READ_INPUT_DISCRETES:
                request = new ReadInputDiscretesRequest();
                break;
            case Modbus.READ_MULTIPLE_REGISTERS:
                request = new ReadMultipleRegistersRequest();
                break;
            case Modbus.READ_INPUT_REGISTERS:
                request = new ReadInputRegistersRequest();
                break;
            case Modbus.WRITE_COIL:
                request = new WriteCoilRequest();
                break;
            case Modbus.WRITE_SINGLE_REGISTER:
                request = new WriteSingleRegisterRequest();
                break;
            case Modbus.WRITE_MULTIPLE_COILS:
                request = new WriteMultipleCoilsRequest();
                break;
            case Modbus.WRITE_MULTIPLE_REGISTERS:
                request = new WriteMultipleRegistersRequest();
                break;
            case Modbus.READ_EXCEPTION_STATUS:
                request = new ReadExceptionStatusRequest();
                break;
            case Modbus.READ_SERIAL_DIAGNOSTICS:
                request = new ReadSerialDiagnosticsRequest();
                break;
            case Modbus.READ_COMM_EVENT_COUNTER:
                request = new ReadCommEventCounterRequest();
                break;
            case Modbus.READ_COMM_EVENT_LOG:
                request = new ReadCommEventLogRequest();
                break;
            case Modbus.REPORT_SLAVE_ID:
                request = new ReportSlaveIDRequest();
                break;
            case Modbus.READ_FILE_RECORD:
                request = new ReadFileRecordRequest();
                break;
            case Modbus.WRITE_FILE_RECORD:
                request = new WriteFileRecordRequest();
                break;
            case Modbus.MASK_WRITE_REGISTER:
                request = new MaskWriteRegisterRequest();
                break;
            case Modbus.READ_WRITE_MULTIPLE:
                request = new ReadWriteMultipleRequest();
                break;
            case Modbus.READ_FIFO_QUEUE:
                request = new ReadFIFOQueueRequest();
                break;
            case Modbus.READ_MEI:
                request = new ReadMEIRequest();
                break;
            default:
                request = new IllegalFunctionRequest(functionCode);
                break;
        }
        return request;
    }

    /**
     * Returns the <tt>ModbusResponse</tt> that correlates with this
     * <tt>ModbusRequest</tt>.
     *
     * <p>
     * The response must include the unit number, function code, as well as any
     * transport-specific header information.
     *
     * <p>
     * This method is used to create an empty response which must be populated
     * by the caller. It is commonly used to un-marshal responses from Modbus
     * slaves.
     *
     * @return the corresponding <tt>ModbusResponse</tt>.
     */
    public abstract ModbusResponse getResponse();

    /**
     * Returns the <tt>ModbusResponse</tt> that represents the answer to this
     * <tt>ModbusRequest</tt>.
     *
     * <p>
     * The implementation should take care about assembling the reply to this
     * <tt>ModbusRequest</tt>.
     *
     * <p>
     * This method is used to create responses from the process image associated
     * with the listener. It is commonly used to implement Modbus slave
     * instances.
     *
     * @param listener Listener that received the request
     * @return the corresponding <tt>ModbusResponse</tt>.
     */
    public abstract ModbusResponse createResponse(AbstractModbusListener listener);

    /**
     * Factory method for creating exception responses with the given exception
     * code.
     *
     * @param code the code of the exception.
     *
     * @return a ModbusResponse instance representing the exception response.
     */
    public ModbusResponse createExceptionResponse(int code) {
        ExceptionResponse response = new ExceptionResponse(getFunctionCode(), code);
        if (!isHeadless()) {
            response.setTransactionID(getTransactionID());
            response.setProtocolID(getProtocolID());
        }
        else {
            response.setHeadless();
        }
        response.setUnitID(getUnitID());
        return response;
    }
}
