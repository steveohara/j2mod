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

import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory;
import com.ghgande.j2mod.modbus.utils.AbstractTestModbus;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Regression test for the executor leak in {@code TCPConnectionHandler}. <br>
 *
 * Each TCP connection with {@code maxIdleSeconds > 0} creates a
 * {@link java.util.concurrent.ScheduledExecutorService} watchdog thread.
 * Before the fix, this executor was only shut down when the idle timeout
 * fired. If the connection ended normally (client disconnect), the watchdog
 * thread was leaked. <br>
 *
 * This test verifies the fix by:
 * 1. Creating a slave with maxIdleSeconds enabled (so watchdog executors are created)
 * 2. Recording the baseline thread count
 * 3. Opening and closing many connections that end normally (not via idle timeout)
 * 4. Asserting that no significant number of threads were leaked
 */
public class TestTCPConnectionHandlerExecutorLeak extends AbstractTestModbus {

    private static final Logger logger = LoggerFactory.getLogger(TestTCPConnectionHandlerExecutorLeak.class);

    private static final int LEAK_TEST_PORT = 2504;
    private static final int POOL_SIZE = 5;
    private static final int MAX_IDLE_SECONDS = 300;
    private static final int CONNECTION_COUNT = 50;

    @BeforeClass
    public static void setUpSlave() {
        try {
            slave = ModbusSlaveFactory.createTCPSlave(null, LEAK_TEST_PORT, POOL_SIZE, false, MAX_IDLE_SECONDS);
            slave.addProcessImage(UNIT_ID, getSimpleProcessImage());
            slave.open();
        }
        catch (Exception e) {
            fail(String.format("Cannot initialise test slave - %s", e.getMessage()));
        }
    }

    @AfterClass
    public static void tearDownSlave() {
        if (slave != null) {
            slave.close();
            slave = null;
        }
    }

    @Test
    public void testNoWatchdogThreadLeakOnNormalDisconnect() throws Exception {
        ModbusTCPMaster warmup = new ModbusTCPMaster(LOCALHOST, LEAK_TEST_PORT);
        warmup.setTimeout(3000);
        warmup.connect();
        warmup.readMultipleRegisters(UNIT_ID, 0, 1);
        warmup.disconnect();

        // warmup
        Thread.sleep(500);

        Set<Long> baselineThreadIds = getScheduledExecutorThreadIds();
        int baselineCount = baselineThreadIds.size();
        logger.info("Baseline scheduled executor threads: {}", baselineCount);

        for (int i = 0; i < CONNECTION_COUNT; i++) {
            ModbusTCPMaster master = new ModbusTCPMaster(LOCALHOST, LEAK_TEST_PORT);
            master.setTimeout(3000);
            master.connect();
            master.readMultipleRegisters(UNIT_ID, 0, 1);
            master.disconnect();
        }

        // complete
        Thread.sleep(2000);

        // Force GC
        System.gc();
        Thread.sleep(500);

        Set<Long> finalThreadIds = getScheduledExecutorThreadIds();
        int finalCount = finalThreadIds.size();
        int leakedThreads = finalCount - baselineCount;

        logger.info("Final scheduled executor threads: {} (leaked: {})", finalCount, leakedThreads);

        assertTrue(
                String.format("Watchdog executor thread leak detected: %d threads leaked after %d connections. " +
                                "Before the fix in TCPConnectionHandler, the ScheduledExecutorService watchdog " +
                                "was not shut down when connections ended normally (only on idle timeout).",
                        leakedThreads, CONNECTION_COUNT),
                leakedThreads < 5
        );
    }

    private static Set<Long> getScheduledExecutorThreadIds() {
        Set<Long> ids = new HashSet<>();
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.isAlive() && t.getName().matches("pool-\\d+-thread-\\d+")) {
                ids.add(t.getId());
            }
        }
        return ids;
    }
}
