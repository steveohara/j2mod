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
import com.ghgande.j2mod.modbus.io.AbstractModbusTransport;
import java.util.Timer;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class implementing a handler for incoming Modbus/TCP requests.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class TCPConnectionHandler implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(TCPConnectionHandler.class);
    private static final long watchDogResolution = 5000L; // Check connection status every 5 seconds
    private static final long nanosPerSecond = 1000L * 1000L * 1000L;

    private final TCPSlaveConnection connection;
    private final AbstractModbusTransport transport;
    private final AbstractModbusListener listener;
    
    private final Timer watchDog;

    /**
     * Constructs a new <tt>TCPConnectionHandler</tt> instance.
     *
     * <p>
     * The connections will be handling using the <tt>ModbusCouple</tt> class
     * and a <tt>ProcessImage</tt> which provides the interface between the
     * slave implementation and the <tt>TCPSlaveConnection</tt>.
     *
     * @param listener the listener that handled the incoming request
     * @param connection an incoming connection.
     * @param maxIdleSeconds 0 or maximum inactivity time for the connection
     */
    public TCPConnectionHandler(AbstractModbusListener listener, TCPSlaveConnection connection, final int maxIdleSeconds) {
        this.listener = listener;
        this.connection = connection;
        transport = this.connection.getModbusTransport();
        
        if(maxIdleSeconds != 0) {
            watchDog = new Timer();
            long checkRate = Math.min(maxIdleSeconds * 1000L, watchDogResolution);
            watchDog.schedule(new TimerTask() {
                @Override
                public void run() {
                    long nanosIdle = System.nanoTime() - TCPConnectionHandler.this.connection.getLastActivityTs();
                    if(nanosIdle > (maxIdleSeconds * nanosPerSecond)) {
                        // Watchdog timer elapsed
                        logger.warn("Watchdog expired: " + (nanosIdle / nanosPerSecond) + ", limit: " + maxIdleSeconds);
                        
                        // Socket.close() will cause read operation to fail
                        TCPConnectionHandler.this.connection.close();
                        
                        // Stop the watchdog, it is not needed anymore
                        watchDog.cancel();
                    }
                }
            }, checkRate, checkRate);
        } else {
            watchDog = null;
        }
    }

    @Override
    public void run() {
        try {
            do {
                listener.handleRequest(transport, listener);
            } while (!Thread.currentThread().isInterrupted());
        }
        catch (ModbusIOException ex) {
            if (!ex.isEOF()) {
                logger.debug(ex.getMessage());
            }
        }
        finally {
            connection.close();
        }
    }
}