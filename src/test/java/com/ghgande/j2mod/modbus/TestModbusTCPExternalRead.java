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

import com.ghgande.j2mod.modbus.utils.AbstractTestModbusTCPSlave;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * This class uses an external master tool to test that the j2mod slave read features
 * work against a known good standard
 * Once this is verified, the j2mod slave code can be used to test the features of the
 * master
 *
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public class TestModbusTCPExternalRead extends AbstractTestModbusTCPSlave {

    @Test
    public void testSlaveReadCoils() {
        assertTrue("Incorrect status for coil 1", readModPoll(1, 0, "[1]: 1"));
        assertTrue("Incorrect status for coil 2", readModPoll(2, 0, "[2]: 0"));
    }

    @Test
    public void testSlaveReadInvalidCoil() {
        assertTrue("Failed check for missing coil 3", readModPoll(3, 0, "Invalid MPAB indentifer"));
    }

    @Test
    public void testSlaveReadDiscretes() {
        assertTrue("Incorrect status for discrete 1", readModPoll(1, 1, "[1]: 0"));
        assertTrue("Incorrect status for discrete 2", readModPoll(2, 1, "[2]: 1"));
    }

    @Test
    public void testSlaveReadInvalidDiscretes() {
        assertTrue("Failed check for missing discrete 3", readModPoll(9, 1, "Illegal Data Address exception response"));
    }

    @Test
    public void testSlaveReadInputRegisters() {
        assertTrue("Incorrect value for input register 1", readModPoll(1, 3, "[1]: 45"));
    }

    @Test
    public void testSlaveReadInvalidInputRegisters() {
        assertTrue("Failed check for missing input register 6", readModPoll(6, 3, "Illegal Data Address exception response"));
    }

    @Test
    public void testSlaveReadHoldingRegisters() {
        assertTrue("Incorrect value for holding register 1", readModPoll(1, 4, "[1]: 251"));
    }

    @Test
    public void testSlaveReadInvalidHoldingRegisters() {
        assertTrue("Failed check for missing holding register 5", readModPoll(6, 4, "Illegal Data Address exception response"));
    }

    @Test
    public void testSlaveReadMultipleInputRegisters() {
        assertTrue("Failed to read multiple input register 1 length 5", readModPoll(1, 3, 5, "[1]: 45\n[2]: 9999\n[3]: 8888\n[4]: 7777\n[5]: 6666"));
    }

    @Test
    public void testSlaveReadMultipleHoldingRegisters() {
        assertTrue("Failed to read multiple holding register 1 length 5", readModPoll(1, 4, 5, "[1]: 251\n[2]: 1111\n[3]: 2222\n[4]: 3333\n[5]: 4444"));
    }

}
