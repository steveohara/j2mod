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

import com.ghgande.j2mod.modbus.procimg.DefaultProcessImageFactory;
import com.ghgande.j2mod.modbus.procimg.ProcessImage;
import com.ghgande.j2mod.modbus.procimg.ProcessImageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Class implemented following a Singleton pattern, to couple the slave side
 * with a master side or with a device.
 *
 * <p>
 * At the moment it only provides a reference to the OO model of the process
 * image.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 *
 * @deprecated As of 2.3.4 This mechanism for handling process images has been superseded by a more flexible mechanism {@link com.ghgande.j2mod.modbus.slave.ModbusSlave} and {@link com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory}
 */
@Deprecated
public class ModbusCoupler {

    private static final Logger logger = LoggerFactory.getLogger(ModbusCoupler.class);

    // class attributes
    private static ModbusCoupler modbusCoupler; // Singleton reference

    // instance attributes
    private Map<Integer, ProcessImage> processImages = new HashMap<Integer, ProcessImage>();
    private boolean master = true;
    private ProcessImageFactory processImageFactory;

    /**
     * A private constructor which creates a default process image.
     */
    private ModbusCoupler() {
        processImageFactory = new DefaultProcessImageFactory();
    }

    public static synchronized boolean isInitialized() {
        return modbusCoupler != null;
    }

    /**
     * Returns a reference to the singleton instance.
     *
     * @return the <tt>ModbusCoupler</tt> instance reference.
     */
    public static synchronized ModbusCoupler getReference() {
        if (modbusCoupler == null) {
            modbusCoupler = new ModbusCoupler();
        }
        return modbusCoupler;
    }

    /**
     * Returns the actual <tt>ProcessImageFactory</tt> instance.
     *
     * @return a <tt>ProcessImageFactory</tt> instance.
     */
    public ProcessImageFactory getProcessImageFactory() {
        return processImageFactory;
    }

    /**
     * Sets the <tt>ProcessImageFactory</tt> instance.
     *
     * @param factory the instance to be used for creating process image instances.
     */
    public void setProcessImageFactory(ProcessImageFactory factory) {
        processImageFactory = factory;
    }

    /**
     * Returns a reference to the <tt>ProcessImage</tt> by <tt>unitID</tt> of this
     * <tt>ModbusCoupler</tt>.
     *
     * @param unitID the <tt>unitID</tt> of the <tt>ProcessImage</tt> to fetch.
     * @return the <tt>ProcessImage</tt>.
     */
    public synchronized ProcessImage getProcessImage(int unitID) {
        return processImages.get(unitID);
    }

    /**
     * Sets the reference to the <tt>ProcessImage</tt> by the <tt>unitID</tt>
     * specified in the <tt>ProcessImage</tt> of this <tt>ModbusCoupler</tt>.
     *
     * @param procimg the <tt>ProcessImage</tt> to be set.
     */
    public synchronized void setProcessImage(ProcessImage procimg) {
        processImages.put(procimg.getUnitID(), procimg);
    }

    /**
     * Tests if this instance is a master device.
     *
     * @return true if master, false otherwise.
     */
    public boolean isMaster() {
        return master;
    }

    /**
     * Sets this instance to be or not to be a master device.
     *
     * @param master true if master device, false otherwise.
     */
    public void setMaster(boolean master) {
        this.master = master;
    }

    /**
     * Tests if this instance is not a master device.
     *
     * @return true if slave, false otherwise.
     */
    public boolean isSlave() {
        return !master;
    }
}
