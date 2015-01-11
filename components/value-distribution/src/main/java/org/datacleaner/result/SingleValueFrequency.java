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
package org.datacleaner.result;

import java.util.List;

import org.datacleaner.beans.valuedist.ValueDistributionAnalyzer;
import org.datacleaner.util.LabelUtils;

/**
 * Represents a simple value and count pair, used by the
 * {@link ValueDistributionAnalyzer} to represent which values occur at what
 * frequencies.
 */
public final class SingleValueFrequency extends AbstractValueFrequency implements ValueFrequency {

    private static final long serialVersionUID = 1L;

    private final String _value;
    private final int _count;

    public SingleValueFrequency(String value, int count) {
        _value = value;
        _count = count;
    }

    public String getValue() {
        return _value;
    }

    public int getCount() {
        return _count;
    }

    @Override
    public String getName() {
        return LabelUtils.getLabel(_value);
    }

    @Override
    public boolean isComposite() {
        return false;
    }

    @Override
    public List<ValueFrequency> getChildren() {
        return null;
    }

}
