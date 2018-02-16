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

import com.ghgande.j2mod.modbus.net.AbstractModbusListener;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

/**
 * All the slave unit tests extend this class so that the system will automatically
 * create a test slave to work with and tear it down after a run
 *
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public class AbstractTestModbusTCPSlave extends AbstractTestModbusTCPMaster {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTestModbusTCPSlave.class);
    private static AbstractModbusListener listener = null;
    protected static File modPollTool;
    protected static int port = PORT;

    @BeforeClass
    public static void setUpSlave() {
        assumeTrue("This platform does not support modpoll so the result of this test will be ignored", TestUtils.platformSupportsModPoll());
        try {
            port = PORT;
            modPollTool = TestUtils.loadModPollTool();
            listener = createTCPSlave();
        }
        catch (Exception e) {
            fail(String.format("Cannot initialise tests - %s", e.getMessage()));
        }
    }

    @AfterClass
    public static void tearDownSlave() {
        if (listener != null && listener.isListening()) {
            listener.stop();
        }
    }

    /**
     * Convenience method to run the modpoll executable with the given command parameters
     * to read from a register
     *
     * @param register       Register reference number
     * @param type           Type of register to query
     * @param expectedOutput The text that should be found in the output
     *
     * @return True if the expected output is available
     */
    public static boolean readModPoll(int register, int type, String expectedOutput) {
        return readModPoll(register, type, 1, expectedOutput);
    }

    /**
     * Convenience method to run the modpoll executable with the given command parameters
     * to read from a register
     *
     * @param register          Register reference number
     * @param type              Type of register to query
     * @param numberOfRegisters Number of registers to read
     * @param expectedOutput    The text that should be found in the output
     *
     * @return True if the expected output is available
     */
    public static boolean readModPoll(int register, int type, int numberOfRegisters, String expectedOutput) {
        return execModPoll(register, type, null, expectedOutput, numberOfRegisters);
    }

    /**
     * Convenience method to run the modpoll executable with the given command parameters
     * to write to a register
     *
     * @param register       Register reference number
     * @param type           Type of register to query
     * @param expectedOutput The text that should be found in the output
     *
     * @return True if the expected output is available
     */
    public static boolean writeModPoll(int register, int type, int value, String expectedOutput) {
        return execModPoll(register, type, value, expectedOutput, 1);
    }

    /**
     * Convenience method to run the modpoll executable with the given command parameters
     *
     * @param register          Register reference number
     * @param type              Type of register to query
     * @param outValue          Value to write to register
     * @param expectedOutput    The text that should be found in the output
     * @param numberOfRegisters Number of registers to read
     *
     * @return True if the expected output is available
     */
    private static boolean execModPoll(int register, int type, Integer outValue, String expectedOutput, int numberOfRegisters) {
        try {
            String output = TestUtils.execToString(String.format("%s -m tcp -p %d -a %d -r %d -t %d -c %d -1 %s %s",
                    modPollTool.toString(), port, UNIT_ID, register, type, numberOfRegisters,
                    LOCALHOST, outValue == null ? "" : outValue));
            boolean returnValue = output != null && output.replaceAll("[\r]", "").contains(expectedOutput);
            if (!returnValue) {
                logger.error(output);
            }
            return returnValue;
        }
        catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }

}
