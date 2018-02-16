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

/**
 * Class that implements a <tt>ModbusSlaveException</tt>. Instances of this
 * exception are thrown when the slave returns a Modbus exception.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public class ModbusSlaveException extends ModbusException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instance type attribute
     */
    private int type = -1;

    /**
     * <p>
     * Constructs a new <tt>ModbusSlaveException</tt> instance with the given
     * type.
     *
     * <p>
     * Types are defined according to the protocol specification in
     * <tt>net.wimpi.modbus.Modbus</tt>.
     *
     * @param type the type of exception that occurred.
     */
    public ModbusSlaveException(int type) {
        super();

        this.type = type;
    }

    /**
     * Get the exception type message associated with the given exception
     * number.
     *
     * @param type Numerical value of the Modbus exception.
     *
     * @return a String indicating the type of slave exception.
     */
    public static String getMessage(int type) {
        switch (type) {
            case 1:
                return "Illegal Function";
            case 2:
                return "Illegal Data Address";
            case 3:
                return "Illegal Data Value";
            case 4:
                return "Slave Device Failure";
            case 5:
                return "Acknowledge";
            case 6:
                return "Slave Device Busy";
            case 8:
                return "Memory Parity Error";
            case 10:
                return "Gateway Path Unavailable";
            case 11:
                return "Gateway Target Device Failed to Respond";
        }
        return "Error Code = " + type;
    }

    /**
     * <p>
     * Returns the type of this <tt>ModbusSlaveException</tt>. <br>
     * Types are defined according to the protocol specification in
     * <tt>net.wimpi.modbus.Modbus</tt>.
     *
     * @return the type of this <tt>ModbusSlaveException</tt>.
     */
    public int getType() {
        return type;
    }

    /**
     * <p>
     * Tests if this <tt>ModbusSlaveException</tt> is of a given type.
     *
     * <p>
     * Types are defined according to the protocol specification in
     * <tt>net.wimpi.modbus.Modbus</tt>.
     *
     * @param TYPE the type to test this <tt>ModbusSlaveException</tt> type
     *             against.
     *
     * @return true if this <tt>ModbusSlaveException</tt> is of the given type,
     * false otherwise.
     */
    public boolean isType(int TYPE) {
        return (TYPE == type);
    }

    /**
     * Get the exception type message associated with this exception.
     *
     * @return a String indicating the type of slave exception.
     */
    public String getMessage() {
        return getMessage(type);
    }
}
