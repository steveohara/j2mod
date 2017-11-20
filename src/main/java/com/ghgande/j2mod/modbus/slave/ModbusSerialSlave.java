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

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.net.ModbusSerialListener;
import com.ghgande.j2mod.modbus.util.SerialParameters;

/**
 * @author Joe Montanari
 *
 */
public class ModbusSerialSlave extends AbstractModbusSlave<ModbusSerialListener> {

    private SerialParameters serialParams;

    /**
     * @throws ModbusException
     */
    protected ModbusSerialSlave(SerialParameters serialParams) throws ModbusException {
        super();
        this.serialParams = serialParams;
        this.getListener().setSerialParams(serialParams);
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.ghgande.j2mod.modbus.slave.AbstractModbusSlave#initListener()
     */
    @Override
    protected void initListener() {
        this.setListener(new ModbusSerialListener());
    }

    /**
     * @return the serialParams
     */
    public final SerialParameters getSerialParams() {
        return serialParams;
    }

    /**
     * @param serialParams the serialParams to set
     */
    public final void setSerialParams(SerialParameters serialParams) {
        this.serialParams = serialParams;
    }

}
