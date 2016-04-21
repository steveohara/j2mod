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
package com.ghgande.j2mod.modbus.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Class implementing a simple thread pool.
 *
 * @author Dieter Wimberger
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public class ThreadPool {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPool.class);

    private LinkedBlockingQueue<Runnable> taskPool;
    private List<PoolThread> threadPool = new ArrayList<PoolThread>();
    private int size = 1;
    private boolean running;

    /**
     * Constructs a new <tt>ThreadPool</tt> instance.
     *
     * @param size the size of the thread pool.
     */
    public ThreadPool(int size) {
        this.size = size;
        taskPool = new LinkedBlockingQueue<Runnable>();
        initPool();
    }

    /**
     * Execute the <tt>Runnable</tt> instance
     * through a thread in this <tt>ThreadPool</tt>.
     *
     * @param task the <tt>Runnable</tt> to be executed.
     */
    public synchronized void execute(Runnable task) {
        if (running) {
            try {
                taskPool.put(task);
            }
            catch (InterruptedException ex) {
                //FIXME: Handle!?
            }
        }
    }

    /**
     * Initializes the pool, populating it with
     * n started threads.
     */
    protected void initPool() {
        running = true;
        for (int i = size; --i >= 0; ) {
            PoolThread thread = new PoolThread();
            threadPool.add(thread);
            thread.start();
        }
    }

    /**
     * Shutdown the pool of threads
     */
    public void close() {
        if (running) {
            taskPool.clear();
            running = false;
            for (PoolThread thread : threadPool) {
                thread.interrupt();
            }
        }
    }

    /**
     * Inner class implementing a thread that can be
     * run in a <tt>ThreadPool</tt>.
     *
     * @author Dieter Wimberger
     * @version 1.2rc1 (09/11/2004)
     */
    private class PoolThread extends Thread {

        /**
         * Runs the <tt>PoolThread</tt>.
         * <p>
         * This method will infinitely loop, picking
         * up available tasks from the <tt>LinkedQueue</tt>.
         */
        public void run() {
            logger.debug("Running PoolThread");
            do {
                try {
                    logger.debug(this.toString());
                    taskPool.take().run();
                }
                catch (Exception ex) {
                    if (running) {
                        logger.error("Problem starting receiver thread", ex);
                    }
                }
            } while (running);
        }
    }

}
