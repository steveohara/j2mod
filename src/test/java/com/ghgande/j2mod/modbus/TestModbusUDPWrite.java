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
package com.ghgande.j2mod.modbus;

import com.ghgande.j2mod.modbus.msg.ReadCoilsResponse;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
import com.ghgande.j2mod.modbus.msg.WriteCoilResponse;
import com.ghgande.j2mod.modbus.msg.WriteSingleRegisterResponse;
import com.ghgande.j2mod.modbus.utils.AbstractTestModbusUDPMaster;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This class tests the TCP master write features of the library
 */
@SuppressWarnings("ConstantConditions")
public class TestModbusUDPWrite extends AbstractTestModbusUDPMaster {

    @Test
    public void testWriteCoils() {
        WriteCoilResponse res = (WriteCoilResponse)writeRequest(Modbus.WRITE_COIL, 1, 1);
        assertTrue("Incorrect write status for coil 2", res.getCoil());
        ReadCoilsResponse res1 = (ReadCoilsResponse)readRequest(Modbus.READ_COILS, 1, 1);
        assertTrue("Incorrect status for coil 2", res1.getCoilStatus(0));
    }

    @Test
    public void testWriteHoldingRegisters() {
        WriteSingleRegisterResponse res = (WriteSingleRegisterResponse)writeRequest(Modbus.WRITE_SINGLE_REGISTER, 0, 5555);
        assertEquals("Incorrect write status for register 1", 5555, res.getRegisterValue());
        ReadMultipleRegistersResponse res1 = (ReadMultipleRegistersResponse)readRequest(Modbus.READ_HOLDING_REGISTERS, 0, 1);
        assertEquals("Incorrect status for register 0", 5555, res1.getRegisterValue(0));
    }

}
