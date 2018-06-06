/*
 *
 * Copyright (c) 2018, 4ng and/or its affiliates. All rights reserved.
 * 4ENERGY PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */
package com.ghgande.j2mod.modbus.cmd;

import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.procimg.Register;

/**
 *
 */
public class MasterReadTest {

    public static void main(String[] args) {
        ModbusTCPMaster master = new ModbusTCPMaster("localhost", 502, 1000, true, true);
        try {
            master.connect();
            for (int i = 0; i < 10; i++) {
                int ref = 100;
                Register[] regs = master.readMultipleRegisters(1, ref, 10);
                for (Register reg : regs) {
                    System.out.printf("Reg: %d Val: %d\n", ref, reg.getValue());
                    ref++;
                }
            }
        }
        catch (Exception e) {
            System.out.printf("ERROR - %s\n", e.getMessage());
        }
        finally {
            master.disconnect();
        }
    }
}
