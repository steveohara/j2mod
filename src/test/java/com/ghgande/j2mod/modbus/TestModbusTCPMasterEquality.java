package com.ghgande.j2mod.modbus;

import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.utils.AbstractTestModbusTCPMaster;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

/**
 * Testing class for {@link ModbusTCPMaster#equals} &amp; {@link ModbusTCPMaster#hashCode} methods
 */
public class TestModbusTCPMasterEquality extends AbstractTestModbusTCPMaster {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTestModbusTCPMaster.class);
    public static final String LOCALHOST_2 = "127.0.0.2";
    public static final int PORT_2 = 2503;
    protected static ModbusTCPMaster master_2, master_3;

    @Test
    public void testEquality() {
        master_2 = new ModbusTCPMaster(LOCALHOST_2, PORT);
        master_3 = new ModbusTCPMaster(LOCALHOST, PORT_2);

        assertThat("2 unequal Modbus TCP masters identified as equal", master, not(equalTo(master_2)));
        assertThat("2 unequal Modbus TCP masters identified as equal", master, not(equalTo(master_3)));
        assertThat("2 unequal Modbus TCP masters identified as equal", master_2, not(equalTo(master_3)));

        assertThat("2 equal Modbus TCP masters identified as unequal", master, equalTo(master));
        assertThat("2 equal Modbus TCP masters identified as unequal", master_2, equalTo(master_2));
        assertThat("2 equal Modbus TCP masters identified as unequal", master_3, equalTo(master_3));
    }

    @Test
    public void testHashCode() {
        master_2 = new ModbusTCPMaster(LOCALHOST_2, PORT);
        master_3 = new ModbusTCPMaster(LOCALHOST, PORT_2);

        assertThat("2 unequal Modbus TCP masters had same hash code", master.hashCode(), not(equalTo(master_2.hashCode())));
        assertThat("2 unequal Modbus TCP masters had same hash code", master.hashCode(), not(equalTo(master_3.hashCode())));
        assertThat("2 unequal Modbus TCP masters had same hash code", master_2.hashCode(), not(equalTo(master_3.hashCode())));

        assertThat("2 equal Modbus TCP masters had NOT same hash code", master.hashCode(), equalTo(master.hashCode()));
        assertThat("2 equal Modbus TCP masters had NOT same hash code", master_2.hashCode(), equalTo(master_2.hashCode()));
        assertThat("2 equal Modbus TCP masters had NOT same hash code", master_3.hashCode(), equalTo(master_3.hashCode()));
    }
}
