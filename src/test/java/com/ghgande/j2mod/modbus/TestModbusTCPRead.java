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
import com.ghgande.j2mod.modbus.msg.ReadInputDiscretesResponse;
import com.ghgande.j2mod.modbus.msg.ReadInputRegistersResponse;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
import com.ghgande.j2mod.modbus.utils.AbstractTestModbusTCPMaster;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;

/**
 * This class tests the TCP master read features of the library
 */
@SuppressWarnings("ConstantConditions")
public class TestModbusTCPRead extends AbstractTestModbusTCPMaster {

    @Test
    public void testReadCoils() {
        ReadCoilsResponse res = (ReadCoilsResponse)readRequest(Modbus.READ_COILS, 0, 1);
        assertTrue("Incorrect status for coil 0", res.getCoilStatus(0));
        assertFalse("Incorrect status for coil 1", res.getCoilStatus(1));
    }

    @Test
    public void testReadInvalidCoil() {
        assertNull("Failed check for missing coil 3", readRequest(Modbus.READ_COILS, 3, 1));
    }

    @Test
    public void testReadDiscretes() {
        ReadInputDiscretesResponse res = (ReadInputDiscretesResponse)readRequest(Modbus.READ_INPUT_DISCRETES, 0, 2);
        assertFalse("Incorrect status for discrete 1", res.getDiscreteStatus(0));
        assertTrue("Incorrect status for discrete 2", res.getDiscreteStatus(1));
    }

    @Test
    public void testReadInvalidDiscretes() {
        assertNull("Failed check for missing discrete 3", readRequest(Modbus.READ_INPUT_DISCRETES, 9, 1));
    }

    @Test
    public void testReadInputRegisters() {
        ReadInputRegistersResponse res = (ReadInputRegistersResponse)readRequest(Modbus.READ_INPUT_REGISTERS, 0, 1);
        assertEquals("Incorrect value for input register 1", 45, res.getRegisterValue(0));
    }

    @Test
    public void testReadInvalidInputRegisters() {
        assertNull("Failed check for missing input register 6", readRequest(Modbus.READ_INPUT_REGISTERS, 6, 1));
    }

    @Test
    public void testReadHoldingRegisters() {
        ReadMultipleRegistersResponse res = (ReadMultipleRegistersResponse)readRequest(Modbus.READ_HOLDING_REGISTERS, 0, 1);
        assertEquals("Incorrect value for holding register 1", 251, res.getRegisterValue(0));
    }

    @Test
    public void testReadInvalidHoldingRegisters() {
        assertNull("Failed check for missing holding register 5", readRequest(Modbus.READ_HOLDING_REGISTERS, 5, 1));
    }

    @Test
    public void testReadMultipleInputRegisters() {
        ReadInputRegistersResponse res = (ReadInputRegistersResponse)readRequest(Modbus.READ_INPUT_REGISTERS, 0, 5);
        assertEquals("Failed to read multiple input register 1 length 5", 45, res.getRegisterValue(0));
        assertEquals("Failed to read multiple input register 2 length 5", 9999, res.getRegisterValue(1));
        assertEquals("Failed to read multiple input register 3 length 5", 8888, res.getRegisterValue(2));
        assertEquals("Failed to read multiple input register 4 length 5", 7777, res.getRegisterValue(3));
        assertEquals("Failed to read multiple input register 5 length 5", 6666, res.getRegisterValue(4));
    }

    @Test
    public void testReadMultipleHoldingRegisters() {
        ReadMultipleRegistersResponse res = (ReadMultipleRegistersResponse)readRequest(Modbus.READ_HOLDING_REGISTERS, 0, 5);
        assertEquals("Failed to read multiple holding register 1 length 5", 251, res.getRegisterValue(0));
        assertEquals("Failed to read multiple holding register 2 length 5", 1111, res.getRegisterValue(1));
        assertEquals("Failed to read multiple holding register 3 length 5", 2222, res.getRegisterValue(2));
        assertEquals("Failed to read multiple holding register 4 length 5", 3333, res.getRegisterValue(3));
        assertEquals("Failed to read multiple holding register 5 length 5", 4444, res.getRegisterValue(4));
    }

}
