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
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.io.AbstractModbusTransport;
import com.ghgande.j2mod.modbus.io.AbstractSerialTransportListener;
import com.ghgande.j2mod.modbus.io.ModbusSerialTransport;
import com.ghgande.j2mod.modbus.io.ModbusTransaction;
import com.ghgande.j2mod.modbus.msg.ModbusMessage;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.net.AbstractSerialConnection;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.wiringpi.Gpio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that implements a simple command line tool for writing to an analog
 * output over a Modbus/TCP connection.
 * <p>
 * <p>
 * Note that if you write to a remote I/O with a Modbus protocol stack, it will
 * most likely expect that the communication is <i>kept alive</i> after the
 * first write message.
 * <p>
 * <p>
 * This can be achieved either by sending any kind of message, or by repeating
 * the write message within a given period of time.
 * <p>
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
        params.setBaudRate(9600);
        params.setDatabits(8);
        params.setParity("none");
        params.setStopbits(1);
        params.setEncoding(Modbus.SERIAL_ENCODING_RTU);
        params.setEcho(false);
        ModbusSerialMaster master = null;
        try {
            master = new ModbusSerialMaster(params, 500);
            master.connect();
            ((ModbusSerialTransport) master.getTransport()).addListener(new EventListener());
            for (int i = 0; i < 100; i++) {
                try {
                    Integer value = master.readInputRegisters(49, 1, 1)[0].getValue();
                    //                System.out.printf("Data: %3.1f\n", (value / 5.0) - 50.0);
                    System.out.printf("Data: %3.1f\n", value / 10.0);
                }
                catch (Exception e) {

                }
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
        System.exit(0);
    }

    private static class EventListener extends AbstractSerialTransportListener {

        @Override
        public void beforeMessageWrite(AbstractSerialConnection port, ModbusMessage msg) {
            Gpio.digitalWrite(RTS_PIN, true);
            try {
//                Thread.sleep(30);
            }
            catch (Exception e) {
                logger.debug("nothing to do");
            }
        }

        @Override
        public void afterMessageWrite(AbstractSerialConnection port, ModbusMessage msg) {
            Gpio.digitalWrite(RTS_PIN, false);
        }
    }
}
