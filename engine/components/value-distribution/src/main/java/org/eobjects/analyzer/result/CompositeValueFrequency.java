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
package org.eobjects.analyzer.result;

import java.util.Collection;
import java.util.List;

import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.Func;

/**
 * Represents a group of values, counted as one item.
 */
public final class CompositeValueFrequency extends AbstractValueFrequency implements ValueFrequency {

    private static final long serialVersionUID = 1L;

    private final String _name;
    private final List<ValueFrequency> _children;
    private int _count;

    /**
     * Creates a composite value frequency based on a list of child values.
     * 
     * @param name
     * @param children
     */
    public CompositeValueFrequency(String name, List<ValueFrequency> children) {
        _name = name;
        _children = children;
        int sum = 0;
        for (ValueFrequency child : _children) {
            sum += child.getCount();
        }
        _count = sum;
    }

    /**
     * Creates a composite value frequency based on a list of child values and a
     * constant count for all children.
     * 
     * @param name
     * @param values
     * @param count
     */
    public CompositeValueFrequency(String name, Collection<String> values, final int count) {
        _name = name;
        _children = CollectionUtils.map(values, new Func<String, ValueFrequency>() {
            @Override
            public ValueFrequency eval(String str) {
                return new SingleValueFrequency(str, count);
            }
        });
        _count = count * values.size();
    }

    /**
     * Creates a composite value frequency for a group of values which have a
     * count but no details about the children.
     * 
     * @param name
     * @param count
     */
    public CompositeValueFrequency(String name, int count) {
        _name = name;
        _children = null;
        _count = count;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public int getCount() {
        return _count;
    }

    @Override
    public boolean isComposite() {
        return true;
    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    public List<ValueFrequency> getChildren() {
        return _children;
    }

}
