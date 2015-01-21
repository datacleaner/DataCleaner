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

import org.datacleaner.api.InputColumn;
import org.datacleaner.util.ReflectionUtils;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnType;

public final class MetaModelInputColumn extends AbstractInputColumn<Object> {

    private static final long serialVersionUID = 1L;

    private final Column _column;

    public MetaModelInputColumn(Column column) {
        if (column == null) {
            throw new IllegalArgumentException("column cannot be null");
        }
        _column = column;
    }

    @SuppressWarnings("unchecked")
    public <E> InputColumn<E> narrow(Class<E> e) {
        Class<?> javaEquivalentClass = _column.getType().getJavaEquivalentClass();
        if (ReflectionUtils.is(javaEquivalentClass, e)) {
            return (InputColumn<E>) this;
        }
        throw new IllegalArgumentException("Can only narrow this column to supertypes of: " + javaEquivalentClass);
    }

    @Override
    public String getName() {
        return _column.getName();
    }

    @Override
    protected Column getPhysicalColumnInternal() {
        return _column;
    }

    @Override
    protected boolean equalsInternal(AbstractInputColumn<?> that) {
        MetaModelInputColumn that2 = (MetaModelInputColumn) that;
        return _column.equals(that2._column);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Object> getDataType() {
        ColumnType type = _column.getType();
        if (type == null) {
            return null;
        } else if (type == ColumnType.CLOB) {
            return String.class;
        } else if (type == ColumnType.BLOB) {
            return byte[].class;
        }
        return (Class<Object>) type.getJavaEquivalentClass();
    }

    @Override
    protected int hashCodeInternal() {
        return _column.hashCode();
    }

    @Override
    public String toString() {
        return "MetaModelInputColumn[" + _column.getQualifiedLabel() + "]";
    }
}
