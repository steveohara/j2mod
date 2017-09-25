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

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Assert;
import org.junit.Test;

import com.ghgande.j2mod.modbus.slave.ModbusSerialSlave;
import com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory;
import com.ghgande.j2mod.modbus.slave.ModbusTCPSlave;
import com.ghgande.j2mod.modbus.slave.ModbusUDPSlave;
import com.ghgande.j2mod.modbus.util.SerialParameters;



/**
 * @author Joe Montanari
 *
 */
public class TestModbusSlaveFactory {

    
    /**
     * Test Create/Get/Remove TCP Slaves
     * @throws ModbusException
     */
    @Test
    public void testAddRemoteTCPSlaveSetIP() throws ModbusException, UnknownHostException{
        InetAddress address=InetAddress.getByName("1.2.3.4");
        this.testTCPSlaveAddRemove(address, 502);
       
    }
    
    /**
     * Test Create/Get/Remove TCP Slaves without an IP defined
     * @throws ModbusException
     */
    @Test
    public void testAddRemoteTCPSlaveNoIP() throws ModbusException, UnknownHostException{
        this.testTCPSlaveAddRemove(null, 502);
        this.testTCPSlaveAddRemoveNoIP( 502);
        
    }
    
    
    /**
     * Test Create/Get/Remove UDP Slaves
     *      * @throws ModbusException
     */
    @Test
    public void testAddRemoteUDPSlaveSetIP() throws ModbusException, UnknownHostException{
        InetAddress address=InetAddress.getByName("1.2.3.4");
        this.testUDPSlaveAddRemove(address, 502);
       
    }
    
    /**
     * Test Create/Get/Remove UDP Slaves without an IP defined
     * @throws ModbusException
     */
    @Test
    public void testAddRemoteUDPSlaveNoIP() throws ModbusException, UnknownHostException{
        this.testUDPSlaveAddRemove(null, 502);
        this.testUDPSlaveAddRemoveNoIP( 502);
        
    }
    
    /**
     * Test Create/Get/Remove Serial Slaves.
     * @throws ModbusException
     */
    @Test
    public void testAddRemoveSerialSlave() throws ModbusException{
        SerialParameters serialParams = new SerialParameters();
        serialParams.setPortName("abcd");
        
        this.testSerialSlaveAddRemove(serialParams);
    }
    
    
    /**
     * Test Create/Get/Remove TCP Slaves
     * @param address - address to test
     * @param port - port to test
     * @throws ModbusException
     */
    private void testTCPSlaveAddRemove(InetAddress address, int port) throws ModbusException{
        ModbusTCPSlave slaveCreate = ModbusSlaveFactory.createTCPSlave(address,port,1,false);
        Assert.assertNotNull(slaveCreate);
        ModbusTCPSlave slaveGet = ModbusSlaveFactory.getTCPSlave(address, port);
        Assert.assertNotNull(slaveGet);
        Assert.assertEquals(slaveCreate, slaveGet);
        
        ModbusSlaveFactory.close(slaveGet);
        ModbusTCPSlave slaveGetClose = ModbusSlaveFactory.getTCPSlave(address, port);
        Assert.assertNull(slaveGetClose);
    }
    
    /**
     * Test Create/Get/Remove TCP Slaves without an IP defined
     * @param address - address to test
     * @param port - port to test
     * @throws ModbusException
     */
    private void testTCPSlaveAddRemoveNoIP(int port) throws ModbusException{
        ModbusTCPSlave slaveCreate = ModbusSlaveFactory.createTCPSlave(port,1,false);
        Assert.assertNotNull(slaveCreate);
        ModbusTCPSlave slaveGet = ModbusSlaveFactory.getTCPSlave(null, port);
        Assert.assertNotNull(slaveGet);
        Assert.assertEquals(slaveCreate, slaveGet);
        
        ModbusSlaveFactory.close(slaveGet);
        ModbusTCPSlave slaveGetClose = ModbusSlaveFactory.getTCPSlave(null, port);
        Assert.assertNull(slaveGetClose);
    }
    
    
    /**
     * Test Create/Get/Remove UDP Slaves
     * @param address - address to test
     * @param port - port to test
     * @throws ModbusException
     */
    private void testUDPSlaveAddRemove(InetAddress address, int port) throws ModbusException{
        ModbusUDPSlave slaveCreate = ModbusSlaveFactory.createUDPSlave(address,port);
        Assert.assertNotNull(slaveCreate);
        ModbusUDPSlave slaveGet = ModbusSlaveFactory.getUDPSlave(address, port);
        Assert.assertNotNull(slaveGet);
        Assert.assertEquals(slaveCreate, slaveGet);
        
        ModbusSlaveFactory.close(slaveGet);
        ModbusUDPSlave slaveGetClose = ModbusSlaveFactory.getUDPSlave(address, port);
        Assert.assertNull(slaveGetClose);
    }
    
    /**
     * Test Create/Get/Remove UDP Slaves without an IP defined
     * @param address - address to test
     * @param port - port to test
     * @throws ModbusException
     */
    private void testUDPSlaveAddRemoveNoIP( int port) throws ModbusException{
        ModbusUDPSlave slaveCreate = ModbusSlaveFactory.createUDPSlave(port);
        Assert.assertNotNull(slaveCreate);
        ModbusUDPSlave slaveGet = ModbusSlaveFactory.getUDPSlave(null, port);
        Assert.assertNotNull(slaveGet);
        Assert.assertEquals(slaveCreate, slaveGet);
        
        ModbusSlaveFactory.close(slaveGet);
        ModbusUDPSlave slaveGetClose = ModbusSlaveFactory.getUDPSlave(null, port);
        Assert.assertNull(slaveGetClose);
    }
    
    /**
     * Test Create/Get/Remove TCP Slaves
     * @param address - address to test
     * @param port - port to test
     * @throws ModbusException
     */
    private void testSerialSlaveAddRemove(SerialParameters serialParams) throws ModbusException{
        ModbusSerialSlave slaveCreate = ModbusSlaveFactory.createSerialSlave( serialParams);
        Assert.assertNotNull(slaveCreate);
        ModbusSerialSlave slaveGet = ModbusSlaveFactory.getSerialSlave( serialParams);
        Assert.assertNotNull(slaveGet);
        Assert.assertEquals(slaveCreate, slaveGet);
        
        ModbusSlaveFactory.close(slaveGet);
        ModbusSerialSlave slaveGetClose = ModbusSlaveFactory.getSerialSlave( serialParams);
        Assert.assertNull(slaveGetClose);
    }
}
