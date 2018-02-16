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

import com.ghgande.j2mod.modbus.slave.ModbusSlave;
import com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

/**
 * All the slave unit tests extend this class so that the system will automatically
 * create a test slave to work with and tear it down after a run
 *
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public class AbstractTestModbusTCPSlaveNonCoupler extends AbstractTestModbusTCPSlave {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTestModbusTCPSlaveNonCoupler.class);

    @BeforeClass
    public static void setUpSlave() {
        assumeTrue("This platform does not support modpoll so the result of this test will be ignored", TestUtils.platformSupportsModPoll());
        try {
            port = PORT + 1;
            modPollTool = TestUtils.loadModPollTool();
            ModbusSlave slave = ModbusSlaveFactory.createTCPSlave(PORT, 5);
            slave.addProcessImage(UNIT_ID, getSimpleProcessImage());
            slave.open();

            slave = ModbusSlaveFactory.createTCPSlave(PORT + 1, 5);
            slave.addProcessImage(UNIT_ID, getSimpleProcessImage());
            slave.open();
        }
        catch (Exception e) {
            tearDownSlave();
            fail(String.format("Cannot initialise tests - %s", e.getMessage()));
        }
    }

    @AfterClass
    public static void tearDownSlave() {
        ModbusSlaveFactory.close();
    }
}
