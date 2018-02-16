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
package com.ghgande.j2mod.modbus.net;

import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.io.ModbusUDPTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Class that implements a ModbusUDPListener.<br>
 *
 * @author Dieter Wimberger
 * @author Julie Haugh
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public class ModbusUDPListener extends AbstractModbusListener {

    private static final Logger logger = LoggerFactory.getLogger(ModbusUDPListener.class);
    private UDPSlaveTerminal terminal;

    /**
     * Create a new <tt>ModbusUDPListener</tt> instance listening to the given
     * interface address.
     *
     * @param ifc an <tt>InetAddress</tt> instance.
     */
    public ModbusUDPListener(InetAddress ifc) {
        address = ifc;
        listening = true;
    }

    /**
     * Constructs a new ModbusUDPListener instance. The address will be set to a
     * default value of the wildcard local address and the default Modbus port.
     */
    public ModbusUDPListener() {
        try {
            address = InetAddress.getByAddress(new byte[]{0, 0, 0, 0});
        }
        catch (UnknownHostException e) {
            // Can't happen -- length is fixed by code.
        }
    }

    @Override
    public void setTimeout(int timeout) {
        super.setTimeout(timeout);
        if (terminal != null && listening) {
            terminal.setTimeout(timeout);
        }
    }

    /**
     * Starts this <tt>ModbusUDPListener</tt>.
     */
    @Override
    public void run() {
        ModbusUDPTransport transport;
        try {
            if (address == null) {
                terminal = new UDPSlaveTerminal(InetAddress.getByAddress(new byte[]{0, 0, 0, 0}));
            }
            else {
                terminal = new UDPSlaveTerminal(address);
            }
            terminal.setTimeout(timeout);
            terminal.setPort(port);
            terminal.activate();
            transport = new ModbusUDPTransport(terminal);
        }

        // Catch any fatal errors and set the listening flag to false to indicate an error
        catch (Exception e) {
            error = String.format("Cannot start UDP listener - %s", e.getMessage());
            listening = false;
            return;
        }

        listening = true;
        try {
            while (listening) {
                handleRequest(transport, this);
            }
        }
        catch (ModbusIOException ex1) {
            if (!ex1.isEOF()) {
                logger.error("Exception occurred before EOF while handling request", ex1);
            }
        }
        finally {
            try {
                terminal.deactivate();
                transport.close();
            }
            catch (Exception ex) {
                // ignore
            }
        }
    }

    @Override
    public void stop() {
        terminal.deactivate();
        listening = false;
    }
}
