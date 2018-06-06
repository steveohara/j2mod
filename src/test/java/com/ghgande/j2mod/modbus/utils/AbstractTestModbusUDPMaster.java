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
import com.ghgande.j2mod.modbus.facade.ModbusUDPMaster;
import com.ghgande.j2mod.modbus.io.ModbusUDPTransaction;
import com.ghgande.j2mod.modbus.msg.*;
import com.ghgande.j2mod.modbus.net.UDPMasterConnection;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.slave.ModbusSlave;
import com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;

import static org.junit.Assert.fail;

/**
 * All the master unit tests extend this class so that the system will automatically
 * create a test slave to work with and tear it down after a run
 *
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class AbstractTestModbusUDPMaster extends AbstractTestModbus {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTestModbusUDPMaster.class);
    protected static ModbusUDPMaster master;

    @BeforeClass
    public static void setUpSlave() {
        try {
            slave = createUDPSlave();
            master = new ModbusUDPMaster(TestUtils.getFirstIp4Address(), PORT);
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
    public static ModbusSlave createUDPSlave() throws Exception {
        ModbusSlave slave;
        try {
            // Create a UDP slave on the 'all interfaces' address 0.0.0.0
            slave = ModbusSlaveFactory.createUDPSlave(PORT);
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
        ModbusUDPTransaction trans;
        UDPMasterConnection connection = null;
        try {
            // Prepare the connection

            connection = new UDPMasterConnection(InetAddress.getByName(TestUtils.getFirstIp4Address()));
            connection.setPort(PORT);
            connection.connect();
            connection.setTimeout(500);
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
            trans = new ModbusUDPTransaction(connection);
            trans.setRequest(req);

            // Execute the transaction
            trans.execute();
            return trans.getResponse();
        }
        catch (Exception e) {
            logger.debug(e.getMessage());
        }
        finally {
            if (connection != null) {
                connection.close();
            }
        }
        return null;
    }

    /**
     * Executes a write transaction using the function code, register and value
     *
     * @param functionCode Function code to use
     * @param register     Register number
     * @param value        Value to apply
     *
     * @return Response object
     */
    protected static ModbusResponse writeRequest(int functionCode, int register, int value) {
        ModbusUDPTransaction trans;
        UDPMasterConnection connection = null;
        try {
            // Prepare the connection
            connection = new UDPMasterConnection(InetAddress.getByName(TestUtils.getFirstIp4Address()));
            connection.setPort(Modbus.DEFAULT_PORT);
            connection.setPort(PORT);
            connection.connect();
            connection.setTimeout(500);
            ModbusRequest req = null;

            // Prepare the request
            switch (functionCode) {
                case Modbus.WRITE_COIL:
                    req = new WriteCoilRequest(register, value != 0);
                    break;
                case Modbus.WRITE_SINGLE_REGISTER:
                    req = new WriteSingleRegisterRequest(register, new SimpleRegister(value));
                    break;
                default:
                    fail(String.format("Request type %d is not supported by the test harness", functionCode));
            }
            req.setUnitID(UNIT_ID);

            // Prepare the transaction
            trans = new ModbusUDPTransaction(connection);
            trans.setRequest(req);

            // Execute the transaction
            trans.execute();
            return trans.getResponse();
        }
        catch (Exception e) {
            logger.debug(e.getMessage());
        }
        finally {
            if (connection != null) {
                connection.close();
            }
        }
        return null;
    }

}
