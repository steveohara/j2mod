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
package com.ghgande.j2mod.modbus.utils;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransport;
import com.ghgande.j2mod.modbus.msg.*;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.slave.ModbusSlave;
import com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory;
import com.ghgande.j2mod.modbus.util.BitVector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

import static org.junit.Assert.fail;

/**
 * All the master unit tests extend this class so that the system will automatically
 * create a test slave to work with and tear it down after a run
 *
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class AbstractTestModbusTCPMaster extends AbstractTestModbus {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTestModbusTCPMaster.class);
    protected static ModbusTCPMaster master;

    @BeforeClass
    public static void setUpSlave() {
        try {
            slave = createTCPSlave();
            master = new ModbusTCPMaster(LOCALHOST, PORT);
            master.connect();
        }
        catch (Exception e) {
            tearDownSlave();
            fail(String.format("Cannot initialise tests - %s", e.getMessage()));
        }
    }

    @AfterClass
    public static void tearDownSlave() {
        if (master != null) {
            master.disconnect();
        }
        if (slave != null) {
            slave.close();
        }
    }

    /**
     * Creates a Slave to use for testing
     *
     * @return Listener of the slave
     *
     * @throws IOException If slave cannot be created
     */
    public static ModbusSlave createTCPSlave() throws Exception {
        ModbusSlave slave;
        try {
            // Create a TCP slave on the 'all interfaces' address 0.0.0.0
            slave = ModbusSlaveFactory.createTCPSlave(PORT, 5);
            slave.addProcessImage(UNIT_ID, getSimpleProcessImage());
            slave.open();
        }
        catch (Exception x) {
            throw new Exception(x.getMessage());
        }
        return slave;
    }

    /**
     * Executes a read transaction using the function code, register and count
     *
     * @param functionCode Function code to use
     * @param register     Register number
     * @param count        Number of registers
     *
     * @return Response object
     */
    protected static ModbusResponse readRequest(int functionCode, int register, int count) {
        ModbusTCPTransport transport = null;
        ModbusTCPTransaction trans;
        try {
            // Create a socket to use
            Socket socket = new Socket(LOCALHOST, PORT);
            transport = new ModbusTCPTransport(socket);
            ModbusRequest req = null;

            // Prepare the request
            switch (functionCode) {
                case Modbus.READ_COILS:
                    req = new ReadCoilsRequest(register, count);
                    break;
                case Modbus.READ_INPUT_DISCRETES:
                    req = new ReadInputDiscretesRequest(register, count);
                    break;
                case Modbus.READ_INPUT_REGISTERS:
                    req = new ReadInputRegistersRequest(register, count);
                    break;
                case Modbus.READ_HOLDING_REGISTERS:
                    req = new ReadMultipleRegistersRequest(register, count);
                    break;
                default:
                    fail(String.format("Request type %d is not supported by the test harness", functionCode));
            }
            req.setUnitID(UNIT_ID);

            // Prepare the transaction
            trans = (ModbusTCPTransaction)transport.createTransaction();
            trans.setRequest(req);
            trans.setReconnecting(true);

            // Execute the transaction
            trans.execute();
            return trans.getResponse();
        }
        catch (Exception e) {
            logger.info("Got error for request to {}:{} (FC:{}, Reg:{}, Cnt:{}) - {}", LOCALHOST, PORT, functionCode, register, count, e.getMessage());
        }
        finally {
            if (transport != null) {
                try {
                    transport.close();
                }
                catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }
        }
        return null;
    }

    /**
     * Executes a write transaction using the function code, register and value
     *
     * @param functionCode Function code to use
     * @param register     Register number
     * @param values       Values to apply
     *
     * @return Response object
     */
    protected static ModbusResponse writeRequest(int functionCode, int register, int... values) {
        ModbusTCPTransport transport = null;
        ModbusTCPTransaction trans;
        try {
            // Create a socket to use
            Socket socket = new Socket(LOCALHOST, PORT);
            transport = new ModbusTCPTransport(socket);
            ModbusRequest req = null;

            // Prepare the request
            switch (functionCode) {
                case Modbus.WRITE_COIL:
                    req = new WriteCoilRequest(register, values[0] != 0);
                    break;
                case Modbus.WRITE_SINGLE_REGISTER:
                    req = new WriteSingleRegisterRequest(register, new SimpleRegister(values[0]));
                    break;
                case Modbus.WRITE_MULTIPLE_REGISTERS:
                    Register[] regs = new Register[values.length];
                    for (int i = 0; i < values.length; i++) {
                        regs[i] = new SimpleRegister(values[i]);
                    }
                    req = new WriteMultipleRegistersRequest(register, regs);
                    break;
                case Modbus.WRITE_MULTIPLE_COILS:
                    BitVector bitVector = new BitVector(values.length);
                    for (int i = 0; i < values.length; i++) {
                        bitVector.setBit(i, values[i] != 0);
                    }
                    req = new WriteMultipleCoilsRequest(register, bitVector);
                    break;
                default:
                    fail(String.format("Request type %d is not supported by the test harness", functionCode));
            }
            req.setUnitID(UNIT_ID);

            // Prepare the transaction
            trans = (ModbusTCPTransaction)transport.createTransaction();
            trans.setRequest(req);
            trans.setReconnecting(true);

            // Execute the transaction
            trans.execute();
            return trans.getResponse();
        }
        catch (Exception e) {
            logger.debug(e.getMessage());
        }
        finally {
            if (transport != null) {
                try {
                    transport.close();
                }
                catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }
        }
        return null;
    }

}
