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

import java.util.Vector;

/**
 * A cleanroom implementation of the Observable pattern.
 *
 * @author Dieter Wimberger (wimpi)
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public class Observable {

    private static final Logger logger = LoggerFactory.getLogger(Observable.class);

    private Vector<Observer> observers;

    /**
     * Constructs a new Observable instance.
     */
    public Observable() {
        observers = new Vector<Observer>(10);
    }

    public synchronized int getObserverCount() {
        return observers.size();
    }

    /**
     * Adds an observer instance if it is not already in the set of observers
     * for this <tt>Observable</tt>.
     *
     * @param o an observer instance to be added.
     */
    public synchronized void addObserver(Observer o) {
        if (!observers.contains(o)) {
            observers.addElement(o);
        }
    }

    /**
     * Removes an observer instance from the set of observers of this
     * <tt>Observable</tt>.
     *
     * @param o an observer instance to be removed.
     */
    public synchronized void removeObserver(Observer o) {
        observers.removeElement(o);
    }

    /**
     * Removes all observer instances from the set of observers of this
     * <tt>Observable</tt>.
     */
    public synchronized void removeObservers() {
        observers.removeAllElements();
    }

    /**
     * Notifies all observer instances in the set of observers of this
     * <tt>Observable</tt>.
     *
     * @param arg an arbitrary argument to be passed.
     */
    public synchronized void notifyObservers(Object arg) {
        for (int i = 0; i < observers.size(); i++) {
            observers.elementAt(i).update(this, arg);
        }
    }
}
