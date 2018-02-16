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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * This class is a replacement implementation for ByteArrayOutputStream
 * that does not synchronize every
 * byte written.
 *
 * @author Mark Hayes
 * @author Dieter Wimberger
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public class FastByteArrayOutputStream extends OutputStream {

    private static final Logger logger = LoggerFactory.getLogger(FastByteArrayOutputStream.class);

    /**
     * Defines the default oputput buffer size (100 bytes).
     */
    private static final int DEFAULT_INIT_SIZE = 100;
    /**
     * Defines the default increment of the output buffer size
     * (100 bytes).
     */
    private static final int DEFAULT_BUMP_SIZE = 100;

    /**
     * Number of bytes in the output buffer.
     */
    protected int count;

    /**
     * Increment of the output buffer size on overflow.
     */
    private int bumpLen;

    /**
     * Output buffer <tt>byte[]</tt>.
     */
    byte[] buf;

    /**
     * Creates an output stream with default sizes.
     */
    FastByteArrayOutputStream() {
        buf = new byte[DEFAULT_INIT_SIZE];
        bumpLen = DEFAULT_BUMP_SIZE;
    }

    /**
     * Creates an output stream with a default bump size and a given initial
     * size.
     *
     * @param initialSize the initial size of the buffer.
     */
    FastByteArrayOutputStream(int initialSize) {
        buf = new byte[initialSize];
        bumpLen = DEFAULT_BUMP_SIZE;
    }

    /**
     * Creates an output stream with a given bump size and initial size.
     *
     * @param initialSize the initial size of the buffer.
     * @param bumpSize    the amount to increment the buffer.
     */
    public FastByteArrayOutputStream(int initialSize, int bumpSize) {
        buf = new byte[initialSize];
        bumpLen = bumpSize;
    }

    // --- begin ByteArrayOutputStream compatible methods ---

    /**
     * Returns the number of bytes written to this
     * <tt>FastByteArrayOutputStream</tt>.
     *
     * @return the number of bytes written as <tt>int</tt>.
     */
    public int size() {
        return count;
    }

    /**
     * Resets this <tt>FastByteArrayOutputStream</tt>.
     */
    public void reset() {
        count = 0;
    }

    public void write(int b) throws IOException {
        if (count + 1 > buf.length) {
            bump(1);
        }
        buf[count++] = (byte)b;
    }

    public void write(byte[] fromBuf) throws IOException {
        int needed = count + fromBuf.length - buf.length;
        if (needed > 0) {
            bump(needed);
        }
        for (byte aFromBuf : fromBuf) {
            buf[count++] = aFromBuf;
        }
    }

    public void write(byte[] fromBuf, int offset, int length) throws IOException {

        int needed = count + length - buf.length;
        if (needed > 0) {
            bump(needed);
        }
        int fromLen = offset + length;

        for (int i = offset; i < fromLen; i++) {
            buf[count++] = fromBuf[i];
        }
    }

    /**
     * Writes the content of this <tt>FastByteArrayOutputStream</tt>
     * to the given output stream.
     *
     * @param out the output stream to be written to.
     *
     * @throws IOException if an I/O error occurs.
     */
    public synchronized void writeTo(OutputStream out) throws IOException {
        out.write(buf, 0, count);
    }

    public String toString() {
        try {
            return new String(buf, 0, count, "US-ASCII");
        }
        catch (Exception e) {
            logger.debug("Problem converting bytes to string - {}", e.getMessage());
        }
        return "";
    }

    /**
     * Returns the content of this <tt>FastByteArrayOutputStream</tt>
     * as String.
     *
     * @param encoding the encoding to be used for conversion.
     *
     * @return a newly allocated String.
     *
     * @throws UnsupportedEncodingException if the given encoding is not supported.
     */
    public String toString(String encoding) throws UnsupportedEncodingException {
        return new String(buf, 0, count, encoding);
    }

    /**
     * Returns the written bytes in a newly allocated byte[]
     * of length getSize().
     *
     * @return a newly allocated byte[] with the content of the
     * output buffer.
     */
    public byte[] toByteArray() {
        byte[] toBuf = new byte[count];
        System.arraycopy(buf, 0, toBuf, 0, count);
        //for (int i = 0; i < count; i++) {
        //  toBuf[i] = buf[i];
        //}
        return toBuf;
    }

    // --- end ByteArrayOutputStream compatible methods ---

    /**
     * Copy the buffered data to the given array.
     *
     * @param toBuf  the buffer to hold a copy of the data.
     * @param offset the offset at which to start copying.
     */
    public void toByteArray(byte[] toBuf, int offset) {
        int toLen = (toBuf.length > count) ? count : toBuf.length;
        System.arraycopy(buf, offset, toBuf, offset, toLen - offset);
    }

    /**
     * Returns the buffer owned by this object.
     *
     * @return the buffer.
     */
    public synchronized byte[] getBufferBytes() {
        byte[] dest = new byte[count];
        System.arraycopy(buf, 0, dest, 0, dest.length);
        return dest;
    }

    /**
     * Returns the offset of the internal buffer.
     *
     * @return always zero currently.
     */
    public int getBufferOffset() {
        return 0;
    }

    /**
     * Returns the length used in the internal buffer, that is, the offset at
     * which data will be written next.
     *
     * @return the buffer length.
     */
    public int getBufferLength() {
        return count;
    }

    /**
     * Ensure that at least the given number of bytes are available in the
     * internal buffer.
     *
     * @param sizeNeeded the number of bytes desired.
     */
    public void makeSpace(int sizeNeeded) {
        int needed = count + sizeNeeded - buf.length;
        if (needed > 0) {
            bump(needed);
        }
    }

    /**
     * Skip the given number of bytes in the buffer.
     *
     * @param sizeAdded number of bytes to skip.
     */
    public void addSize(int sizeAdded) {
        count += sizeAdded;
    }

    private void bump(int needed) {

        byte[] toBuf = new byte[buf.length + needed + bumpLen];

        System.arraycopy(buf, 0, toBuf, 0, count);
        buf = toBuf;
    }

}