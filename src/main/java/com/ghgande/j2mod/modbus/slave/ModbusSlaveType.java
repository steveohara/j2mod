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
package com.ghgande.j2mod.modbus.slave;

import com.ghgande.j2mod.modbus.util.ModbusUtil;

/**
 * Descibes the types of Modbus Slaves
 */
public enum ModbusSlaveType {
    TCP, UDP, SERIAL;

    /**
     * Returns true if this type is one of those listed
     *
     * @param types Array of types to check for
     * @return True if this is one of the array
     */
    public boolean is(ModbusSlaveType... types) {
        if (!ModbusUtil.isBlank(types)) {
            for (ModbusSlaveType type : types) {
                if (equals(type)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns a unique key for this port and type
     *
     * @param port Port number
     * @return Unique key
     */
    public String getKey(int port) {
        return toString() + port;
    }

    /**
     * Returns a unique key for this port and type
     *
     * @param port Port number
     * @return Unique key
     */
    public String getKey(String port) {
        if (ModbusUtil.isBlank(port)) {
            throw new IllegalArgumentException("Port must not be null or empty");
        }
        return this + port;
    }
}
