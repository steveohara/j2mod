package com.ghgande.j2mod.modbus.util;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.net.AbstractSerialConnection;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SerialParametersTest {

    @Test
    public void testSetAndGetParityWithValidParameters() {
        SerialParameters serialParameters = new SerialParameters();
        serialParameters.setParity("none");
        assertEquals(serialParameters.getParity(), AbstractSerialConnection.NO_PARITY);
        serialParameters.setParity("even");
        assertEquals(serialParameters.getParity(), AbstractSerialConnection.EVEN_PARITY);
        serialParameters.setParity("odd");
        assertEquals(serialParameters.getParity(), AbstractSerialConnection.ODD_PARITY);
        serialParameters.setParity("mark");
        assertEquals(serialParameters.getParity(), AbstractSerialConnection.MARK_PARITY);
        serialParameters.setParity("space");
        assertEquals(serialParameters.getParity(), AbstractSerialConnection.SPACE_PARITY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetParityThrowsExceptionWithNullValue() {
        SerialParameters serialParameters = new SerialParameters();
        serialParameters.setParity(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetParityThrowsExceptionWithInvalidValue() {
        SerialParameters serialParameters = new SerialParameters();
        serialParameters.setParity("parity");
    }

    @Test
    public void testSetEncodingWithValidValues() {
        SerialParameters serialParameters = new SerialParameters();
        serialParameters.setEncoding("ASCII");
        assertEquals(serialParameters.getEncoding(), Modbus.SERIAL_ENCODING_ASCII);

        serialParameters.setEncoding("rtU");
        assertEquals(serialParameters.getEncoding(), Modbus.SERIAL_ENCODING_RTU);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEncodingThrowsExceptionWithInvalidValue() {
        SerialParameters serialParameters = new SerialParameters();
        serialParameters.setEncoding("ascll");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEncodingThrowsExceptionWithNull() {
        SerialParameters serialParameters = new SerialParameters();
        serialParameters.setEncoding(null);
    }

    @Test
    public void testSetFlowControlInWithValidValues() {
        SerialParameters serialParameters = new SerialParameters();
        serialParameters.setFlowControlIn("none");
        assertEquals(serialParameters.getFlowControlIn(), AbstractSerialConnection.FLOW_CONTROL_DISABLED);

        serialParameters.setFlowControlIn("xon/xoff out");
        assertEquals(serialParameters.getFlowControlIn(), AbstractSerialConnection.FLOW_CONTROL_XONXOFF_OUT_ENABLED);

        serialParameters.setFlowControlIn("xon/xoff in");
        assertEquals(serialParameters.getFlowControlIn(), AbstractSerialConnection.FLOW_CONTROL_XONXOFF_IN_ENABLED);

        serialParameters.setFlowControlIn("rts/cts");
        assertEquals(serialParameters.getFlowControlIn(), AbstractSerialConnection.FLOW_CONTROL_CTS_ENABLED | AbstractSerialConnection.FLOW_CONTROL_RTS_ENABLED);

        serialParameters.setFlowControlIn("dsr/dtr");
        assertEquals(serialParameters.getFlowControlIn(), AbstractSerialConnection.FLOW_CONTROL_DSR_ENABLED | AbstractSerialConnection.FLOW_CONTROL_DTR_ENABLED);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetFlowControlInWithNull() {
        SerialParameters serialParameters = new SerialParameters();
        serialParameters.setFlowControlIn(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetFlowControlInWithInvalidValue() {
        SerialParameters serialParameters = new SerialParameters();
        serialParameters.setFlowControlIn("bigFlow");
    }


    @Test(expected = IllegalArgumentException.class)
    public void testSetRs485DelayBeforeTxMicrosecondsWithInvalidValue() {
        SerialParameters serialParameters = new SerialParameters();
        serialParameters.setRs485DelayBeforeTxMicroseconds(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetRs485DelayAfterTxMicrosecondsWithInvalidValue() {
        SerialParameters serialParameters = new SerialParameters();
        serialParameters.setRs485DelayAfterTxMicroseconds(-1);
    }

    @Test
    public void testSetStopbitsWithValidValues() {
        SerialParameters serialParameters = new SerialParameters();

        serialParameters.setStopbits("1");
        assertEquals(serialParameters.getStopbits(), AbstractSerialConnection.ONE_STOP_BIT);

        serialParameters.setStopbits("1.5");
        assertEquals(serialParameters.getStopbits(), AbstractSerialConnection.ONE_POINT_FIVE_STOP_BITS);

        serialParameters.setStopbits("2");
        assertEquals(serialParameters.getStopbits(), AbstractSerialConnection.TWO_STOP_BITS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStopbitsWithInValidValues() {
        SerialParameters serialParameters = new SerialParameters();
        serialParameters.setStopbits("");
    }

    @Test
    public void testSetStopbitsIntWithValidValues() {
        SerialParameters serialParameters = new SerialParameters();

        serialParameters.setStopbits(1);
        assertEquals(serialParameters.getStopbits(), AbstractSerialConnection.ONE_STOP_BIT);

        serialParameters.setStopbits(2);
        assertEquals(serialParameters.getStopbits(), AbstractSerialConnection.ONE_POINT_FIVE_STOP_BITS);

        serialParameters.setStopbits(3);
        assertEquals(serialParameters.getStopbits(), AbstractSerialConnection.TWO_STOP_BITS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStopbitsIntWithInValidValues() {
        SerialParameters serialParameters = new SerialParameters();
        serialParameters.setStopbits(4);
    }

    @Test
    public void testSetFlowControlInIntWithValidValues() {
        SerialParameters serialParameters = new SerialParameters();

        serialParameters.setFlowControlIn(0);
        assertEquals(serialParameters.getFlowControlIn(), AbstractSerialConnection.FLOW_CONTROL_DISABLED);

        serialParameters.setFlowControlIn(1048576);
        assertEquals(serialParameters.getFlowControlIn(), AbstractSerialConnection.FLOW_CONTROL_XONXOFF_OUT_ENABLED);

        serialParameters.setFlowControlIn(65536);
        assertEquals(serialParameters.getFlowControlIn(), AbstractSerialConnection.FLOW_CONTROL_XONXOFF_IN_ENABLED);

        serialParameters.setFlowControlIn(17);
        assertEquals(serialParameters.getFlowControlIn(), AbstractSerialConnection.FLOW_CONTROL_CTS_ENABLED | AbstractSerialConnection.FLOW_CONTROL_RTS_ENABLED);

        serialParameters.setFlowControlIn(4352);
        assertEquals(serialParameters.getFlowControlIn(), AbstractSerialConnection.FLOW_CONTROL_DSR_ENABLED | AbstractSerialConnection.FLOW_CONTROL_DTR_ENABLED);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetFlowControlIn() {
        SerialParameters serialParameters = new SerialParameters();
        serialParameters.setFlowControlIn(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetFlowControlOut() {
        SerialParameters serialParameters = new SerialParameters();
        serialParameters.setFlowControlOut(-1);
    }
}