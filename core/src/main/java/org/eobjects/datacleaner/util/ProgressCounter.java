/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.datacleaner.util;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A progress counter that can keep track of e.g. current row counting and tell
 * if updates to the count would be significant to a user. It allows you to set
 * a count value atomically, and only if it is greater than other informed
 * counter values.
 */
public class ProgressCounter implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int _significantUpdateIntervalMillis;
    private final AtomicInteger _value;
    private transient volatile long _lastUpdate;

    /**
     * Constructs the counter
     * 
     * @param significantUpdateIntervalMillis
     *            the number of milliseconds that are considered a minimum
     *            between significant updates
     */
    public ProgressCounter(int significantUpdateIntervalMillis) {
        _value = new AtomicInteger(0);
        _lastUpdate = -1;
        _significantUpdateIntervalMillis = significantUpdateIntervalMillis;
    }

    /**
     * Constructs the counter with defaults.
     */
    public ProgressCounter() {
        this(2000);
    }

    /**
     * Gets the value of the counter.
     * 
     * @return
     */
    public int get() {
        return _value.get();
    }

    /**
     * Sets the value of the counter if the new value is considered significant
     * to the user. In order to be 'significant' we require two circumstances to
     * be true:
     * 
     * 1) The new value is greater than the previous value.
     * 
     * 2) The value was not updated within the last significant update interval.
     * 
     * @param newValue
     * @return
     */
    public boolean setIfSignificantToUser(final int newValue) {
        while (true) {
            final int currentValue = _value.get();

            if (newValue > currentValue) {
                final long currentTimestamp;
                if (_significantUpdateIntervalMillis > 0) {
                    currentTimestamp = System.currentTimeMillis();
                    if (currentTimestamp - _lastUpdate < _significantUpdateIntervalMillis) {
                        return false;
                    }
                } else {
                    currentTimestamp = -1;
                }

                if (_value.compareAndSet(currentValue, newValue)) {
                    _lastUpdate = currentTimestamp;
                    return true;
                }
            } else {
                return false;
            }
        }
    }
}
