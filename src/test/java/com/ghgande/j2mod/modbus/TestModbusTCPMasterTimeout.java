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
import com.ghgande.j2mod.modbus.utils.AbstractTestModbusTCPMaster;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * This class tests the TCP master read features of the library
 */
@SuppressWarnings("ConstantConditions")
public class TestModbusTCPMasterTimeout extends AbstractTestModbusTCPMaster {

    @BeforeClass
    public static void setUpSlave() {
        long start = 0;
        try {
            master = new ModbusTCPMaster("apple.com", PORT);
            start = System.currentTimeMillis();
            master.setTimeout(1000);
            master.connect();
            master.disconnect();
            fail("Somehow, the master has found a slave at mythical-modbus.com");
        }
        catch (Exception e) {
            long time = System.currentTimeMillis() - start;
            assertTrue(String.format("Timeout is not respected [%d] should be approximately 1000", time), time >= 1000 && time < 1200);
        }
    }


    @Test
    public void testReadTimeout() {
    }

}
