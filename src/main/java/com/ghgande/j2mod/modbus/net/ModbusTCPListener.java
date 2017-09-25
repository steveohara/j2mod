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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ghgande.j2mod.modbus.ModbusCoupler;
import com.ghgande.j2mod.modbus.procimg.ProcessImage;
import com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory;
import com.ghgande.j2mod.modbus.slave.ModbusTCPSlave;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class that implements a ModbusTCPListener.
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
    private ExecutorService threadPool;
    private Thread listener;
    private boolean useRtuOverTcp;

    /**
     * Constructor.
     */
    public ModbusTCPListener() {
        
    }

    /**
     * Constructs a ModbusTCPListener instance.<br>
     *
     * @param poolsize the size of the ThreadPool used to handle incoming
     *            requests.
     * @param addr the interface to use for listening.
     */
    public ModbusTCPListener(int poolsize, InetAddress addr) {
        this(poolsize, addr, false);
    }

    /**
     * Constructs a ModbusTCPListener instance.<br>
     *
     * @param poolsize the size of the ThreadPool used to handle incoming
     *            requests.
     * @param addr the interface to use for listening.
     * @param useRtuOverTcp True if the RTU protocol should be used over TCP
     */
    public ModbusTCPListener(int poolsize, InetAddress addr, boolean useRtuOverTcp) {
        this( Executors.newFixedThreadPool(poolsize),addr,useRtuOverTcp);
    }
    
    /**
     * Constructs a ModbusTCPListener instance.<br>
     *
     * @param pool the <tt>ExecutorService</tt> used to handle incoming
     *            requests.
     * @param addr the interface to use for listening.
     * @param useRtuOverTcp True if the RTU protocol should be used over TCP
     */
    public ModbusTCPListener(ExecutorService pool, InetAddress addr, boolean useRtuOverTcp) {
        threadPool = pool;
        try {
            address = addr==null?InetAddress.getByAddress(new byte[] { 0, 0, 0, 0 }):addr;
        } catch (UnknownHostException e) {
         // Can't happen -- size is fixed.
        }
        this.useRtuOverTcp = useRtuOverTcp;
    }

    /**
     * /**
     * Constructs a ModbusTCPListener instance. This interface is created
     * to listen on the wildcard address (0.0.0.0), which will accept TCP packets
     * on all available adapters/interfaces
     *
     * @param poolsize the size of the <tt>ThreadPool</tt> used to handle incoming
     *            requests.
     */
    public ModbusTCPListener(int poolsize) {
        this(poolsize, false);
    }

    /**
     * /**
     * Constructs a ModbusTCPListener instance. This interface is created
     * to listen on the wildcard address (0.0.0.0), which will accept TCP packets
     * on all available adapters/interfaces
     *
     * @param poolsize the size of the <tt>ThreadPool</tt> used to handle incoming
     *            requests.
     * @param useRtuOverTcp True if the RTU protocol should be used over TCP
     */
    public ModbusTCPListener(int poolsize, boolean useRtuOverTcp) {
        this(poolsize,null,useRtuOverTcp);

    }

    @Override
    public void setTimeout(int timeout) {
        super.setTimeout(timeout);
        if (serverSocket != null && listening) {
            try {
                serverSocket.setSoTimeout(timeout);
            } catch (SocketException e) {
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
            int floodProtection = 100;
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
                Socket incoming;
                try {
                    incoming = serverSocket.accept();
                } catch (SocketTimeoutException e) {
                    continue;
                }
                logger.debug("Making new connection {}", incoming.toString());
                if (listening) {
                    threadPool.execute(new TCPConnectionHandler(this, new TCPSlaveConnection(incoming, useRtuOverTcp)));
                } else {
                    incoming.close();
                }
            }
        } catch (IOException e) {
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
                threadPool.shutdown();
            }
        } catch (Exception ex) {
            logger.error("Error while stopping ModbusTCPListener", ex);
        }
    }

    /**
     * @return the useRtuOverTcp
     */
    public final boolean isUseRtuOverTcp() {
        return useRtuOverTcp;
    }

    /**
     * @param useRtuOverTcp the useRtuOverTcp to set
     */
    public final void setUseRtuOverTcp(boolean useRtuOverTcp) {
        this.useRtuOverTcp = useRtuOverTcp;
    }

    /**
     * @return the threadPool
     */
    public final ExecutorService getThreadPool() {
        return threadPool;
    }

    /**
     * @param threadPool the threadPool to set
     */
    public final void setThreadPool(ExecutorService threadPool) {
        this.threadPool = threadPool;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ghgande.j2mod.modbus.net.AbstractModbusListener#getProcessImage(int)
     */
    @Override
    public ProcessImage getProcessImage(int unitId) {
        ModbusTCPSlave slave = ModbusSlaveFactory.getTCPSlave(address, port);
        if (slave != null) {
            return slave.getProcessImage(unitId);
        } else {

            // Legacy: Use the ModbusCoupler if no image was associated with the listener
            // This will be removed when the ModbusCoupler is removed

            return ModbusCoupler.getReference().getProcessImage(unitId);
        }
    }

}
