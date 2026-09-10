package com.ghgande.j2mod.modbus.net;

import com.ghgande.j2mod.modbus.util.SerialParameters;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SerialConnectionTest {

    @Test
    public void testBitsPerCharacter_Standard8N1() {
        SerialParameters parameters = new SerialParameters("", 9600,
                AbstractSerialConnection.FLOW_CONTROL_DISABLED,
                AbstractSerialConnection.FLOW_CONTROL_DISABLED,
                8,
                AbstractSerialConnection.ONE_STOP_BIT,
                AbstractSerialConnection.NO_PARITY,
                false
        );
        SerialConnection serialCon = new SerialConnection(parameters);

        // 1 start-bit + 8 data-bits + 0 parity + 1 stop-bit = 10
        assertEquals(10.0, serialCon.getBitsPerCharacter(), 0.001);
    }

    @Test
    public void testBitsPerCharacter_FiveBitsAndOnePointFiveStopBits() {
        SerialParameters parameters = new SerialParameters("", 9600,
                AbstractSerialConnection.FLOW_CONTROL_DISABLED,
                AbstractSerialConnection.FLOW_CONTROL_DISABLED,
                5,
                AbstractSerialConnection.ONE_POINT_FIVE_STOP_BITS,
                AbstractSerialConnection.NO_PARITY,
                false
        );
        SerialConnection serialCon = new SerialConnection(parameters);

        // 1 start-bit + 5 data-bits + 0 parity + 1.5 stop-bits = 7.5
        assertEquals(7.5, serialCon.getBitsPerCharacter(), 0.001);
    }

    @Test
    public void testBitsPerCharacter_WithParityAndTwoStopBits() {
        SerialParameters parameters = new SerialParameters("", 9600,
                AbstractSerialConnection.FLOW_CONTROL_DISABLED,
                AbstractSerialConnection.FLOW_CONTROL_DISABLED,
                8,
                AbstractSerialConnection.TWO_STOP_BITS,
                AbstractSerialConnection.MARK_PARITY,
                false
        );
        SerialConnection serialCon = new SerialConnection(parameters);

        // 1 start-bit + 8 data-bits + 1 parity + 2 stop-bits = 12
        assertEquals(12.0, serialCon.getBitsPerCharacter(), 0.001);
    }

    @Test
    public void testStopBits_OneStopBits() {
        SerialParameters parameters = new SerialParameters();
        parameters.setStopbits(AbstractSerialConnection.ONE_STOP_BIT);

        SerialConnection serialCon = new SerialConnection(parameters);
        assertEquals(1.0, serialCon.getStopBits(), 0.001);
    }

    @Test
    public void testStopBits_OnePointFiveStopBits() {
        SerialParameters parameters = new SerialParameters();
        parameters.setStopbits(AbstractSerialConnection.ONE_POINT_FIVE_STOP_BITS);

        SerialConnection serialCon = new SerialConnection(parameters);
        assertEquals(1.5, serialCon.getStopBits(), 0.001);
    }

    @Test
    public void testStopBits_TwoStopBits() {
        SerialParameters parameters = new SerialParameters();
        parameters.setStopbits(AbstractSerialConnection.TWO_STOP_BITS);

        SerialConnection serialCon = new SerialConnection(parameters);
        assertEquals(2.0, serialCon.getStopBits(), 0.001);
    }

}