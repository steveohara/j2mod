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
import com.ghgande.j2mod.modbus.ModbusCoupler;
import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.io.AbstractModbusTransport;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.procimg.ProcessImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class implementing a handler for incoming Modbus/TCP requests.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public class TCPConnectionHandler implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(TCPConnectionHandler.class);

    private TCPSlaveConnection connection;
    private AbstractModbusTransport transport;

    /**
     * Constructs a new <tt>TCPConnectionHandler</tt> instance.
     *
     * <p>
     * The connections will be handling using the <tt>ModbusCouple</tt> class
     * and a <tt>ProcessImage</tt> which provides the interface between the
     * slave implementation and the <tt>TCPSlaveConnection</tt>.
     *
     * @param con an incoming connection.
     */
    public TCPConnectionHandler(TCPSlaveConnection con) {
        setConnection(con);
    }

    /**
     * Sets a connection to be handled by this <tt>
     * TCPConnectionHandler</tt>.
     *
     * @param con a <tt>TCPSlaveConnection</tt>.
     */
    public void setConnection(TCPSlaveConnection con) {
        connection = con;
        transport = connection.getModbusTransport();
    }

    public void run() {
        try {
            do {
                // Read the request
                ModbusRequest request = transport.readRequest();
                ModbusResponse response;

                // Test if Process image exists and the Unit ID is good
                ProcessImage spi = ModbusCoupler.getReference().getProcessImage();
                if (spi == null ||
                        (spi.getUnitID() != 0 && request.getUnitID() != spi.getUnitID())) {
                    response = request.createExceptionResponse(Modbus.ILLEGAL_ADDRESS_EXCEPTION);
                }
                else {
                    response = request.createResponse();
                }
                logger.debug("Request:{}", request.getHexMessage());
                logger.debug("Response:{}", response.getHexMessage());

                // Write the response message.
                transport.writeMessage(response);
            } while (true);
        }
        catch (ModbusIOException ex) {
            if (!ex.isEOF()) {
                logger.debug("ModbusIOException occurred before EOF", ex);
            }
        }
        finally {
            try {
                connection.close();
            }
            catch (Exception ex) {
                // ignore
            }
        }
    }
}