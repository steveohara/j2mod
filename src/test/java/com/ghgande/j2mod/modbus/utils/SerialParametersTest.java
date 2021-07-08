package com.ghgande.j2mod.modbus.utils;

import com.ghgande.j2mod.modbus.net.AbstractSerialConnection;;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SerialParametersTest {

    private SerialParameters parameters = null;

    @Before
    public void setUp() {
        parameters = new SerialParameters();
    }

    @Test
    public void testConstructorsRs485ModeAndParameters() {
        parameters = new SerialParameters();
        // Check default values for parameter not passed through this
        // constructor.
        assertEquals(false, parameters.getRs485Mode());
        assertEquals(false, parameters.getRs485EnableTermination());
        assertEquals(false, parameters.getRs485RxDuringTx());

        parameters = new SerialParameters(
            "foo",
            42000,
            AbstractSerialConnection.FLOW_CONTROL_DISABLED,
            AbstractSerialConnection.FLOW_CONTROL_DISABLED,
            7,
            AbstractSerialConnection.ONE_STOP_BIT,
            AbstractSerialConnection.EVEN_PARITY,
            false);
        assertEquals(false, parameters.getRs485Mode());
        // Check default values for parameter not passed through this
        // constructor.
        assertEquals(false, parameters.getRs485EnableTermination());
        assertEquals(false, parameters.getRs485RxDuringTx());

        parameters = new SerialParameters(
            "foo",
            42000,
            AbstractSerialConnection.FLOW_CONTROL_DISABLED,
            AbstractSerialConnection.FLOW_CONTROL_DISABLED,
            7,
            AbstractSerialConnection.ONE_STOP_BIT,
            AbstractSerialConnection.EVEN_PARITY,
            false,
            // And now the RS-485 parameters
            true,
            false,
            1234,
            5678);
        assertEquals(true, parameters.getRs485Mode());
        assertEquals(false, parameters.getRs485TxEnableActiveHigh());
        assertEquals(1234, parameters.getRs485DelayBeforeTxMicroseconds());
        assertEquals(5678, parameters.getRs485DelayAfterTxMicroseconds());
        // Check default values for parameter not passed through this
        // constructor.
        assertEquals(false, parameters.getRs485EnableTermination());
        assertEquals(false, parameters.getRs485RxDuringTx());
    }

    @Test
    public void testSetAndGetRs485Mode() {
        parameters.setRs485Mode(true);
        assertEquals(true, parameters.getRs485Mode());

        parameters.setRs485Mode(false);
        assertEquals(false, parameters.getRs485Mode());
    }

    @Test
    public void testSetAndGetRs485TxEnableActiveHigh() {
        parameters.setRs485TxEnableActiveHigh(true);
        assertEquals(true, parameters.getRs485TxEnableActiveHigh());

        parameters.setRs485TxEnableActiveHigh(false);
        assertEquals(false, parameters.getRs485TxEnableActiveHigh());
    }

    @Test
    public void testSetAndGetRs485EnableTermination() {
        parameters.setRs485EnableTermination(true);
        assertEquals(true, parameters.getRs485EnableTermination());

        parameters.setRs485EnableTermination(false);
        assertEquals(false, parameters.getRs485EnableTermination());
    }

    @Test
    public void testSetAndGetRs485RxDuringTx() {
        parameters.setRs485RxDuringTx(true);
        assertEquals(true, parameters.getRs485RxDuringTx());

        parameters.setRs485RxDuringTx(false);
        assertEquals(false, parameters.getRs485RxDuringTx());
    }

    @Test
    public void testSetAndGetRs485DelayBeforeTxMicrosecondsInt() {
        try {
            parameters.setRs485DelayBeforeTxMicroseconds(-1);
            fail();
        }
        catch (IllegalArgumentException _) {
        }

        parameters.setRs485DelayBeforeTxMicroseconds(0);
        assertEquals(0, parameters.getRs485DelayBeforeTxMicroseconds());

        parameters.setRs485DelayBeforeTxMicroseconds(42);
        assertEquals(42, parameters.getRs485DelayBeforeTxMicroseconds());
    }

    @Test
    public void testSetAndGetRs485DelayBeforeTxMicrosecondsString() {
        try {
            parameters.setRs485DelayBeforeTxMicroseconds("-1");
            fail();
        }
        catch (IllegalArgumentException _) {
        }

        parameters.setRs485DelayBeforeTxMicroseconds("0");
        assertEquals(0, parameters.getRs485DelayBeforeTxMicroseconds());

        parameters.setRs485DelayBeforeTxMicroseconds("42");
        assertEquals(42, parameters.getRs485DelayBeforeTxMicroseconds());

        // Parsing accepts only decimal integer values.
        try {
            parameters.setRs485DelayBeforeTxMicroseconds("f");
            fail();
        }
        catch (NumberFormatException _) {
        }
    }

    @Test
    public void testSetAndGetRs485DelayAfterTxMicrosecondsInt() {
        try {
            parameters.setRs485DelayAfterTxMicroseconds(-1);
            fail();
        }
        catch (IllegalArgumentException _) {
        }

        parameters.setRs485DelayAfterTxMicroseconds(0);
        assertEquals(0, parameters.getRs485DelayAfterTxMicroseconds());

        parameters.setRs485DelayAfterTxMicroseconds(42);
        assertEquals(42, parameters.getRs485DelayAfterTxMicroseconds());
    }

    @Test
    public void testSetAndGetRs485DelayAfterTxMicrosecondsString() {
        try {
            parameters.setRs485DelayAfterTxMicroseconds("-1");
            fail();
        }
        catch (IllegalArgumentException _) {
        }

        parameters.setRs485DelayAfterTxMicroseconds("0");
        assertEquals(0, parameters.getRs485DelayAfterTxMicroseconds());

        parameters.setRs485DelayAfterTxMicroseconds("42");
        assertEquals(42, parameters.getRs485DelayAfterTxMicroseconds());

        // Parsing accepts only decimal integer values.
        try {
            parameters.setRs485DelayAfterTxMicroseconds("f");
            fail();
        }
        catch (NumberFormatException _) {
        }
    }
}
