/**
 * AnalyzerBeans
 * Copyright (C) 2014 Neopost - Customer Information Management
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
package org.eobjects.analyzer.beans.writers;

import org.apache.metamodel.util.HasName;

/**
 * An enum that represents typical user-selectable sizes for a
 * {@link WriteBuffer}
 */
public enum WriteBufferSizeOption implements HasName {

    HUGE("Huge (1M values)", 1000000),

    LARGE("Large (100k values)", 100000),

    MEDIUM("Medium (10k values)", 10000),

    SMALL("Small (1000 values)", 1000),

    TINY("Tiny (100 values)", 100);
    
    private final String _name;
    private final int _values;

    private WriteBufferSizeOption(String name, int values) {
        _name = name;
        _values = values;
    }
    
    /**
     * Gets the number of values (rows x columns) that this buffer size
     * recommends
     * 
     * @return the number of values (rows x columns) that this buffer size
     *         recommends
     */
    public int getValues() {
        return _values;
    }

    /**
     * Gets the display name of this buffer size
     * 
     * @return the display name of this buffer size
     */
    @Override
    public String getName() {
        return _name;
    }
    
    public int calculateBufferSize(int numColumns) {
        // add one, because there is a small "per record" overhead
        final int objectsPerRow = numColumns + 1;

        final int bufferSize = _values / objectsPerRow;
        
        return Math.max(1, bufferSize);
    }
}
