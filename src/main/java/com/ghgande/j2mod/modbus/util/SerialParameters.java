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
package com.ghgande.j2mod.modbus.util;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.net.AbstractSerialConnection;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Helper class wrapping all serial port communication parameters.
 * Very similar to the javax.comm demos, however, not the same.
 *
 * @author Dieter Wimberger
 * @author John Charlton
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class SerialParameters {

    private static final boolean DEFAULT_RS485_MODE = false;
    private static final boolean DEFAULT_RS485_TX_ENABLE_ACTIVE_HIGH = true;
    private static final boolean DEFAULT_RS485_ENABLE_TERMINATION = false;
    private static final boolean DEFAULT_RS485_TX_DURING_RX = false;
    private static final int DEFAULT_RS485_DELAY_BEFORE_TX_MICROSECONDS = 1000;
    private static final int DEFAULT_RS485_DELAY_AFTER_TX_MICROSECONDS = 1000;

    //instance attributes
    private String portName;
    private int baudRate;
    private int flowControlIn;
    private int flowControlOut;
    private int databits;
    private int stopbits;
    private int parity;
    private String encoding;
    private boolean echo;
    private int openDelay;
    private boolean rs485Mode;
    private boolean rs485TxEnableActiveHigh;
    private boolean rs485EnableTermination;
    private boolean rs485RxDuringTx;
    private int rs485DelayBeforeTxMicroseconds;
    private int rs485DelayAfterTxMicroseconds;

    /**
     * Constructs a new <tt>SerialParameters</tt> instance with
     * default values.
     */
    public SerialParameters() {
        portName = "";
        baudRate = 9600;
        flowControlIn = AbstractSerialConnection.FLOW_CONTROL_DISABLED;
        flowControlOut = AbstractSerialConnection.FLOW_CONTROL_DISABLED;
        databits = 8;
        stopbits = AbstractSerialConnection.ONE_STOP_BIT;
        parity = AbstractSerialConnection.NO_PARITY;
        // Historically, the encoding has been null which got converted to RTU
        // by SerialConnection.open(). Let's make it more explicit which serial
        // protocol will be used by default.
        encoding = Modbus.SERIAL_ENCODING_RTU;
        echo = false;
        openDelay = AbstractSerialConnection.OPEN_DELAY;
        rs485Mode = DEFAULT_RS485_MODE;
        rs485TxEnableActiveHigh = DEFAULT_RS485_TX_ENABLE_ACTIVE_HIGH;
        rs485DelayBeforeTxMicroseconds = DEFAULT_RS485_DELAY_BEFORE_TX_MICROSECONDS;
        rs485DelayAfterTxMicroseconds = DEFAULT_RS485_DELAY_AFTER_TX_MICROSECONDS;
    }

    /**
     * Constructs a new <tt>SerialParameters</tt> instance with
     * given parameters for a regular serial interface.
     *
     * @param portName       The name of the port.
     * @param baudRate       The baud rate.
     * @param flowControlIn  Type of flow control for receiving.
     * @param flowControlOut Type of flow control for sending.
     * @param databits       The number of data bits.
     * @param stopbits       The number of stop bits. com.fazecast.jSerialComm.SerialPort stop-bit constants should be used in this method (ONE_STOP_BIT, ONE_POINT_FIVE_STOP_BITS, TWO_STOP_BITS)
     * @param parity         The type of parity.
     * @param echo           Flag for setting the RS485 echo mode.
     */
    public SerialParameters(String portName, int baudRate,
                            int flowControlIn,
                            int flowControlOut,
                            int databits,
                            int stopbits,
                            int parity,
                            boolean echo) {
        // Perform default initialization and update fields of interest
        // afterwards.
        this();
        this.portName = portName;
        this.baudRate = baudRate;
        this.flowControlIn = flowControlIn;
        this.flowControlOut = flowControlOut;
        this.databits = databits;
        this.stopbits = stopbits;
        this.parity = parity;
        this.echo = echo;
    }

    /**
     * Constructs a new <tt>SerialParameters</tt> instance with given
     * parameters for a serial interface in RS-485 mode on Linux.
     * <p>
     * RS-485 half-duplex mode is only available on Linux and only if the
     * device driver supports it. Its configuration parameters have no effect
     * on other platforms.
     * <p>
     * There are interfaces operating in RS-485 mode by default and don't
     * require explicitly configuring this mode.
     *
     * @param portName                       The name of the port.
     * @param baudRate                       The baud rate.
     * @param flowControlIn                  Type of flow control for receiving.
     * @param flowControlOut                 Type of flow control for sending.
     * @param databits                       The number of data bits.
     * @param stopbits                       The number of stop bits. com.fazecast.jSerialComm.SerialPort stop-bit constants should be used in this method (ONE_STOP_BIT, ONE_POINT_FIVE_STOP_BITS, TWO_STOP_BITS)
     * @param parity                         The type of parity.
     * @param echo                           Flag for setting the RS485 echo mode.
     * @param rs485Mode                      Whether to enable RS-485 mode
     *                                       (transmitter control)
     * @param rs485TxEnableActiveHigh        Whether the RS-485 transmitter is
     *                                       enabled when the by a high or low logic level
     * @param rs485DelayBeforeTxMicroseconds The length of the delay between
     *                                       enabling the transmitter and the
     *                                       actual start of sending data
     * @param rs485DelayAfterTxMicroseconds  The length of the delay between
     *                                       the end of a data transmission and
     *                                       disabling the transmitter again
     */
    public SerialParameters(String portName, int baudRate,
                            int flowControlIn,
                            int flowControlOut,
                            int databits,
                            int stopbits,
                            int parity,
                            boolean echo,
                            boolean rs485Mode,
                            boolean rs485TxEnableActiveHigh,
                            int rs485DelayBeforeTxMicroseconds,
                            int rs485DelayAfterTxMicroseconds
                            ) {
        // Perform default non-RS-485 initialization and update fields of
        // interest afterwards.
        this(portName, baudRate, flowControlIn, flowControlOut, databits, stopbits, parity, echo);
        this.rs485Mode = rs485Mode;
        this.rs485TxEnableActiveHigh = rs485TxEnableActiveHigh;
        this.rs485DelayBeforeTxMicroseconds = rs485DelayBeforeTxMicroseconds;
        this.rs485DelayAfterTxMicroseconds = rs485DelayAfterTxMicroseconds;
    }

    /**
     * Constructs a new <tt>SerialParameters</tt> instance with
     * parameters obtained from a <tt>Properties</tt> instance.
     *
     * @param props  a <tt>Properties</tt> instance.
     * @param prefix a prefix for the properties keys if embedded into
     *               other properties.
     */
    public SerialParameters(Properties props, String prefix) {
        if (prefix == null) {
            prefix = "";
        }
        setPortName(props.getProperty(prefix + "portName", ""));
        setBaudRate(props.getProperty(prefix + "baudRate", "" + 9600));
        setFlowControlIn(props.getProperty(prefix + "flowControlIn", "" + AbstractSerialConnection.FLOW_CONTROL_DISABLED));
        setFlowControlOut(props.getProperty(prefix + "flowControlOut", "" + AbstractSerialConnection.FLOW_CONTROL_DISABLED));
        setParity(props.getProperty(prefix + "parity", "" + AbstractSerialConnection.NO_PARITY));
        setDatabits(props.getProperty(prefix + "databits", "8"));
        setStopbits(props.getProperty(prefix + "stopbits", "" + AbstractSerialConnection.ONE_STOP_BIT));
        setEncoding(props.getProperty(prefix + "encoding", Modbus.DEFAULT_SERIAL_ENCODING));
        setEcho("true".equals(props.getProperty(prefix + "echo")));
        setOpenDelay(props.getProperty(prefix + "openDelay", "" + AbstractSerialConnection.OPEN_DELAY));

        setRs485Mode("true".equals(props.getProperty(prefix + "rs485Mode", Boolean.toString(DEFAULT_RS485_MODE))));
        setRs485TxEnableActiveHigh("true".equals(props.getProperty(prefix + "rs485TxEnableActiveHigh", Boolean.toString(DEFAULT_RS485_TX_ENABLE_ACTIVE_HIGH))));
        setRs485DelayBeforeTxMicroseconds(props.getProperty(prefix + "rs485DelayBeforeTxMicroseconds", Integer.toString(DEFAULT_RS485_DELAY_BEFORE_TX_MICROSECONDS)));
        setRs485DelayAfterTxMicroseconds(props.getProperty(prefix + "rs485DelayAfterTxMicroseconds", Integer.toString(DEFAULT_RS485_DELAY_AFTER_TX_MICROSECONDS)));
    }

    /**
     * Returns the port name.
     *
     * @return the port name.
     */
    public String getPortName() {
        return portName;
    }

    /**
     * Sets the port name.
     *
     * @param name the new port name.
     */
    public void setPortName(String name) {
        portName = name;
    }

    /**
     * Sets the baud rate.
     *
     * @param rate the new baud rate.
     */
    public void setBaudRate(int rate) {
        baudRate = rate;
    }

    /**
     * Return the baud rate as <tt>int</tt>.
     *
     * @return the baud rate as <tt>int</tt>.
     */
    public int getBaudRate() {
        return baudRate;
    }

    /**
     * Sets the baud rate.
     *
     * @param rate the new baud rate.
     */
    public void setBaudRate(String rate) {
        baudRate = Integer.parseInt(rate);
    }

    /**
     * Returns the baud rate as a <tt>String</tt>.
     *
     * @return the baud rate as <tt>String</tt>.
     */
    public String getBaudRateString() {
        return Integer.toString(baudRate);
    }

    /**
     * Sets the type of flow control for the input
     * as given by the passed in <tt>int</tt>.
     *
     * @param flowControl the new flow control type.
     * @throws IllegalArgumentException on invalid flowControl
     */
    public void setFlowControlIn(int flowControl) {
        if (!this.isValidFlowControlInt(flowControl)) {
            throw new IllegalArgumentException("Invalid flowControl passed in setFlowControlIn: " + flowControl +
                    ". Valid values are: " + this.validFlowControls());
        }
        flowControlIn = flowControl;
    }

    /**
     * Returns the input flow control type as <tt>int</tt>.
     *
     * @return the input flow control type as <tt>int</tt>.
     */
    public int getFlowControlIn() {
        return flowControlIn;
    }

    /**
     * Sets the type of flow control for the input
     * as given by the passed in <tt>String</tt>.
     *
     * @param flowControl the flow control for reading type.
     * @throws IllegalArgumentException on invalid flowControl
     */
    public void setFlowControlIn(String flowControl) {
        flowControlIn = stringToFlow(flowControl);
    }

    /**
     * Returns the input flow control type as <tt>String</tt>.
     *
     * @return the input flow control type as <tt>String</tt>.
     */
    public String getFlowControlInString() {
        return flowToString(flowControlIn);
    }

    /**
     * Sets the output flow control type as given
     * by the passed in <tt>int</tt>.
     *
     * @param flowControlOut new output flow control type as <tt>int</tt>.
     */
    public void setFlowControlOut(int flowControlOut) {
        if (!this.isValidFlowControlInt(flowControlOut)) {
            throw new IllegalArgumentException("Invalid flowControl passed in setFlowControlOut: " + flowControlOut +
                    ". Valid values are: " + this.validFlowControls());
        }
        this.flowControlOut = flowControlOut;
    }

    /**
     * Returns the list of valid flow controls IN or OUT
     *
     * @return List<Integer>
     */
    private List<Integer> validFlowControls() {
        return Arrays.asList(AbstractSerialConnection.FLOW_CONTROL_DISABLED,
                AbstractSerialConnection.FLOW_CONTROL_XONXOFF_OUT_ENABLED,
                AbstractSerialConnection.FLOW_CONTROL_XONXOFF_IN_ENABLED,
                AbstractSerialConnection.FLOW_CONTROL_CTS_ENABLED | AbstractSerialConnection.FLOW_CONTROL_RTS_ENABLED,
                AbstractSerialConnection.FLOW_CONTROL_DSR_ENABLED | AbstractSerialConnection.FLOW_CONTROL_DTR_ENABLED);
    }


    /**
     * +
     * Returns whether an int is a valid flow control
     *
     * @param control the int to test
     * @return control validity
     */
    private boolean isValidFlowControlInt(int control) {
        return this.validFlowControls().contains(control);
    }

    /**
     * Returns the output flow control type as <tt>int</tt>.
     *
     * @return the output flow control type as <tt>int</tt>.
     */
    public int getFlowControlOut() {
        return flowControlOut;
    }

    /**
     * Sets the output flow control type as given
     * by the passed in <tt>String</tt>.
     *
     * @param flowControlOut the new output flow control type as <tt>String</tt>.
     */
    public void setFlowControlOut(String flowControlOut) {
        this.flowControlOut = stringToFlow(flowControlOut);
    }

    /**
     * Returns the output flow control type as <tt>String</tt>.
     *
     * @return the output flow control type as <tt>String</tt>.
     */
    public String getFlowControlOutString() {
        return flowToString(flowControlOut);
    }

    /**
     * Sets the number of data bits.
     *
     * @param databits the new number of data bits.
     */
    public void setDatabits(int databits) {
        this.databits = databits;
    }

    /**
     * Returns the number of data bits as <tt>int</tt>.
     *
     * @return the number of data bits as <tt>int</tt>.
     */
    public int getDatabits() {
        return databits;
    }

    /**
     * Sets the number of data bits from the given <tt>String</tt>.
     *
     * @param databits the new number of data bits as <tt>String</tt>.
     */
    public void setDatabits(String databits) {
        if (!ModbusUtil.isBlank(databits) && databits.matches("[0-9]+")) {
            this.databits = Integer.parseInt(databits);
        }
        else {
            this.databits = 8;
        }
    }

    /**
     * Returns the number of data bits as <tt>String</tt>.
     *
     * @return the number of data bits as <tt>String</tt>.
     */
    public String getDatabitsString() {
        return databits + "";
    }

    /**
     * Sets the number of stop bits. com.fazecast.jSerialComm.SerialPort stop-bit constants should be used in this method (ONE_STOP_BIT, ONE_POINT_FIVE_STOP_BITS, TWO_STOP_BITS)
     *
     * @param stopbits the new number of stop bits setting. 
     */
    public void setStopbits(int stopbits) {
        if (stopbits <0 || stopbits > 3) {
            throw new IllegalArgumentException("Invalid parameter passed in setStobits. Valid values are: [1, 2, 3].");
        }
        this.stopbits = stopbits;
    }

    /**
     * Returns the number of stop bits as <tt>int</tt>. com.fazecast.jSerialComm.SerialPort stop-bit constants are used in this method (ONE_STOP_BIT, ONE_POINT_FIVE_STOP_BITS, TWO_STOP_BITS)
     *
     * @return the number of stop bits as <tt>int</tt>.
     */
    public int getStopbits() {
        return stopbits;
    }

    /**
     * Sets the number of stop bits from the given <tt>String</tt>.
     *
     * @param stopbits the number of stop bits as <tt>String</tt>.
     */
    public void setStopbits(String stopbits) {
        if ("1".equals(stopbits)) {
            this.stopbits = AbstractSerialConnection.ONE_STOP_BIT;
        }
        else if ("1.5".equals(stopbits)) {
            this.stopbits = AbstractSerialConnection.ONE_POINT_FIVE_STOP_BITS;
        }
        else if ("2".equals(stopbits)) {
            this.stopbits = AbstractSerialConnection.TWO_STOP_BITS;
        }
        else {
            throw new IllegalArgumentException("Invalid stopBit passed in setStopbits. Valid values are: [1, 1.5, 2]");
        }
    }

    /**
     * Returns the number of stop bits as <tt>String</tt>.
     *
     * @return the number of stop bits as <tt>String</tt>.
     */
    public String getStopbitsString() {
        switch (stopbits) {
            case AbstractSerialConnection.ONE_STOP_BIT:
                return "1";
            case AbstractSerialConnection.ONE_POINT_FIVE_STOP_BITS:
                return "1.5";
            case AbstractSerialConnection.TWO_STOP_BITS:
                return "2";
            default:
                return "1";
        }
    }

    /**
     * Sets the parity schema.
     *
     * @param parity the new parity schema as <tt>int</tt>.
     */
    public void setParity(int parity) {
        this.parity = parity;
    }

    /**
     * Returns the parity schema as <tt>int</tt>.
     *
     * @return the parity schema as <tt>int</tt>.
     */
    public int getParity() {
        return parity;
    }

    /**
     * Sets the parity schema from the given
     * <tt>String</tt>.
     *
     * @param parity the new parity schema as <tt>String</tt>.
     * @throws IllegalArgumentException on invalid parity
     */
    public void setParity(String parity) {
        if ("none".equalsIgnoreCase(parity)) {
            this.parity = AbstractSerialConnection.NO_PARITY;
        }
        else if ("even".equalsIgnoreCase(parity)) {
            this.parity = AbstractSerialConnection.EVEN_PARITY;
        }
        else if ("odd".equalsIgnoreCase(parity)) {
            this.parity = AbstractSerialConnection.ODD_PARITY;
        }
        else if ("mark".equalsIgnoreCase(parity)) {
            this.parity = AbstractSerialConnection.MARK_PARITY;
        }
        else if ("space".equalsIgnoreCase(parity)) {
            this.parity = AbstractSerialConnection.SPACE_PARITY;
        }
        else {
            throw new IllegalArgumentException("Invalid value passed in setParity: " + parity + ". Valid values are [none, even, odd, mark, space]");
        }
    }

    /**
     * Returns the parity schema as <tt>String</tt>.
     *
     * @return the parity schema as <tt>String</tt>.
     */
    public String getParityString() {
        switch (parity) {
            case AbstractSerialConnection.NO_PARITY:
                return "none";
            case AbstractSerialConnection.EVEN_PARITY:
                return "even";
            case AbstractSerialConnection.ODD_PARITY:
                return "odd";
            case AbstractSerialConnection.MARK_PARITY:
                return "mark";
            case AbstractSerialConnection.SPACE_PARITY:
                return "space";
            default:
                return "none";
        }
    }

    /**
     * Returns the encoding to be used.
     *
     * @return the encoding as string.
     *
     * @see Modbus#SERIAL_ENCODING_ASCII
     * @see Modbus#SERIAL_ENCODING_RTU
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the encoding to be used.
     *
     * @param enc the encoding as string.
     * @throws IllegalArgumentException on invalid parity
     * @see Modbus#SERIAL_ENCODING_ASCII
     * @see Modbus#SERIAL_ENCODING_RTU
     */
    public void setEncoding(String enc) {
        if (Modbus.SERIAL_ENCODING_ASCII.equalsIgnoreCase(enc)) {
            this.encoding = Modbus.SERIAL_ENCODING_ASCII;
        } else if (Modbus.SERIAL_ENCODING_RTU.equalsIgnoreCase(enc)) {
            this.encoding = Modbus.SERIAL_ENCODING_RTU;
        } else {
            throw new IllegalArgumentException("Invalid value passed in setEncoding: " + enc
                    + ". Valid values are [" + Modbus.SERIAL_ENCODING_ASCII + ", " + Modbus.SERIAL_ENCODING_RTU + "]");
        }

    }

    /**
     * Get the Echo value.
     *
     * @return the Echo value.
     */
    public boolean isEcho() {
        return echo;
    }

    /**
     * Set the Echo value.
     *
     * @param newEcho The new Echo value.
     */
    public void setEcho(boolean newEcho) {
        echo = newEcho;
    }

    /**
     * Converts a <tt>String</tt> describing a flow control type to the
     * <tt>int</tt> which is defined in SerialPort.
     *
     * @param flowControl the <tt>String</tt> describing the flow control type.
     * @return the <tt>int</tt> describing the flow control type.
     * @throws IllegalArgumentException on invalid flowControl
     */
    private int stringToFlow(String flowControl) {
        if ("none".equalsIgnoreCase(flowControl)) {
            return AbstractSerialConnection.FLOW_CONTROL_DISABLED;
        }
        else if ("xon/xoff out".equalsIgnoreCase(flowControl)) {
            return AbstractSerialConnection.FLOW_CONTROL_XONXOFF_OUT_ENABLED;
        }
        else if ("xon/xoff in".equalsIgnoreCase(flowControl)) {
            return AbstractSerialConnection.FLOW_CONTROL_XONXOFF_IN_ENABLED;
        }
        else if ("rts/cts".equalsIgnoreCase(flowControl)) {
            return AbstractSerialConnection.FLOW_CONTROL_CTS_ENABLED | AbstractSerialConnection.FLOW_CONTROL_RTS_ENABLED;
        }
        else if ("dsr/dtr".equalsIgnoreCase(flowControl)) {
            return AbstractSerialConnection.FLOW_CONTROL_DSR_ENABLED | AbstractSerialConnection.FLOW_CONTROL_DTR_ENABLED;
        }
        else {
            throw new IllegalArgumentException("Invalid value passed as flowControl: " + flowControl
                    + ". Valid values are ['none', 'xon/xoff out', 'xon/xoff in', 'rts/cts', 'dsr/dtr']");
        }
    }

    /**
     * Converts an <tt>int</tt> describing a flow control type to a
     * String describing a flow control type.
     *
     * @param flowcontrol the <tt>int</tt> describing the
     *                    flow control type.
     * @return the <tt>String</tt> describing the flow control type.
     */
    private String flowToString(int flowcontrol) {
        switch (flowcontrol) {
            case AbstractSerialConnection.FLOW_CONTROL_DISABLED:
                return "none";
            case AbstractSerialConnection.FLOW_CONTROL_XONXOFF_OUT_ENABLED:
                return "xon/xoff out";
            case AbstractSerialConnection.FLOW_CONTROL_XONXOFF_IN_ENABLED:
                return "xon/xoff in";
            case AbstractSerialConnection.FLOW_CONTROL_CTS_ENABLED:
                return "rts/cts";
            case AbstractSerialConnection.FLOW_CONTROL_DTR_ENABLED:
                return "dsr/dtr";
            default:
                return "none";
        }
    }

    /**
     * Gets the open delay used to prevent some OS from losing the comms port
     *
     * @return Sleep before an open is attempted on a comms port
     */
    public int getOpenDelay() {
        return openDelay;
    }

    /**
     * Sets the sleep time tat occurs just prior to opening a coms port
     * Some OS don't like to have their comms ports opened/closed in very quick succession
     * particularly, virtual ports. This delay is a rather crude way of stopping the problem that
     * a comms port doesn't re-appear immediately after a close
     *
     * @param openDelay Sleep time in millieseconds
     */
    public void setOpenDelay(int openDelay) {
        this.openDelay = openDelay;
    }

    /**
     * Sets the sleep time tat occurs just prior to opening a coms port
     * Some OS don't like to have their comms ports opened/closed in very quick succession
     * particularly, virtual ports. This delay is a rather crude way of stopping the problem that
     * a comms port doesn't re-appear immediately after a close
     *
     * @param openDelay Sleep time in millieseconds
     */
    public void setOpenDelay(String openDelay) {
        this.openDelay = Integer.parseInt(openDelay);
    }

    /**
     * Returns whether RS-485 half-duplex mode is enabled.
     * <p>
     * RS-485 half-duplex mode is only available on Linux and only if the
     * device driver supports it. Its configuration parameters have no effect
     * on other platforms.
     *
     * @return Whether RS-485 mode is enabled
     */
    public boolean getRs485Mode() {
        return rs485Mode;
    }

    /**
     * Sets whether to configure the serial interface into RS-485 half-duplex
     * mode.
     * <p>
     * RS-485 half-duplex mode is only available on Linux and only if the
     * device driver supports it. Its configuration parameters have no effect
     * on other platforms.
     *
     * @param enable Whether to enable RS-485 half-duplex mode
     */
    public void setRs485Mode(boolean enable) {
        rs485Mode = enable;
    }

    /**
     * Returns whether the RS-485 transmitter is enabled by a high or low
     * control signal.
     * <p>
     * RS-485 half-duplex mode is only available on Linux and only if the
     * device driver supports it. Its configuration parameters have no effect
     * on other platforms.
     *
     * @return Wether the RS-485 transmitter is enabled by a high control
     *         signal level. Otherwise it returns <tt>false</tt>.
     */
    public boolean getRs485TxEnableActiveHigh() {
        return rs485TxEnableActiveHigh;
    }

    /**
     * Sets whether the RS-485 transmitter is enabled by a high or low control
     * signal.
     * <p>
     * RS-485 half-duplex mode is only available on Linux and only if the
     * device driver supports it. Its configuration parameters have no effect
     * on other platforms.
     *
     * @param activeHigh If <tt>true</tt>, the transmitter is activated by a
     *                   high control signal level. Otherwise, it is activated
     *                   by a low level.
     */
    public void setRs485TxEnableActiveHigh(boolean activeHigh) {
        rs485TxEnableActiveHigh = activeHigh;
    }

    /**
     * Returns whether the RS-485 interface shall enable internal bus
     * termination.
     * <p>
     * This configuration option is only available under Linux and only if
     * device driver and hardware support it.
     *
     * @return Whether the serial interface shall enable internal bus
     *         termination.
     */
    public boolean getRs485EnableTermination() {
        return rs485EnableTermination;
    }

    /**
     * Sets whether the RS-485 interface shall enable internal bus termination.
     * <p>
     * This configuration option is only available under Linux and only if
     * device driver and hardware support it.
     *
     * @param enable If <tt>true</tt>, the serial interface shall enable its
     *               internal bus termination.
     */
    public void setRs485EnableTermination(boolean enable) {
        rs485EnableTermination = enable;
    }

    /**
     * Returns whether the RS-485 interface receives data it sends.
     * <p>
     * This configuration option is only available under Linux and only if
     * device driver and hardware support it.
     * <p>
     * See {@link #setRs485RxDuringTx} for more details.
     *
     * @return Whether the serial interface shall receive the data it transmits
     *         too.
     */
    public boolean getRs485RxDuringTx() {
        return rs485RxDuringTx;
    }

    /**
     * Sets whether the RS-485 interface receives the data its sends.
     * <p>
     * This configuration option is only available under Linux and only if
     * device driver and hardware support it.
     * <p>
     * <b>BEWARE: For normal operation, j2mod expects this feature do be
     * disable. This method is provided only for fixing the behaviour with
     * certain device drivers which require this feature to be enabled for
     * normal operation.</b>
     *
     * @param enable If <tt>true</tt>, the serial interface is expected to
     *               receive the data it transmits itself too.
     */
    public void setRs485RxDuringTx(boolean enable) {
        rs485RxDuringTx = enable;
    }

    /**
     * Returns the delay between activating the RS-485 transmitter and actually
     * sending data. There are devices in the field requiring such a delay for
     * start bit detection.
     * <p>
     * Please note that the actual interface might not support a resolution
     * down to microseconds and might require appropriately large values for
     * actually generating a delay.
     * <p>
     * RS-485 half-duplex mode is only available on Linux and only if the
     * device driver supports it. Its configuration parameters have no effect
     * on other platforms.
     *
     * @return The configured delay in microseconds
     */
    public int getRs485DelayBeforeTxMicroseconds() {
        return rs485DelayBeforeTxMicroseconds;
    }

    /**
     * Sets the delay between activating the RS-485 transmitter and actually
     * sending data. There are devices in the field requiring such a delay for
     * start bit detection.
     * <p>
     * Please note that the actual interface might not support a resolution
     * down to microseconds and might require appropriately large values for
     * actually generating a delay.
     * <p>
     * RS-485 half-duplex mode is only available on Linux and only if the
     * device driver supports it. Its configuration parameters have no effect
     * on other platforms.
     *
     * @param microseconds The delay in microseconds
     * @throws IllegalArgumentException on strictly negative input
     */
    public void setRs485DelayBeforeTxMicroseconds(int microseconds) {
        if (microseconds < 0) {
            throw new IllegalArgumentException("Expecting non-negative delay.");
        }

        rs485DelayBeforeTxMicroseconds = microseconds;
    }

    /**
     * Sets the delay between activating the RS-485 transmitter and actually
     * sending data. There are devices in the field requiring such a delay for
     * start bit detection.
     * <p>
     * This is a convenience wrapper around
     * {@link #setRs485DelayBeforeTxMicroseconds(int)} which parses the delay
     * from the supplied string. See the documentation of this method for more
     * details.
     *
     * @param microseconds The string to parse the delay value from
     */
    public void setRs485DelayBeforeTxMicroseconds(String microseconds) {
        setRs485DelayBeforeTxMicroseconds(Integer.parseInt(microseconds));
    }

    /**
     * Returns the delay between the end of transmitting data and deactivating
     * the RS-485 transmitter.
     * <p>
     * Please note that the actual interface might not support a resolution
     * down to microseconds and might require appropriately large values for
     * actually generating a delay.
     * <p>
     * RS-485 half-duplex mode is only available on Linux and only if the
     * device driver supports it. Its configuration parameters have no effect
     * on other platforms.
     *
     * @return The configured delay in microseconds
     */
    public int getRs485DelayAfterTxMicroseconds() {
        return rs485DelayAfterTxMicroseconds;
    }

    /**
     * Sets the delay between the end of transmitting data and deactivating the
     * RS-485 transmitter.
     * <p>
     * Please note that the actual interface might not support a resolution
     * down to microseconds and might require appropriately large values for
     * actually generating a delay.
     * <p>
     * RS-485 half-duplex mode is only available on Linux and only if the
     * device driver supports it. Its configuration parameters have no effect
     * on other platforms.
     *
     * @param microseconds The delay in microseconds
     */
    public void setRs485DelayAfterTxMicroseconds(int microseconds) {
        if (microseconds < 0) {
            throw new IllegalArgumentException("Expecting non-negative delay.");
        }

        rs485DelayAfterTxMicroseconds = microseconds;
    }

    /**
     * Sets the delay between end of transmitting data and deactivating the
     * RS-458 transmitter.
     * <p>
     * This is a convenience wrapper around
     * {@link #setRs485DelayAfterTxMicroseconds(int)} which parses the delay
     * from the supplied string. See the documentation of this method for more
     * details.
     *
     * @param microseconds The string to parse the delay value from
     */
    public void setRs485DelayAfterTxMicroseconds(String microseconds) {
        setRs485DelayAfterTxMicroseconds(Integer.parseInt(microseconds));
    }

    @Override
    public String toString() {
        return "SerialParameters{" +
                "portName='" + portName + '\'' +
                ", baudRate=" + baudRate +
                ", flowControlIn=" + flowControlIn +
                ", flowControlOut=" + flowControlOut +
                ", databits=" + databits +
                ", stopbits=" + stopbits +
                ", parity=" + parity +
                ", encoding='" + encoding + '\'' +
                ", echo=" + echo +
                ", openDelay=" + openDelay +
                ", rs485Mode=" + rs485Mode +
                ", rs485TxEnableActiveHight=" + rs485TxEnableActiveHigh +
                ", rs485EnableTermination=" + rs485EnableTermination +
                ", rs485RxDuringTx" + rs485RxDuringTx +
                ", rs485DelayBeforeTxMicroseconds=" + rs485DelayBeforeTxMicroseconds +
                ", rs485DelayAfterTxMicroseconds=" + rs485DelayAfterTxMicroseconds +
                '}';
    }
}
