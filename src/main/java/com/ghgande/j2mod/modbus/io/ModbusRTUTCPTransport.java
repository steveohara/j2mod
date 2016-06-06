package com.ghgande.j2mod.modbus.io;

import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.msg.ModbusMessage;
import com.ghgande.j2mod.modbus.util.ModbusUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketException;

/**
 * Created by axuan on 06/06/16.
 */
public class ModbusRTUTCPTransport extends ModbusTCPTransport {

    private static final Logger logger = LoggerFactory.getLogger(ModbusRTUTCPTransport.class);

    public ModbusRTUTCPTransport() {
        // RTU over TCP is headless by default
        setHeadless();
    }

    @Override
    public void writeMessage(ModbusMessage msg) throws ModbusIOException {
        try {
            byte message[] = msg.getMessage();

            byteOutputStream.reset();
            if (!headless) {
                byteOutputStream.writeShort(msg.getTransactionID());
                byteOutputStream.writeShort(msg.getProtocolID());
                byteOutputStream.writeShort((message != null ? message.length : 0) + 2);
            }
            byteOutputStream.writeByte(msg.getUnitID());
            byteOutputStream.writeByte(msg.getFunctionCode());
            if (message != null && message.length > 0) {
                byteOutputStream.write(message);
            }

            // Add CRC for RTU over TCP
            int len = byteOutputStream.size();
            int[] crc = ModbusUtil.calculateCRC(byteOutputStream.getBuffer(), 0, len);
            byteOutputStream.writeByte(crc[0]);
            byteOutputStream.writeByte(crc[1]);

            dataOutputStream.write(byteOutputStream.toByteArray());
            dataOutputStream.flush();
            logger.debug("Sent: {}", ModbusUtil.toHex(byteOutputStream.toByteArray()));
            // write more sophisticated exception handling
        } catch (SocketException ex1) {
            if (master != null && !master.isConnected()) {
                try {
                    master.connect();
                }
                catch (Exception e) {
                    // Do nothing.
                }
            }
            throw new ModbusIOException("I/O exception - failed to write", ex1);
        } catch (Exception ex2) {
            throw new ModbusIOException("I/O exception - failed to write", ex2);
        }
    }
}
