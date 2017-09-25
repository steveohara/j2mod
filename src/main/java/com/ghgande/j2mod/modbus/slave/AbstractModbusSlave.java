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

import java.util.HashMap;
import java.util.Map;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.net.AbstractModbusListener;
import com.ghgande.j2mod.modbus.procimg.ProcessImage;

/**
 * @author Joe Montanari
 *
 */
public abstract class AbstractModbusSlave<T extends AbstractModbusListener> {
    private T listener;
    private boolean isRunning;

    protected Map<Integer, ProcessImage> processImages = new HashMap<Integer, ProcessImage>();

    /**
     * Creates an appropriate type of listener
     *
     * @param type Type of slave to create
     * @param address IP address to listen on
     * @param port Port to listen on if IP type
     * @param poolSize Pool size for TCP slaves
     * @param serialParams Serial parameters for serial type slaves
     * @param useRtuOverTcp True if the RTU protocol should be used over TCP
     * @throws ModbusException If a problem occurs e.g. port already in use
     */
    protected AbstractModbusSlave() throws ModbusException {
        this.initListener();
        this.getListener().setListening(true);
        this.getListener().setTimeout(0);

    }

    /**
     * Create an Instance of the ModbusListener.
     */
    protected abstract void initListener();

    
    /**
     * Return if the Slave has any process images.
     * @return
     */
    public boolean hasProcessImages() {
        return processImages.size() > 0;
    }

    /**
     * Start Slave Listener.
     * @throws ModbusException
     */
    public void open() throws ModbusException {

        // Start the listener if it isn' already running

        if (!isRunning) {
            try {
                new Thread(listener).start();
                isRunning = true;
            } catch (Exception x) {
                if (listener != null) {
                    listener.stop();
                }
                throw new ModbusException(x.getMessage());
            }
        }
    }

    /**
     * Stop Slave Listener.
     * @throws ModbusException
     */
    public void close() {
        ModbusSlaveFactory.close(this);
    }

    /**
     * Closes the listener of this slave
     */
    protected void closeListener() {
        if (listener != null && listener.isListening()) {
            listener.stop();
        }
        isRunning = false;
    }

    /**
     * @return the listener
     */
    public final T getListener() {
        return listener;
    }

    /**
     * @param listener the listener to set
     */
    public final void setListener(T listener) {
        this.listener = listener;
    }

    /**
     * Returns the process image for the given Unit ID
     *
     * @param unitId
     * @return Process image
     */
    public ProcessImage getProcessImage(int unitId) {
        return processImages.get(unitId);
    }

    /**
     * Removes the process image for the given Unit ID
     *
     * @param unitId
     * @return Process image
     */
    public ProcessImage removeProcessImage(int unitId) {
        return processImages.remove(unitId);
    }

    /**
     * Adds a process image for the given Unit ID
     *
     * @param unitId
     * @param processImage
     * @return Process image
     */
    public ProcessImage addProcessImage(int unitId, ProcessImage processImage) {
        return processImages.put(unitId, processImage);
    }

    /**
     * @return the isRunning
     */
    public final boolean isRunning() {
        return isRunning;
    }

}
