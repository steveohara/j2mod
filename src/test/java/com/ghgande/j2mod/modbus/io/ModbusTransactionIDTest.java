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
package com.ghgande.j2mod.modbus.io;

import com.ghgande.j2mod.modbus.Modbus;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.ghgande.j2mod.modbus.Modbus.READ_MULTIPLE_REGISTERS;
import static com.ghgande.j2mod.modbus.msg.ModbusRequest.createModbusRequest;
import static org.junit.Assert.*;

/**
 * Tests that the shared transaction ID counter in {@link ModbusTransaction}
 * produces unique IDs under concurrent access and wraps correctly.
 */
public class ModbusTransactionIDTest {

    @Before
    public void resetTransactionID() {
        ModbusTransaction.transactionID.set(Modbus.DEFAULT_TRANSACTION_ID);
    }

    @Test
    public void testGetTransactionIDReturnsCurrentValue() {
        ModbusTransaction.transactionID.set(42);
        ModbusTCPTransaction transaction = new ModbusTCPTransaction();
        assertEquals(42, transaction.getTransactionID());
    }

    @Test
    public void testSettingRequestWillResetTransactionIDWhenAtMax() {
        ModbusTransaction.transactionID.set(Modbus.MAX_TRANSACTION_ID);
        ModbusTCPTransaction transaction = new ModbusTCPTransaction();
        transaction.setRequest(createModbusRequest(READ_MULTIPLE_REGISTERS));
        assertEquals(Modbus.DEFAULT_TRANSACTION_ID, transaction.getTransactionID());
    }

    @Test
    public void testSettingRequestWillResetTransactionIDWhenAboveMax() {
        ModbusTransaction.transactionID.set(Modbus.MAX_TRANSACTION_ID + 1);
        ModbusTCPTransaction transaction = new ModbusTCPTransaction();
        transaction.setRequest(createModbusRequest(READ_MULTIPLE_REGISTERS));
        assertEquals(Modbus.DEFAULT_TRANSACTION_ID, transaction.getTransactionID());
    }

    @Test
    public void testSettingRequestWillResetTransactionIDWhenNegative() {
        ModbusTransaction.transactionID.set(-1);
        ModbusTCPTransaction transaction = new ModbusTCPTransaction();
        transaction.setRequest(createModbusRequest(READ_MULTIPLE_REGISTERS));
        assertEquals(Modbus.DEFAULT_TRANSACTION_ID, transaction.getTransactionID());
    }

    /**
     * Verifies that concurrent threads calling getAndUpdate on the shared
     * AtomicInteger counter never produce duplicate transaction IDs.
     *
     * Each thread atomically increments the counter and records the value
     * it obtained. With correct atomicity, every value must be unique.
     */
    @Test
    public void testConcurrentIncrementProducesUniqueIDs() throws InterruptedException {
        int threadCount = 16;
        int incrementsPerThread = 1000;
        int totalIncrements = threadCount * incrementsPerThread;

        Set<Integer> observedIDs = ConcurrentHashMap.newKeySet();
        AtomicBoolean duplicateFound = new AtomicBoolean(false);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int t = 0; t < threadCount; t++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < incrementsPerThread; i++) {
                        int id = new DemoTransaction().incrementTransactionID();
                        if (!observedIDs.add(id)) {
                            duplicateFound.set(true);
                        }
                    }
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        // Release all threads at once
        startLatch.countDown();
        doneLatch.await();

        assertFalse("Duplicate transaction IDs were produced under concurrent access", duplicateFound.get());
        assertEquals("Expected exactly " + totalIncrements + " unique IDs", totalIncrements, observedIDs.size());
    }

    /**
     * Verifies that the counter wraps around correctly when it reaches
     * MAX_TRANSACTION_ID under concurrent access.
     */
    @Test
    public void testConcurrentIncrementWrapsCorrectly() throws InterruptedException {
        // Set counter close to the wrap point
        ModbusTransaction.transactionID.set(Modbus.MAX_TRANSACTION_ID - 10);

        int threadCount = 8;
        int incrementsPerThread = 100;

        Set<Integer> observedIDs = ConcurrentHashMap.newKeySet();
        AtomicBoolean duplicateFound = new AtomicBoolean(false);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int t = 0; t < threadCount; t++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < incrementsPerThread; i++) {
                        int id = new DemoTransaction().incrementTransactionID();
                        if (!observedIDs.add(id)) {
                            duplicateFound.set(true);
                        }
                    }
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        doneLatch.await();

        assertFalse("Duplicate transaction IDs were produced during wrap-around", duplicateFound.get());

        // All observed IDs must be in valid range
        for (int id : observedIDs) {
            assertTrue("Transaction ID out of range: " + id,
                    id >= Modbus.DEFAULT_TRANSACTION_ID && id <= Modbus.MAX_TRANSACTION_ID);
        }
    }

    @Test
    public void testConcurrentSetRequestAcrossTransactionsAssignsUniqueTransactionIDs() throws InterruptedException {
        int threadCount = 2;

        Set<Integer> observedIDs = ConcurrentHashMap.newKeySet();
        AtomicBoolean duplicateFound = new AtomicBoolean(false);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int t = 0; t < threadCount; t++) {
            new Thread(() -> {
                try {
                    startLatch.await();

                    ModbusTCPTransaction transaction = new ModbusTCPTransaction();
                    transaction.setRequest(createModbusRequest(READ_MULTIPLE_REGISTERS));

                    int assignedID = transaction.getRequest().getTransactionID();
                    if (!observedIDs.add(assignedID)) {
                        duplicateFound.set(true);
                    }
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        doneLatch.await();

        assertFalse("Duplicate transaction IDs were assigned by setRequest under concurrent access", duplicateFound.get());
        assertEquals("Expected one unique transaction ID per transaction", threadCount, observedIDs.size());
    }

    private static class DemoTransaction extends ModbusTransaction {

        @Override
        public void execute() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int incrementTransactionID() {
            return isCheckingValidity() ? transactionID.updateAndGet(current -> {
                if (current >= Modbus.MAX_TRANSACTION_ID) {
                    return Modbus.DEFAULT_TRANSACTION_ID;
                }
                return current + 1;
            }) : getTransactionID();
        }
    }
}
