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

import com.ghgande.j2mod.modbus.util.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;

/**
 * Class that implements a ModbusTCPListener.
 *
 * <p>
 * If listening, it accepts incoming requests passing them on to be handled.
 * If not listening, silently drops the requests.
 *
 * @author Dieter Wimberger
 * @author Julie Haugh
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public class ModbusTCPListener extends AbstractModbusListener {

    private static final Logger logger = LoggerFactory.getLogger(ModbusTCPListener.class);

    private ServerSocket serverSocket = null;
    private ThreadPool threadPool;
    private Thread listener;

    /**
     * Constructs a ModbusTCPListener instance.<br>
     *
     * @param poolsize the size of the <tt>ThreadPool</tt> used to handle incoming
     *                 requests.
     * @param addr     the interface to use for listening.
     */
    public ModbusTCPListener(int poolsize, InetAddress addr) {
        threadPool = new ThreadPool(poolsize);
        address = addr;
    }

    /**
     * /**
     * Constructs a ModbusTCPListener instance.  This interface is created
     * to listen on the wildcard address (0.0.0.0), which will accept TCP packets
     * on all available adapters/interfaces
     *
     * @param poolsize the size of the <tt>ThreadPool</tt> used to handle incoming
     *                 requests.
     */
    public ModbusTCPListener(int poolsize) {
        threadPool = new ThreadPool(poolsize);
        try {
            address = InetAddress.getByAddress(new byte[]{0, 0, 0, 0});
        }
        catch (UnknownHostException ex) {
            // Can't happen -- size is fixed.
        }
    }

    @Override
    public void setTimeout(int timeout) {
        super.setTimeout(timeout);
        if (serverSocket != null && listening) {
            try {
                serverSocket.setSoTimeout(timeout);
            }
            catch (SocketException e) {
                logger.error("Cannot set socket timeout", e);
            }
        }
    }

    @Override
    public void run() {
        try {
            /*
             * A server socket is opened with a connectivity queue of a size
             * specified in int floodProtection. Concurrent login handling under
             * normal circumstances should be alright, denial of service
             * attacks via massive parallel program logins can probably be
             * prevented.
             */
            int floodProtection = 5;
            serverSocket = new ServerSocket(port, floodProtection, address);
            serverSocket.setSoTimeout(timeout);
            logger.debug("Listening to {} (Port {})", serverSocket.toString(), port);
        }

        // Catch any fatal errors and set the listening flag to false to indicate an error
        catch (Exception e) {
            error = String.format("Cannot start TCP listener - %s", e.getMessage());
            listening = false;
            return;
        }

        listener = Thread.currentThread();
        listening = true;
        try {

            // Infinite loop, taking care of resources in case of a lot of
            // parallel logins
            listening = true;
            while (listening) {
                Socket incoming = serverSocket.accept();
                logger.debug("Making new connection {}", incoming.toString());
                if (listening) {
                    threadPool.execute(new TCPConnectionHandler(new TCPSlaveConnection(incoming)));
                }
                else {
                    incoming.close();
                }
            }
        }
        catch (IOException e) {
            error = String.format("Problem starting listener - %s", e.getMessage());
        }
    }

    @Override
    public void stop() {
        listening = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            if (listener != null) {
                listener.join();
            }
            if (threadPool != null) {
                threadPool.close();
            }
        }
        catch (Exception ex) {
            logger.error("Error while stopping ModbusTCPListener", ex);
        }
    }

}
