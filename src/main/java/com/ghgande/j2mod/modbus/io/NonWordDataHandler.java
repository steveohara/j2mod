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

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;

/**
 * Interface implementing a non word data handler for the read/write multiple
 * register commands.
 *
 * This interface can be used by any class which works with multiple words of
 * data for a non-standard data item. For example, message may involve data
 * items which are floating point values or string.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4NG)
 * @version 2.0 (March 2016)
 * @deprecated In the interests of keeping the library simple, this will be removed in a future release
 */
@Deprecated
public interface NonWordDataHandler {

    /**
     * Returns the intermediate raw non-word data.
     *
     * <p>
     * An implementation would need to provide a means of converting between the
     * raw byte data and the registers that are present in actual messages.
     *
     * @return the raw data as <tt>byte[]</tt>.
     */
    byte[] getData();

    /**
     * Reads the non-word raw data based on an arbitrary implemented structure.
     *
     * @param in        the <tt>DataInput</tt> to read from.
     * @param reference to specify the offset as <tt>int</tt>.
     * @param count     to specify the amount of bytes as <tt>int</tt>.
     *
     * @throws IOException  if I/O fails.
     * @throws EOFException if the stream ends before all data is read.
     */
    void readData(DataInput in, int reference, int count) throws IOException, EOFException;

    /**
     * Returns the word count of the data. Note that this should be the length
     * of the byte array divided by two.
     *
     * @return the number of words the data consists of.
     */
    int getWordCount();

    /**
     * Commits the data if it has been read into an intermediate repository.
     *
     * <p>
     * This method is called for a message (for example, a
     * <tt>WriteMultipleRegistersRequest</tt> instance) when finished with
     * reading, for creating a response.
     *
     * @return -1 if the commit was successful, a Modbus exception code valid
     * for the read/write multiple registers commands otherwise.
     */
    int commitUpdate();

    /**
     * Prepares the raw data, putting it together from a backing data store.
     *
     * <p>
     * This method is called for a message (for example, * <tt>ReadMultipleRegistersRequest</tt>) when finished with reading, for
     * creating a response.
     *
     * @param reference to specify the offset as <tt>int</tt>.
     * @param count     to specify the number of bytes as <tt>int</tt>.
     */
    void prepareData(int reference, int count);
}