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
package com.ghgande.j2mod.modbus.utils;

import com.ghgande.j2mod.modbus.procimg.*;
import com.ghgande.j2mod.modbus.procimg.Record;
import com.ghgande.j2mod.modbus.slave.ModbusSlave;
import com.ghgande.j2mod.modbus.util.Observable;
import com.ghgande.j2mod.modbus.util.Observer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * All the master unit tests extend this class so that the system will automatically
 * create a test slave to work with and tear it down after a run
 *
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class AbstractTestModbus {
    private static final Logger logger = LoggerFactory.getLogger(AbstractTestModbus.class);
    public static ModbusSlave slave = null;
    public static final int UNIT_ID = 15;
    public static final int PORT = 2502;
    public static final String LOCALHOST = "127.0.0.1";

    private static Observer observer = new ObserverMonitor();
    protected static Observable updatedRegister;
    protected static String updatedArgument;

    /**
     * Creates a process image to use for slave testing
     *
     * @return Process image for testing
     */
    protected static SimpleProcessImage getSimpleProcessImage() {
        // Create a Slave that we can use to exercise each and every register type

        SimpleProcessImage spi = new SimpleProcessImage(UNIT_ID);

        // Create some coils
        spi.addDigitalOut(new SimpleDigitalOut(true));
        spi.addDigitalOut(new SimpleDigitalOut(false));

        spi.addDigitalOut(50000, new SimpleDigitalOut(false));
        spi.addDigitalOut(50001, new SimpleDigitalOut(true));
        spi.addDigitalOut(50002, new SimpleDigitalOut(false));

        // Now some discretes
        spi.addDigitalIn(new SimpleDigitalIn(false));
        spi.addDigitalIn(new SimpleDigitalIn(true));
        spi.addDigitalIn(new SimpleDigitalIn(false));
        spi.addDigitalIn(new SimpleDigitalIn(true));
        spi.addDigitalIn(new SimpleDigitalIn(true));
        spi.addDigitalIn(new SimpleDigitalIn(true));
        spi.addDigitalIn(new SimpleDigitalIn(true));
        spi.addDigitalIn(new SimpleDigitalIn(true));

        spi.addDigitalIn(65535, new SimpleDigitalIn(true));

        // A couple of files
        spi.addFile(new com.ghgande.j2mod.modbus.procimg.File(0, 10)
                .setRecord(0, new Record(0, 10))
                .setRecord(1, new Record(1, 10))
                .setRecord(2, new Record(2, 10))
                .setRecord(3, new Record(3, 10))
                .setRecord(4, new Record(4, 10))
                .setRecord(5, new Record(5, 10))
                .setRecord(6, new Record(6, 10))
                .setRecord(7, new Record(7, 10))
                .setRecord(8, new Record(8, 10))
                .setRecord(9, new Record(9, 10)));
        spi.addFile(new com.ghgande.j2mod.modbus.procimg.File(1, 20)
                .setRecord(0, new Record(0, 10))
                .setRecord(1, new Record(1, 20))
                .setRecord(2, new Record(2, 20))
                .setRecord(3, new Record(3, 20))
                .setRecord(4, new Record(4, 20))
                .setRecord(5, new Record(5, 20))
                .setRecord(6, new Record(6, 20))
                .setRecord(7, new Record(7, 20))
                .setRecord(8, new Record(8, 20))
                .setRecord(9, new Record(9, 20))
                .setRecord(10, new Record(10, 10))
                .setRecord(11, new Record(11, 20))
                .setRecord(12, new Record(12, 20))
                .setRecord(13, new Record(13, 20))
                .setRecord(14, new Record(14, 20))
                .setRecord(15, new Record(15, 20))
                .setRecord(16, new Record(16, 20))
                .setRecord(17, new Record(17, 20))
                .setRecord(18, new Record(18, 20))
                .setRecord(19, new Record(19, 20)));

        // Some input registers
        ObservableRegister or = new ObservableRegister();
        or.setValue(251);
        or.addObserver(observer);
        spi.addRegister(or);

        or = new ObservableRegister();
        or.setValue(1111);
        or.addObserver(observer);
        spi.addRegister(or);

        or = new ObservableRegister();
        or.setValue(2222);
        or.addObserver(observer);
        spi.addRegister(or);

        or = new ObservableRegister();
        or.setValue(3333);
        or.addObserver(observer);
        spi.addRegister(or);  

        or = new ObservableRegister();
        or.setValue(4444);
        or.addObserver(observer);
        spi.addRegister(or);

        or = new ObservableRegister();
        or.setValue(1234);
        or.addObserver(observer);
        spi.addRegister(40000,or);

        or = new ObservableRegister();          
        or.setValue(2345);
        or.addObserver(observer);
        spi.addRegister(40001,or);

        or = new ObservableRegister();        
        or.setValue(3456);
        or.addObserver(observer);
        spi.addRegister(40002,or);
       
        // Some holding registers
        spi.addInputRegister(new SimpleInputRegister(45));
        spi.addInputRegister(new SimpleInputRegister(9999));
        spi.addInputRegister(new SimpleInputRegister(8888));
        spi.addInputRegister(new SimpleInputRegister(7777));
        spi.addInputRegister(new SimpleInputRegister(6666));
        
        return spi;
    }
    
    
    /**
     * Returns true if the OS is windows
     *
     * @return True if Windows OS
     */
    boolean isWindows() {
      return System.getProperty("os.name").startsWith("Windows");
    }

    private static class ObserverMonitor implements Observer {
        @Override
        public void update(Observable o, Object arg) {
            updatedRegister = o;
            updatedArgument = (String)arg;
        }
    }
}
