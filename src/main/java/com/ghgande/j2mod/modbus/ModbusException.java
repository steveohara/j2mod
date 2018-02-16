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
 * Superclass of all specialised exceptions in this package.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public class ModbusException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new <tt>ModbusException</tt> instance.
     */
    public ModbusException() {
        super();
    }

    /**
     * Constructs a new <tt>ModbusException</tt> instance with the given
     * message.
     * <p>
     *
     * @param message the message describing this <tt>ModbusException</tt>.
     */
    public ModbusException(String message) {
        super(message);
    }

    /**
     * Constructs a new <tt>ModbusException</tt> instance with the given
     * message.
     * <p>
     *
     * @param message the message describing this <tt>ModbusException</tt>.
     * @param values optional values of the exception
     */
    public ModbusException(String message, Object... values) {
        super(String.format(message, values));
    }

    /**
     * Constructs a new <tt>ModbusException</tt> instance with the given
     * message and underlying cause.
     * <p>
     *
     * @param message the message describing this <tt>ModbusException</tt>.
     * @param cause   the cause (which is saved for later retrieval by the {@code getCause()} method).
     *                (A {@code null} value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public ModbusException(String message, Throwable cause) {
        super(message, cause);
    }
}
