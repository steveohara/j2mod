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

import com.ghgande.j2mod.modbus.net.ModbusSerialListener;
import com.ghgande.j2mod.modbus.procimg.*;
import com.ghgande.j2mod.modbus.slave.ModbusSlave;
import com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class implementing a simple Modbus slave. A simple process image is available
 * to test functionality and behavior of the implementation.
 *
 * @author Dieter Wimberger
 * @author Julie Haugh
 *         Added ability to specify the number of coils, discreates, input and
 *         holding registers.
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public class SerialSlaveTest {

    private static final Logger logger = LoggerFactory.getLogger(SerialSlaveTest.class);

    public static void main(String[] args) {

        ModbusSerialListener listener;
        SimpleProcessImage spi;
        String portname = null;
        boolean hasUnit = false;
        int unit = 2;
        int coils = 2;
        int discretes = 4;
        boolean hasInputs = false;
        int inputs = 1;
        boolean hasHoldings = false;
        int holdings = 1;
        int arg;

        for (arg = 0; arg < args.length; arg++) {
            if (args[arg].equals("--port") || args[arg].equals("-p")) {
                portname = args[++arg];
            }
            else if (args[arg].equals("--unit") || args[arg].equals("-u")) {
                unit = Integer.parseInt(args[++arg]);
                hasUnit = true;
            }
            else if (args[arg].equals("--coils") || args[arg].equals("-c")) {
                coils = Integer.parseInt(args[++arg]);
            }
            else if (args[arg].equals("--discretes") || args[arg].equals("-d")) {
                discretes = Integer.parseInt(args[++arg]);
            }
            else if (args[arg].equals("--inputs") || args[arg].equals("-i")) {
                inputs = Integer.parseInt(args[++arg]);
                hasInputs = true;
            }
            else if (args[arg].equals("--holdings") || args[arg].equals("-h")) {
                holdings = Integer.parseInt(args[++arg]);
                hasHoldings = true;
            }
            else {
                break;
            }
        }

        if (arg < args.length && portname == null) {
            portname = args[arg++];
        }

        if (arg < args.length && !hasUnit) {
            unit = Integer.parseInt(args[arg++]);
        }

        System.out.printf("j2mod ModbusSerial Slave");

        try {

            /*
             * Prepare a process image.
             *
             * The file records from the TCP and UDP test harnesses are
             * not included.  They can be added if there is a need to
             * test READ FILE RECORD and WRITE FILE RECORD with a Modbus/RTU
             * device.
             */
            spi = new SimpleProcessImage(15);

            for (int i = 0; i < coils; i++) {
                spi.addDigitalOut(new SimpleDigitalOut(i % 2 == 0));
            }

            for (int i = 0; i < discretes; i++) {
                spi.addDigitalIn(new SimpleDigitalIn(i % 2 == 0));
            }

            if (hasHoldings) {
                System.out.printf("Adding %d holding registers", holdings);

                for (int i = 0; i < holdings; i++) {
                    spi.addRegister(new SimpleRegister(i));
                }
            }
            else {
                spi.addRegister(new SimpleRegister(251));
            }

            if (hasInputs) {
                System.out.printf("Adding input registers", inputs);

                for (int i = 0; i < inputs; i++) {
                    spi.addInputRegister(new SimpleInputRegister(i));
                }
            }
            else {
                spi.addInputRegister(new SimpleInputRegister(45));
            }

            // 2. Set up serial parameters
            SerialParameters params = new SerialParameters();
            params.setPortName(portname);
            params.setBaudRate(19200);
            params.setDatabits(8);
            params.setParity("None");
            params.setStopbits(1);
            params.setEncoding("rtu");
            params.setEcho(false);
            System.out.printf("Encoding [%s]", params.getEncoding());

            // 3. Setup and start slave
            ModbusSlave slave = ModbusSlaveFactory.createSerialSlave(params);
            slave.addProcessImage(unit, spi);
            slave.open();

        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
