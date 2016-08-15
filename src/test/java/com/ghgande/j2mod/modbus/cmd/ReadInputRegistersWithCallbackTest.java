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
import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.io.*;
import com.ghgande.j2mod.modbus.msg.*;
import com.ghgande.j2mod.modbus.net.ModbusMasterFactory;
import com.ghgande.j2mod.modbus.net.SerialConnectionInterface;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.wiringpi.Gpio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Class that implements a simple command line tool for writing to an analog
 * output over a Modbus/TCP connection.
 *
 * <p>
 * Note that if you write to a remote I/O with a Modbus protocol stack, it will
 * most likely expect that the communication is <i>kept alive</i> after the
 * first write message.
 *
 * <p>
 * This can be achieved either by sending any kind of message, or by repeating
 * the write message within a given period of time.
 *
 * <p>
 * If the time period is exceeded, then the device might react by turning off
 * all signals of the I/O modules. After this timeout, the device might require
 * a reset message.
 *
 * @author Dieter Wimberger
 * @author Julie Haugh
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public class ReadInputRegistersWithCallbackTest {

    private static final Logger logger = LoggerFactory.getLogger(ReadInputRegistersWithCallbackTest.class);
    public static final int RTS_PIN = 0;

    private static void printUsage() {
        System.out.printf("\nUsage:\n    java com.ghgande.j2mod.modbus.cmd.ReadInputRegistersWithCallbackTest <address{:port{:unit}} [String]> <base [int]> <count [int]> {<repeat [int]>}");
    }

    public static void main(String[] args) {
        AbstractModbusTransport transport = null;
        ModbusRequest req;
        ModbusTransaction trans;
        int ref = 0;
        int count = 0;
        int repeat = 1;
        int unit = 0;

        GpioFactory.getInstance();
        Gpio.pinMode(RTS_PIN, Gpio.OUTPUT);

        SerialParameters params = new SerialParameters();
        params.setPortName("/dev/ttyAMA0");
        params.setBaudRate(115200);
        params.setDatabits(8);
        params.setParity("none");
        params.setStopbits(1);
        params.setEncoding(Modbus.SERIAL_ENCODING_RTU);
        params.setEcho(false);
        ModbusSerialMaster master = null;
        try {
            master = new ModbusSerialMaster(params, 500);
            master.connect();
            ((ModbusSerialTransport)master.getTransport()).addListener(new EventListener());
            for (int i = 0; i < 5; i++) {
                Integer value = master.readInputRegisters(1, 1002, 1)[0].getValue();
                System.out.printf("Data: %3.1f\n", (value / 5.0) - 50.0);
            }
        }
        catch (Throwable e) {
            logger.error("Modbus problem connecting to {}", e.getMessage());
        }
        finally {
            if (master != null) {
                master.disconnect();
            }
        }








        // 1. Setup parameters
        if (args.length < 3) {
            printUsage();
            System.exit(1);
        }

        try {
            try {
                // 2. Open the connection.
                transport = ModbusMasterFactory.createModbusMaster(args[0]);

                if (transport == null) {
                    System.out.printf("Cannot open %s", args[0]);
                    System.exit(1);
                }

                if (transport instanceof ModbusSerialTransport) {
                    transport.setTimeout(500);
                    ((ModbusSerialTransport)transport).addListener(new EventListener());
                    if (System.getProperty("com.ghgande.j2mod.modbus.baud") != null) {
                        ((ModbusSerialTransport)transport).setBaudRate(Integer.parseInt(System.getProperty("com.ghgande.j2mod.modbus.baud")));
                    }
                    else {
                        ((ModbusSerialTransport)transport).setBaudRate(115200);
                    }
                }

                // There are a number of devices which won't initialize immediately
                // after being opened.  Take a moment to let them come up.
                Thread.sleep(100);

                ref = Integer.parseInt(args[1]);
                count = Integer.parseInt(args[2]);

                if (args.length == 4) {
                    repeat = Integer.parseInt(args[3]);
                }

                if (transport instanceof ModbusTCPTransport) {
                    String parts[] = args[0].split(" *: *");
                    if (parts.length >= 4) {
                        unit = Integer.parseInt(parts[3]);
                    }
                }
                else if (transport instanceof ModbusRTUTransport) {
                    String parts[] = args[0].split(" *: *");
                    if (parts.length >= 3) {
                        unit = Integer.parseInt(parts[2]);
                    }

                    String baud = System.getProperty("com.ghgande.j2mod.modbus.baud");
                    if (baud != null) {
                        ((ModbusRTUTransport)transport).setBaudRate(Integer.parseInt(baud));
                    }
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
                printUsage();
                System.exit(1);
            }

            // 3. Create the command.
            req = new ReadInputRegistersRequest(ref, count);
            req.setUnitID(unit);

            // 4. Prepare the transaction
            trans = transport.createTransaction();
            trans.setRequest(req);
            req.setHeadless(trans instanceof ModbusSerialTransaction);

            System.out.printf("Request is: %s\n", req.getHexMessage());

            // 5. Execute the transaction repeat times

            for (int i = 0; i < repeat; i++) {
                try {
                    trans.execute();
                }
                catch (ModbusException x) {
                    System.out.println(x.getMessage());
                    continue;
                }
                ModbusResponse res = trans.getResponse();
                if (res == null) {
                    System.out.println("No response to READ HOLDING request");
                }
                else {
                    System.out.printf("%d Response: %s ", i, res.getHexMessage());
                    if (res instanceof ExceptionResponse) {
                        ExceptionResponse exception = (ExceptionResponse)res;
                        System.out.println(exception.toString());
                    }
                    else if (res instanceof ReadInputRegistersResponse) {
                        ReadInputRegistersResponse data = (ReadInputRegistersResponse)res;
                        System.out.printf("Data: %3.1f\n", (data.getRegister(0).toShort() / 5.0) - 50);
                    }
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            // 6. Close the connection
            if (transport != null) {
                transport.close();
            }
        }
        catch (IOException e) {
            // Do nothing.
        }
        System.exit(0);
    }

    private static class EventListener extends AbstractSerialTransportListener {
        @Override
        public void beforeMessageWrite(SerialConnectionInterface port, ModbusMessage msg) {
            Gpio.digitalWrite(RTS_PIN, true);
            try {
                Thread.sleep(30);
            }
            catch (Exception e) {
                logger.debug("nothing to do");
            }
        }

        @Override
        public void afterMessageWrite(SerialConnectionInterface port, ModbusMessage msg) {
            Gpio.digitalWrite(RTS_PIN, false);
        }
    }
}
