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
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleInputRegister;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.utils.AbstractTestModbusSerialRTUMaster;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This class tests the Serial master write features of the library
 */
@SuppressWarnings("ConstantConditions")
public class TestModbusSerialRTUMasterWrite extends AbstractTestModbusSerialRTUMaster {

    @Test
    public void testWriteCoils() {
        try {
            boolean before = master.readCoils(UNIT_ID, 1, 1).getBit(0);
            master.writeCoil(UNIT_ID, 1, !before);
            assertTrue("Incorrect status for coil 1", !before);
            master.writeCoil(UNIT_ID, 1, before);
        }
        catch (Exception e) {
            fail(String.format("Cannot write to coil 1 - %s", e.getMessage()));
        }
    }

    @Test
    public void testWriteHoldingRegisters() {
        try {
            int before = master.readInputRegisters(UNIT_ID, 1, 1)[0].getValue();
            int newValue = 9999;

            assertEquals("Incorrect status after write new value for register 1", newValue,
                    master.writeSingleRegister(UNIT_ID, 1, new SimpleInputRegister(newValue)));
            assertEquals("Incorrect status after read new value for register 1", newValue,
                    master.readInputRegisters(UNIT_ID, 1, 1)[0].getValue());
            assertEquals("Incorrect status after write previous value for register 1", before,
                    master.writeSingleRegister(UNIT_ID, 1, new SimpleInputRegister(before)));
        }
        catch (Exception e) {
            fail(String.format("Cannot write to register 1 - %s", e.getMessage()));
        }
    }

    @Test
    public void testWriteMultipleRegisters() {
        try {
            int registerCount = 3;
            Register[] beforeRegisters = master.readMultipleRegisters(UNIT_ID, 1, registerCount);

            Register[] writeRegisters = new Register[registerCount];
            for (int i = 0; i < registerCount; ++i) {
                writeRegisters[i] = new SimpleRegister(Double.valueOf(Math.random()).intValue());
            }

            assertEquals("Incorrect status for register",
                    registerCount, master.writeMultipleRegisters(UNIT_ID, 1, writeRegisters));

            Register[] afterRegisters = master.readMultipleRegisters(UNIT_ID, 1, registerCount);

            for (int i = 0; i < registerCount; ++i) {
                assertEquals("Incorrect status for register",
                        writeRegisters[i].getValue(), afterRegisters[i].getValue());
            }

            master.writeMultipleRegisters(UNIT_ID, 1, beforeRegisters);
        }
        catch (Exception e) {
            fail(String.format("Cannot write to registers - %s", e.getMessage()));
        }
    }

    @Test
    public void testMaskWriteRegister() {
        try {
            int before = master.readMultipleRegisters(UNIT_ID, 1, 1)[0].getValue();
            int andMask = 0xABCD;
            int orMask = 0xBCDA;
            int newValue = (before & andMask) | (orMask & ~andMask);

            assertTrue("Incorrect mask write status for register 1", master.maskWriteRegister(UNIT_ID, 1, andMask, orMask));
            assertEquals("Incorrect status for register 1", newValue, master.readMultipleRegisters(UNIT_ID, 1, 1)[0].getValue());
            master.writeSingleRegister(UNIT_ID, 1, new SimpleInputRegister(before));
        }
        catch (Exception e) {
            fail(String.format("Cannot mask write to register 1 - %s", e.getMessage()));
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
