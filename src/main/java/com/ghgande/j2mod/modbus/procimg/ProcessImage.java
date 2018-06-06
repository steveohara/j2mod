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
package com.ghgande.j2mod.modbus.procimg;

/**
 * Interface defining a process image in an object oriented manner.
 * <p>
 * The process image is understood as a shared memory area used form
 * communication between slave and master or device side.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 */
public interface ProcessImage {

    /**
     * Returns a range of <tt>DigitalOut</tt> instances.
     *
     * @param offset the start offset.
     * @param count  the amount of <tt>DigitalOut</tt> from the offset.
     *
     * @return an array of <tt>DigitalOut</tt> instances.
     *
     * @throws IllegalAddressException if the range from offset to offset+count is non existant.
     */
    DigitalOut[] getDigitalOutRange(int offset, int count) throws IllegalAddressException;

    /**
     * Returns the <tt>DigitalOut</tt> instance at the given reference.
     *
     * @param ref the reference.
     *
     * @return the <tt>DigitalOut</tt> instance at the given address.
     *
     * @throws IllegalAddressException if the reference is invalid.
     */
    DigitalOut getDigitalOut(int ref) throws IllegalAddressException;

    /**
     * Returns the number of <tt>DigitalOut</tt> instances in this
     * <tt>ProcessImage</tt>.
     *
     * @return the number of digital outs as <tt>int</tt>.
     */
    int getDigitalOutCount();

    /**
     * Returns a range of <tt>DigitalIn</tt> instances.
     *
     * @param offset the start offset.
     * @param count  the amount of <tt>DigitalIn</tt> from the offset.
     *
     * @return an array of <tt>DigitalIn</tt> instances.
     *
     * @throws IllegalAddressException if the range from offset to offset+count is non existant.
     */
    DigitalIn[] getDigitalInRange(int offset, int count) throws IllegalAddressException;

    /**
     * Returns the <tt>DigitalIn</tt> instance at the given reference.
     *
     * @param ref the reference.
     *
     * @return the <tt>DigitalIn</tt> instance at the given address.
     *
     * @throws IllegalAddressException if the reference is invalid.
     */
    DigitalIn getDigitalIn(int ref) throws IllegalAddressException;

    /**
     * Returns the number of <tt>DigitalIn</tt> instances in this
     * <tt>ProcessImage</tt>.
     *
     * @return the number of digital ins as <tt>int</tt>.
     */
    int getDigitalInCount();

    /**
     * Returns a range of <tt>InputRegister</tt> instances.
     *
     * @param offset the start offset.
     * @param count  the amount of <tt>InputRegister</tt> from the offset.
     *
     * @return an array of <tt>InputRegister</tt> instances.
     *
     * @throws IllegalAddressException if the range from offset to offset+count is non existant.
     */
    InputRegister[] getInputRegisterRange(int offset, int count) throws IllegalAddressException;

    /**
     * Returns the <tt>InputRegister</tt> instance at the given reference.
     *
     * @param ref the reference.
     *
     * @return the <tt>InputRegister</tt> instance at the given address.
     *
     * @throws IllegalAddressException if the reference is invalid.
     */
    InputRegister getInputRegister(int ref) throws IllegalAddressException;

    /**
     * Returns the number of <tt>InputRegister</tt> instances in this
     * <tt>ProcessImage</tt>.
     *
     * <p>
     * This is not the same as the value of the highest addressable register.
     *
     * @return the number of input registers as <tt>int</tt>.
     */
    int getInputRegisterCount();

    /**
     * Returns a range of <tt>Register</tt> instances.
     *
     * @param offset the start offset.
     * @param count  the amount of <tt>Register</tt> from the offset.
     *
     * @return an array of <tt>Register</tt> instances.
     *
     * @throws IllegalAddressException if the range from offset to offset+count is non existant.
     */
    Register[] getRegisterRange(int offset, int count) throws IllegalAddressException;

    /**
     * Returns the <tt>Register</tt> instance at the given reference.
     * <p>
     *
     * @param ref the reference.
     *
     * @return the <tt>Register</tt> instance at the given address.
     *
     * @throws IllegalAddressException if the reference is invalid.
     */
    Register getRegister(int ref) throws IllegalAddressException;

    /**
     * Returns the number of <tt>Register</tt> instances in this
     * <tt>ProcessImage</tt>.
     *
     * <p>
     * This is not the same as the value of the highest addressable register.
     *
     * @return the number of registers as <tt>int</tt>.
     */
    int getRegisterCount();

    /**
     * Returns the <tt>File</tt> instance at the given reference.
     * <p>
     *
     * @param ref the reference.
     *
     * @return the <tt>File</tt> instance at the given address.
     *
     * @throws IllegalAddressException if the reference is invalid.
     */
    File getFile(int ref) throws IllegalAddressException;

    /**
     * Returns the <tt>File</tt> instance having the specified file number.
     *
     * @param ref The file number for the File object to be returned.
     *
     * @return the <tt>File</tt> instance having the given number.
     *
     * @throws IllegalAddressException if a File with the given number does not exist.
     */
    File getFileByNumber(int ref) throws IllegalAddressException;

    /**
     * Returns the number of <tt>File</tt> instances in this
     * <tt>ProcessImage</tt>.
     *
     * <p>
     * This is not the same as the value of the highest addressable register.
     *
     * @return the number of registers as <tt>int</tt>.
     */
    int getFileCount();

    /**
     * Returns the <tt>FIFO</tt> instance in the list of all FIFO objects
     * in this ProcessImage.
     *
     * @param ref the reference.
     *
     * @return the <tt>File</tt> instance at the given address.
     *
     * @throws IllegalAddressException if the reference is invalid.
     */
    FIFO getFIFO(int ref) throws IllegalAddressException;

    /**
     * Returns the <tt>FIFO</tt> instance having the specified base address.
     *
     * @param ref The address for the FIFO object to be returned.
     *
     * @return the <tt>FIFO</tt> instance having the given base address
     *
     * @throws IllegalAddressException if a File with the given number does not exist.
     */
    FIFO getFIFOByAddress(int ref) throws IllegalAddressException;

    /**
     * Returns the number of <tt>File</tt> instances in this
     * <tt>ProcessImage</tt>.
     *
     * <p>
     * This is not the same as the value of the highest addressable register.
     *
     * @return the number of registers as <tt>int</tt>.
     */
    int getFIFOCount();
}
