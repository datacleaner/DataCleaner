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
package org.datacleaner.data;

import org.apache.metamodel.schema.Column;

/**
 * A mock-implementation of the input column. Use this only for testing purposes
 * or in cases where you want to circumvent the actual framework!
 * 
 * @param <E>
 */
public class MockInputColumn<E> extends AbstractInputColumn<E> {

    private static final long serialVersionUID = 1L;

    private String _name;
    private final Class<? extends E> _clazz;
    private final Column _physicalColumn;

    public MockInputColumn(Column physicalColumn, Class<? extends E> clazz) {
        this(physicalColumn.getName(), clazz, physicalColumn);
    }

    public MockInputColumn(String name) {
        this(name, null);
    }

    public MockInputColumn(String name, Class<? extends E> clazz) {
        this(name, clazz, null);
    }

    public MockInputColumn(String name, Class<? extends E> clazz, Column physicalColumn) {
        _name = name;
        _clazz = clazz;
        _physicalColumn = physicalColumn;
    }

    public void setName(String name) {
        _name = name;
    }

    @Override
    public Class<? extends E> getDataType() {
        return _clazz;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    protected Column getPhysicalColumnInternal() {
        return _physicalColumn;
    }

    @Override
    protected int hashCodeInternal() {
        return _name.hashCode();
    }

    @Override
    protected boolean equalsInternal(AbstractInputColumn<?> that) {
        return this == that;
    }

    @Override
    public String toString() {
        return "MockInputColumn[name=" + _name + "]";
    }
}
