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
package com.ghgande.j2mod.modbus.cmd;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.ModbusCoupler;
import com.ghgande.j2mod.modbus.net.ModbusTCPListener;
import com.ghgande.j2mod.modbus.procimg.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Inet4Address;

/**
 * Class implementing a simple Modbus/TCP slave. A simple process image is
 * available to test functionality and behaviour of the implementation.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public class TCPSlaveTest {

    private static final Logger logger = LoggerFactory.getLogger(TCPSlaveTest.class);

    public static void main(String[] args) {

        ModbusTCPListener listener;
        SimpleProcessImage spi;
        int port = Modbus.DEFAULT_PORT;
        int unit = 0;

        try {
            if (args != null && args.length == 1) {
                port = Integer.parseInt(args[0]);
            }
            System.out.printf("j2mod Modbus Slave (Server) v0.97");

            // Create the process image for this test.
            spi = new SimpleProcessImage(15);

            spi.addDigitalOut(new SimpleDigitalOut(true));
            spi.addDigitalOut(new SimpleDigitalOut(true));

            spi.addDigitalIn(new SimpleDigitalIn(false));
            spi.addDigitalIn(new SimpleDigitalIn(true));
            spi.addDigitalIn(new SimpleDigitalIn(false));
            spi.addDigitalIn(new SimpleDigitalIn(true));

            spi.addFile(new File(0, 10).setRecord(0, new Record(0, 10))
                    .setRecord(1, new Record(1, 10))
                    .setRecord(2, new Record(2, 10))
                    .setRecord(3, new Record(3, 10))
                    .setRecord(4, new Record(4, 10))
                    .setRecord(5, new Record(5, 10))
                    .setRecord(6, new Record(6, 10))
                    .setRecord(7, new Record(7, 10))
                    .setRecord(8, new Record(8, 10))
                    .setRecord(9, new Record(9, 10)));

            spi.addFile(new File(1, 20)
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
                    .setRecord(19, new Record(19, 20))
            );

            // allow checking LSB/MSB order
            spi.addDigitalIn(new SimpleDigitalIn(true));
            spi.addDigitalIn(new SimpleDigitalIn(true));
            spi.addDigitalIn(new SimpleDigitalIn(true));
            spi.addDigitalIn(new SimpleDigitalIn(true));

            spi.addRegister(new SimpleRegister(251));
            spi.addInputRegister(new SimpleInputRegister(45));

            // 2. create the coupler holding the image
            ModbusCoupler.getReference().setProcessImage(spi);
            ModbusCoupler.getReference().setMaster(false);

            // 3. create a listener with 3 threads in pool
            listener = new ModbusTCPListener(3, Inet4Address.getByName("0.0.0.0"), false);
            listener.setPort(port);
            new Thread(listener).start();
            System.out.printf("Listening..");

            // Check to see if it started OK

            Thread.sleep(100);
            if (!listener.isListening()) {
                throw new IOException(String.format("Cannot start Listener %s", listener.getError()));
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}// class TCPSlaveTest
