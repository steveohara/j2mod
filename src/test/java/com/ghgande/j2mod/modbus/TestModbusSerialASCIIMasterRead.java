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

import com.ghgande.j2mod.modbus.io.AbstractSerialTransportListener;
import com.ghgande.j2mod.modbus.io.ModbusSerialTransport;
import com.ghgande.j2mod.modbus.msg.ModbusMessage;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.net.AbstractSerialConnection;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.SimpleInputRegister;
import com.ghgande.j2mod.modbus.utils.AbstractTestModbusSerialASCIIMaster;
import com.ghgande.j2mod.modbus.utils.AbstractTestModbusTCPMaster;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;

/**
 * This class tests the TCP master read features of the library
 */
@SuppressWarnings("ConstantConditions")
public class TestModbusSerialASCIIMasterRead extends AbstractTestModbusSerialASCIIMaster {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTestModbusTCPMaster.class);

    @Test
    public void testReadCoils() {
        try {
            assertTrue("Incorrect status for coil 0", master.readCoils(UNIT_ID, 0, 1).getBit(0));
            assertFalse("Incorrect status for coil 1", master.readCoils(UNIT_ID, 1, 1).getBit(0));
        }
        catch (Exception e) {
            fail(String.format("Cannot read - %s", e.getMessage()));
        }
    }

    @Test
    public void testReadInvalidCoil() {
        try {
            master.readCoils(UNIT_ID, 3, 1);
            fail("Invalid address not thrown");
        }
        catch (ModbusSlaveException e) {
            logger.info("Got expected error response (testReadInvalidCoil) - {}", e.getMessage());
        }
        catch (Exception e) {
            fail(String.format("Cannot read - %s", e.getMessage()));
        }
    }

    @Test
    public void testReadDiscretes() {
        try {
            assertFalse("Incorrect status for discrete 1", master.readInputDiscretes(UNIT_ID, 0, 1).getBit(0));
            assertTrue("Incorrect status for discrete 2", master.readInputDiscretes(UNIT_ID, 1, 1).getBit(0));
        }
        catch (Exception e) {
            fail(String.format("Cannot read - %s", e.getMessage()));
        }
    }

    @Test
    public void testReadInvalidDiscretes() {
        try {
            master.readInputDiscretes(UNIT_ID, 9, 1);
            fail("Failed check for missing discrete 9");
        }
        catch (ModbusSlaveException e) {
            logger.info("Got expected error response (testReadInvalidDiscretes) - {}", e.getMessage());
        }
        catch (Exception e) {
            fail(String.format("Cannot read - %s", e.getMessage()));
        }
    }

    @Test
    public void testReadInputRegisters() {
        try {
            assertEquals("Incorrect value for input register 1", 45, master.readInputRegisters(UNIT_ID, 0, 1)[0].getValue());
        }
        catch (Exception e) {
            fail(String.format("Cannot read - %s", e.getMessage()));
        }
    }

    @Test
    public void testReadInvalidInputRegisters() {
        try {
            master.readInputRegisters(UNIT_ID, 6, 1);
            fail("Failed check for missing register 6");
        }
        catch (ModbusSlaveException e) {
            logger.info("Got expected error response (testReadInvalidInputRegisters) - {}", e.getMessage());
        }
        catch (Exception e) {
            fail(String.format("Cannot read - %s", e.getMessage()));
        }
    }

    @Test
    public void testReadHoldingRegisters() {
        try {
            assertEquals("Incorrect value for holding register 1", 251, master.readMultipleRegisters(UNIT_ID, 0, 1)[0].getValue());
        }
        catch (Exception e) {
            fail(String.format("Cannot read - %s", e.getMessage()));
        }
    }

    @Test
    public void testReadInvalidHoldingRegisters() {
        try {
            master.readMultipleRegisters(UNIT_ID, 5, 1);
            fail("Failed check for missing holding register 5");
        }
        catch (ModbusSlaveException e) {
            logger.info("Got expected error response (testReadInvalidHoldingRegisters) - {}", e.getMessage());
        }
        catch (Exception e) {
            fail(String.format("Cannot read - %s", e.getMessage()));
        }
    }

    @Test
    public void testReadMultipleInputRegisters() {
        try {
            InputRegister[] regs = master.readInputRegisters(UNIT_ID, 0, 5);
            assertEquals("Failed to read multiple input register 1 length 5", 45, regs[0].getValue());
            assertEquals("Failed to read multiple input register 2 length 5", 9999, regs[1].getValue());
            assertEquals("Failed to read multiple input register 3 length 5", 8888, regs[2].getValue());
            assertEquals("Failed to read multiple input register 4 length 5", 7777, regs[3].getValue());
            assertEquals("Failed to read multiple input register 5 length 5", 6666, regs[4].getValue());
        }
        catch (Exception e) {
            fail(String.format("Cannot read - %s", e.getMessage()));
        }
    }

    @Test
    public void testReadMultipleHoldingRegisters() {
        try {
            InputRegister[] regs = master.readMultipleRegisters(UNIT_ID, 0, 5);
            assertEquals("Failed to read multiple holding register 1 length 5", 251, regs[0].getValue());
            assertEquals("Failed to read multiple holding register 2 length 5", 1111, regs[1].getValue());
            assertEquals("Failed to read multiple holding register 3 length 5", 2222, regs[2].getValue());
            assertEquals("Failed to read multiple holding register 4 length 5", 3333, regs[3].getValue());
            assertEquals("Failed to read multiple holding register 5 length 5", 4444, regs[4].getValue());
        }
        catch (Exception e) {
            fail(String.format("Cannot read - %s", e.getMessage()));
        }
    }

    @Test
    public void testBadUnitIdRequest() {
        try {
            master.readCoils(UNIT_ID + 10, 0, 1).getBit(0);
            fail("Failed check for invalid Unit ID");
        }
        catch (Exception e) {
            logger.info("Got expected error response (testBadUnitIdRequest) - {}", e.getMessage());
        }
    }


    @Test
    public void testCallback() {
        EventListener eventListener = new EventListener();
        ((ModbusSerialTransport) master.getTransport()).addListener(eventListener);
        try {
            eventListener.step = 0;
            int before = master.readInputRegisters(UNIT_ID, 1, 1)[0].getValue();
            eventListener.step = 0;
            master.writeSingleRegister(UNIT_ID, 1, new SimpleInputRegister(9999));
            eventListener.step = 0;
            master.writeSingleRegister(UNIT_ID, 1, new SimpleInputRegister(before));
        }
        catch (Exception e) {
            fail(String.format("Cannot read - %s", e.getMessage()));
        }
        ((ModbusSerialTransport) master.getTransport()).removeListener(eventListener);
    }

    private class EventListener extends AbstractSerialTransportListener {
        int step;

        EventListener() {
            step = 0;
        }

        @Override
        public void beforeMessageWrite(AbstractSerialConnection port, ModbusMessage msg) {
            assertEquals("Before message is written to port", 0, step);
            step++;
        }

        @Override
        public void afterMessageWrite(AbstractSerialConnection port, ModbusMessage msg) {
            assertEquals("After message has been written to port", 1, step);
            step++;
        }

        @Override
        public void beforeRequestRead(AbstractSerialConnection port) {
            fail("Should only be called for slaves");
        }

        @Override
        public void afterRequestRead(AbstractSerialConnection port, ModbusRequest req) {
            fail("Should only be called for slaves");
        }

        @Override
        public void beforeResponseRead(AbstractSerialConnection port) {
            assertEquals("Before response message is read from port", 2, step);
            step++;
        }

        @Override
        public void afterResponseRead(AbstractSerialConnection port, ModbusResponse res) {
            assertEquals("After response message has been read from port", 3, step);
            step++;
        }
        
        @Override
        public void disconnected(AbstractSerialConnection port) {
            assertEquals("Device is disconnected", 3, step);
            step++;
        }
    }

}
