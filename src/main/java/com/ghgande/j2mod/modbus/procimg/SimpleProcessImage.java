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

import java.util.Vector;

/**
 * Class implementing a simple process image to be able to run unit tests or
 * handle simple cases.
 *
 * <p>
 * The image has a simple linear address space for, analog, digital and file
 * objects. Holes may be created by adding a object with a reference after the
 * last object reference of that type.
 *
 * @author Dieter Wimberger
 * @author Julie Added support for files of records.
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public class SimpleProcessImage implements ProcessImageImplementation {

    // instance attributes
    protected final Vector<DigitalIn> digitalIns = new Vector<DigitalIn>();
    protected final Vector<DigitalOut> digitalOuts = new Vector<DigitalOut>();
    protected final Vector<InputRegister> inputRegisters = new Vector<InputRegister>();
    protected final Vector<Register> registers = new Vector<Register>();
    protected final Vector<File> files = new Vector<File>();
    protected final Vector<FIFO> fifos = new Vector<FIFO>();
    protected boolean locked = false;
    protected int unitID = 0;

    /**
     * Constructs a new <tt>SimpleProcessImage</tt> instance.
     */
    public SimpleProcessImage() {
    }

    /**
     * Constructs a new <tt>SimpleProcessImage</tt> instance having a
     * (potentially) non-zero unit ID.
     * @param unit
     */
    public SimpleProcessImage(int unit) {
        unitID = unit;
    }

    /**
     * The process image is locked to prevent changes.
     *
     * @return whether or not the process image is locked.
     */
    public synchronized boolean isLocked() {
        return locked;
    }

    /**
     * setLocked -- lock or unlock the process image. It is an error (false
     * return value) to attempt to lock the process image when it is already
     * locked.
     *
     * <p>
     * Compatability Note: jamod did not enforce this restriction, so it is
     * being handled in a way which is backwards compatible. If you wish to
     * determine if you acquired the lock, check the return value. If your code
     * is still based on the jamod paradigm, you will ignore the return value
     * and your code will function as before.
     * </p>
     * @param locked
     * @return setting lock succeded
     */
    public synchronized boolean setLocked(boolean locked) {
        if (this.locked && locked) {
            return false;
        }

        this.locked = locked;
        return true;
    }

    public int getUnitID() {
        return unitID;
    }

    public DigitalOut[] getDigitalOutRange(int ref, int count) {
        // ensure valid reference range
        if (ref < 0 || ref + count > digitalOuts.size()) {
            throw new IllegalAddressException();
        }
        else {
            DigitalOut[] douts = new DigitalOut[count];
            for (int i = 0; i < douts.length; i++) {
                douts[i] = getDigitalOut(ref + i);
            }
            return douts;
        }
    }

    public DigitalOut getDigitalOut(int ref) throws IllegalAddressException {
        try {
            DigitalOut result = digitalOuts.elementAt(ref);
            if (result == null) {
                throw new IllegalAddressException();
            }
            return result;
        }
        catch (IndexOutOfBoundsException ex) {
            throw new IllegalAddressException();
        }
    }

    public int getDigitalOutCount() {
        return digitalOuts.size();
    }

    public DigitalIn[] getDigitalInRange(int ref, int count) {
        // ensure valid reference range
        if (ref < 0 || ref + count > digitalIns.size()) {
            throw new IllegalAddressException();
        }
        else {
            DigitalIn[] dins = new DigitalIn[count];
            for (int i = 0; i < dins.length; i++) {
                dins[i] = getDigitalIn(ref + i);
            }
            return dins;
        }
    }

    public DigitalIn getDigitalIn(int ref) throws IllegalAddressException {
        try {
            DigitalIn result = digitalIns.elementAt(ref);
            if (result == null) {
                throw new IllegalAddressException();
            }
            return result;
        }
        catch (IndexOutOfBoundsException ex) {
            throw new IllegalAddressException();
        }
    }

    public int getDigitalInCount() {
        return digitalIns.size();
    }

    public InputRegister[] getInputRegisterRange(int ref, int count) {
        // ensure valid reference range
        if (ref < 0 || ref + count > inputRegisters.size()) {
            throw new IllegalAddressException();
        }

        InputRegister[] iregs = new InputRegister[count];
        for (int i = 0; i < iregs.length; i++) {
            iregs[i] = getInputRegister(ref + i);
        }

        return iregs;
    }

    public InputRegister getInputRegister(int ref) throws IllegalAddressException {
        try {
            InputRegister result = inputRegisters.elementAt(ref);
            if (result == null) {
                throw new IllegalAddressException();
            }

            return result;
        }
        catch (IndexOutOfBoundsException ex) {
            throw new IllegalAddressException();
        }
    }

    public int getInputRegisterCount() {
        return inputRegisters.size();
    }

    public Register[] getRegisterRange(int ref, int count) {
        if (ref < 0 || ref + count > registers.size()) {
            throw new IllegalAddressException();
        }
        else {
            Register[] iregs = new Register[count];
            for (int i = 0; i < iregs.length; i++) {
                iregs[i] = getRegister(ref + i);
            }
            return iregs;
        }
    }

    public Register getRegister(int ref) throws IllegalAddressException {
        try {
            Register result = registers.elementAt(ref);
            if (result == null) {
                throw new IllegalAddressException();
            }

            return result;
        }
        catch (IndexOutOfBoundsException ex) {
            throw new IllegalAddressException();
        }
    }

    public int getRegisterCount() {
        return registers.size();
    }

    public File getFile(int fileNumber) {
        try {
            File result = files.elementAt(fileNumber);
            if (result == null) {
                throw new IllegalAddressException();
            }

            return result;
        }
        catch (IndexOutOfBoundsException ex) {
            throw new IllegalAddressException();
        }
    }

    public File getFileByNumber(int ref) {
        if (ref < 0 || ref >= 10000 || files == null) {
            throw new IllegalAddressException();
        }

        synchronized (files) {
            for (File file : files) {
                if (file.getFileNumber() == ref) {
                    return file;
                }
            }
        }

        throw new IllegalAddressException();
    }

    public int getFileCount() {
        return files.size();
    }

    public FIFO getFIFO(int fifoNumber) {
        try {
            FIFO result = fifos.elementAt(fifoNumber);
            if (result == null) {
                throw new IllegalAddressException();
            }

            return result;
        }
        catch (IndexOutOfBoundsException ex) {
            throw new IllegalAddressException();
        }
    }

    public FIFO getFIFOByAddress(int ref) {
        for (FIFO fifo : fifos) {
            if (fifo.getAddress() == ref) {
                return fifo;
            }
        }

        return null;
    }

    public int getFIFOCount() {
        if (fifos == null) {
            return 0;
        }

        return fifos.size();
    }

    public void setDigitalOut(int ref, DigitalOut _do) throws IllegalAddressException {
        if (!isLocked()) {
            try {
                if (digitalOuts.get(ref) == null) {
                    throw new IllegalAddressException();
                }
                digitalOuts.setElementAt(_do, ref);
            }
            catch (IndexOutOfBoundsException ex) {
                throw new IllegalAddressException();
            }
        }
    }

    public void addDigitalOut(DigitalOut _do) {
        if (!isLocked()) {
            digitalOuts.addElement(_do);
        }
    }

    public void addDigitalOut(int ref, DigitalOut dout) {
        if (ref < 0 || ref >= 65536) {
            throw new IllegalArgumentException();
        }

        if (!isLocked()) {
            synchronized (digitalOuts) {
                if (ref < digitalOuts.size()) {
                    digitalOuts.setElementAt(dout, ref);
                    return;
                }
                digitalOuts.setSize(ref + 1);
                digitalOuts.setElementAt(dout, ref);
            }
        }
    }

    public void removeDigitalOut(DigitalOut _do) {
        if (!isLocked()) {
            digitalOuts.removeElement(_do);
        }
    }

    public void setDigitalIn(int ref, DigitalIn di) throws IllegalAddressException {
        if (!isLocked()) {
            try {
                if (digitalIns.get(ref) == null) {
                    throw new IllegalAddressException();
                }
                digitalIns.setElementAt(di, ref);
            }
            catch (IndexOutOfBoundsException ex) {
                throw new IllegalAddressException();
            }
        }
    }

    public void addDigitalIn(DigitalIn di) {
        if (!isLocked()) {
            digitalIns.addElement(di);
        }
    }

    public void addDigitalIn(int ref, DigitalIn d1) {
        if (ref < 0 || ref >= 65536) {
            throw new IllegalArgumentException();
        }

        if (!isLocked()) {
            synchronized (digitalIns) {
                if (ref < digitalIns.size()) {
                    digitalIns.setElementAt(d1, ref);
                    return;
                }
                digitalIns.setSize(ref + 1);
                digitalIns.setElementAt(d1, ref);
            }
        }
    }

    public void removeDigitalIn(DigitalIn di) {
        if (!isLocked()) {
            digitalIns.removeElement(di);
        }
    }

    public void setInputRegister(int ref, InputRegister reg) throws IllegalAddressException {
        if (!isLocked()) {
            try {
                if (inputRegisters.get(ref) == null) {
                    throw new IllegalAddressException();
                }

                inputRegisters.setElementAt(reg, ref);
            }
            catch (IndexOutOfBoundsException ex) {
                throw new IllegalAddressException();
            }
        }
    }

    public void addInputRegister(InputRegister reg) {
        if (!isLocked()) {
            inputRegisters.addElement(reg);
        }
    }

    public void addInputRegister(int ref, InputRegister inReg) {
        if (ref < 0 || ref >= 65536) {
            throw new IllegalArgumentException();
        }

        if (!isLocked()) {
            synchronized (inputRegisters) {
                if (ref < inputRegisters.size()) {
                    inputRegisters.setElementAt(inReg, ref);
                    return;
                }
                inputRegisters.setSize(ref + 1);
                inputRegisters.setElementAt(inReg, ref);
            }
        }
    }

    public void removeInputRegister(InputRegister reg) {
        if (!isLocked()) {
            inputRegisters.removeElement(reg);
        }
    }

    public void setRegister(int ref, Register reg) throws IllegalAddressException {
        if (!isLocked()) {
            try {
                if (registers.get(ref) == null) {
                    throw new IllegalAddressException();
                }

                registers.setElementAt(reg, ref);
            }
            catch (IndexOutOfBoundsException ex) {
                throw new IllegalAddressException();
            }
        }
    }

    public void addRegister(Register reg) {
        if (!isLocked()) {
            registers.addElement(reg);
        }
    }

    public void addRegister(int ref, Register reg) {
        if (ref < 0 || ref >= 65536) {
            throw new IllegalArgumentException();
        }

        if (!isLocked()) {
            synchronized (registers) {
                if (ref < registers.size()) {
                    registers.setElementAt(reg, ref);
                    return;
                }
                registers.setSize(ref + 1);
                registers.setElementAt(reg, ref);
            }
        }
    }

    public void removeRegister(Register reg) {
        if (!isLocked()) {
            registers.removeElement(reg);
        }
    }

    public void setFile(int fileNumber, File file) {
        if (!isLocked()) {
            try {
                if (files.get(fileNumber) == null) {
                    throw new IllegalAddressException();
                }

                files.setElementAt(file, fileNumber);
            }
            catch (IndexOutOfBoundsException ex) {
                throw new IllegalAddressException();
            }
        }
    }

    public void addFile(File newFile) {
        if (!isLocked()) {
            files.add(newFile);
        }
    }

    public void addFile(int ref, File newFile) {
        if (ref < 0 || ref >= 65536) {
            throw new IllegalArgumentException();
        }

        if (!isLocked()) {
            synchronized (files) {
                if (ref < files.size()) {
                    files.setElementAt(newFile, ref);
                    return;
                }
                files.setSize(ref + 1);
                files.setElementAt(newFile, ref);
            }
        }
    }

    public void removeFile(File oldFile) {
        if (!isLocked()) {
            files.removeElement(oldFile);
        }
    }

    public void setFIFO(int fifoNumber, FIFO fifo) {
        if (!isLocked()) {
            try {
                if (fifos.get(fifoNumber) == null) {
                    throw new IllegalAddressException();
                }

                fifos.setElementAt(fifo, fifoNumber);
            }
            catch (IndexOutOfBoundsException ex) {
                throw new IllegalAddressException();
            }
        }
    }

    public void addFIFO(FIFO fifo) {
        if (!isLocked()) {
            fifos.add(fifo);
        }
    }

    public void addFIFO(int ref, FIFO newFIFO) {
        if (ref < 0 || ref >= 65536) {
            throw new IllegalArgumentException();
        }

        if (!isLocked()) {
            synchronized (fifos) {
                if (ref < fifos.size()) {
                    fifos.setElementAt(newFIFO, ref);
                    return;
                }
                fifos.setSize(ref + 1);
                fifos.setElementAt(newFIFO, ref);
            }
        }
    }

    public void removeFIFO(FIFO oldFIFO) {
        if (!isLocked()) {
            fifos.removeElement(oldFIFO);
        }
    }

}
