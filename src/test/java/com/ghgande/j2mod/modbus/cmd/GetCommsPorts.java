/*
 *
 * Copyright (c) 2018, 4ng and/or its affiliates. All rights reserved.
 * 4ENERGY PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */
package com.ghgande.j2mod.modbus.cmd;

import com.ghgande.j2mod.modbus.net.SerialConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class GetCommsPorts {

    private static final Logger logger = LoggerFactory.getLogger(GetCommsPorts.class);

    public static void main(String[] args) {

        for (String commPort : new SerialConnection().getCommPorts()) {
            logger.info(commPort);
        }
    }
}
