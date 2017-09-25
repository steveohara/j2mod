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

import java.net.InetAddress;
import java.util.concurrent.ExecutorService;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.net.ModbusUDPListener;

/**
 * @author Joe Montanari
 *
 */
public class ModbusUDPSlave extends AbstractModbusSlave<ModbusUDPListener>{
    private int port;
    private InetAddress address;
 
    
    protected ModbusUDPSlave(InetAddress address, int port,ExecutorService threadPool) throws ModbusException{
        super();
        this.address=address;
        this.port=port;
        
        this.getListener().setAddress(address);
        this.getListener().setPort(port);
        //this.getListener().setThreadPool(threadPool);TODO implement this. UDP Listener does not implement any threading.

    }


    /**
     * @return the port
     */
    public final int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public final void setPort(int port) {
        this.port = port;
    }


    /**
     * @return the address
     */
    public final InetAddress getAddress() {
        return address;
    }


    /**
     * @param address the address to set
     */
    public final void setAddress(InetAddress address) {
        this.address = address;
    }


    /* (non-Javadoc)
     * @see com.ghgande.j2mod.modbus.slave.AbstractModbusSlave#initListener()
     */
    @Override
    protected void initListener() {
        this.setListener(new ModbusUDPListener());
        
    }
}
