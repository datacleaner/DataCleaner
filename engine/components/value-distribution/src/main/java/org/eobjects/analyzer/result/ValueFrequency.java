/**
 * DataCleaner (community edition)
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
package org.eobjects.analyzer.result;

import java.io.Serializable;
import java.util.List;

import org.apache.metamodel.util.HasName;

/**
 * Represents a frequency of values. Value frequencies can either represent a single
 * value or a composite list of other values (for instance - "<unique>" is
 * a typical composite group of values - all with a frequency of 1).
 */
public interface ValueFrequency extends HasName, Serializable, Comparable<ValueFrequency> {

    /**
     * Gets the name of this value count
     * 
     * @return a name to display for this value count. Never null.
     */
    public String getName();

    /**
     * Gets the frequency/count of this value or group of values.
     * 
     * @return
     */
    public int getCount();

    /**
     * Determines if this {@link ValueFrequency} is a composite or not. If true,
     * {@link #getChildren()} will potentially hold child values. If false,
     * {@link #getValue()} will always hold a value.
     * 
     * @return
     */
    public boolean isComposite();

    /**
     * Gets the value that this {@link ValueFrequency} represents. If this is a
     * composite value count, this method will return null.
     * 
     * @see #isComposite()
     * 
     * @return the value that this {@link ValueFrequency} represents
     */
    public String getValue();

    /**
     * Gets the children of this {@link ValueFrequency}, if it is a composite value
     * count. May return null.
     * 
     * @see #isComposite()
     * 
     * @return the children of this {@link ValueFrequency}, if there are any
     */
    public List<ValueFrequency> getChildren();
}
