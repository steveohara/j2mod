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

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.io.AbstractModbusTransport;
import com.ghgande.j2mod.modbus.io.ModbusRTUTransport;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransport;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Create a <tt>ModbusListener</tt> from an URI-like specifier.
 *
 * @author Julie
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public class ModbusMasterFactory {

    private static final Logger logger = LoggerFactory.getLogger(ModbusMasterFactory.class);

    public static AbstractModbusTransport createModbusMaster(String address, AbstractSerialConnection serialConnection) {
        return createCustomModbusMaster(address, serialConnection);
    }

    public static AbstractModbusTransport createModbusMaster(String address) {
        return createCustomModbusMaster(address, null);
    }

    private static AbstractModbusTransport createCustomModbusMaster(String address, AbstractSerialConnection serialConnection) {
        String parts[] = address.split(" *: *");
        if (parts.length < 2) {
            throw new IllegalArgumentException("missing connection information");
        }

        if (parts[0].equalsIgnoreCase("device")) {
            /*
             * Create a ModbusSerialListener with the default Modbus values of
             * 19200 baud, no parity, using the specified device. If there is an
             * additional part after the device name, it will be used as the
             * Modbus unit number.
             */
            SerialParameters parms = new SerialParameters();
            parms.setPortName(parts[1]);
            parms.setBaudRate(9600);
            parms.setDatabits(8);
            parms.setParity(AbstractSerialConnection.NO_PARITY);
            parms.setStopbits(1);
            parms.setFlowControlIn(AbstractSerialConnection.FLOW_CONTROL_DISABLED);
            parms.setEcho(false);
            try {
                ModbusRTUTransport transport = new ModbusRTUTransport();
                if (serialConnection == null) {
                    transport.setCommPort(SerialConnection.getCommPort(parms.getPortName()));
                }
                else {
                    transport.setCommPort(serialConnection);
                }
                transport.setEcho(false);
                return transport;
            }
            catch (IOException e) {
                return null;
            }
        }
        else if (parts[0].equalsIgnoreCase("tcp")) {
            /*
             * Create a ModbusTCPListener with the default interface value. The
             * second optional value is the TCP port number and the third
             * optional value is the Modbus unit number.
             */
            String hostName = parts[1];
            int port = Modbus.DEFAULT_PORT;

            if (parts.length > 2) {
                port = Integer.parseInt(parts[2]);
            }

            try {
                Socket socket = new Socket(hostName, port);
                logger.debug("connecting to {}", socket.toString());

                return new ModbusTCPTransport(socket);
            }
            catch (UnknownHostException x) {
                return null;
            }
            catch (IOException e) {
                return null;
            }
        }
        else if (parts[0].equalsIgnoreCase("udp")) {
            /*
             * Create a ModbusUDPListener with the default interface value. The
             * second optional value is the TCP port number and the third
             * optional value is the Modbus unit number.
             */
            String hostName = parts[1];
            int port = Modbus.DEFAULT_PORT;

            if (parts.length > 2) {
                port = Integer.parseInt(parts[2]);
            }

            UDPMasterTerminal terminal;
            try {
                terminal = new UDPMasterTerminal(InetAddress.getByName(hostName));
                terminal.setPort(port);
                terminal.activate();
            }
            catch (UnknownHostException e) {
                e.printStackTrace();
                return null;
            }
            catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return terminal.getTransport();
        }
        else {
            throw new IllegalArgumentException("unknown type " + parts[0]);
        }
    }
}
