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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.datacleaner.api.InputColumn;
import org.datacleaner.job.IdGenerator;
import org.datacleaner.util.InputColumnComparator;
import org.apache.metamodel.schema.Column;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an InputColumn that is a result of a transformer.
 * 
 * @param <E>
 */
public class TransformedInputColumn<E> implements MutableInputColumn<E>, Serializable, Comparable<InputColumn<E>> {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(TransformedInputColumn.class);

    private final transient Collection<Listener> _listeners;
    private final String _id;
    private Class<?> _dataType;
    private String _name;
    private String _initialName;
    private boolean _hidden;

    public TransformedInputColumn(String name, IdGenerator idGenerator) {
        this(name, idGenerator.nextId());
    }

    public TransformedInputColumn(String name, String id) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        _name = name;
        _initialName = name;
        _id = id;
        _listeners = new HashSet<Listener>();
        _hidden = false;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public String getInitialName() {
        return _initialName;
    }

    public void setInitialName(String initialName) {
        _initialName = initialName;
    }

    @Override
    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        if (name.equals(_name)) {
            return;
        }
        final String oldName = _name;
        _name = name;
        for (Listener listener : getListeners()) {
            listener.onNameChanged(this, oldName, name);
        }
    }

    public Collection<Listener> getListeners() {
        if (_listeners == null) {
            return Collections.emptyList();
        }
        return _listeners;
    }

    @Override
    public String getId() {
        return _id;
    }

    public void setDataType(Class<?> dataType) {
        _dataType = dataType;
    }

    @Override
    public boolean isPhysicalColumn() {
        return false;
    }

    @Override
    public boolean isVirtualColumn() {
        return true;
    }

    @Override
    public String toString() {
        return "TransformedInputColumn[id=" + _id + ",name=" + _name + "]";
    }

    @Override
    public Column getPhysicalColumn() throws IllegalStateException {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends E> getDataType() {
        return (Class<? extends E>) _dataType;
    }

    @Override
    public int hashCode() {
        return _id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        // transformed input columns should always rely on identity equality -
        // other transformed columns with the same name, id etc. are NOT
        // necesarily equal (may come from another job, or even a copy of the
        // job).
        return this == obj;
    }

    @Override
    public int compareTo(InputColumn<E> o) {
        return InputColumnComparator.compareInputColumns(this, o);
    }

    @Override
    public boolean isHidden() {
        return _hidden;
    }

    @Override
    public void setHidden(boolean hidden) {
        _hidden = hidden;
        for (Listener listener : getListeners()) {
            listener.onVisibilityChanged(this, hidden);
        }
    }

    @Override
    public boolean addListener(Listener listener) {
        if (_listeners == null) {
            logger.warn("Attempted to add listener onto TransformedInputColumn with null List of listeners");
            return false;
        }
        boolean added = _listeners.add(listener);
        if (logger.isDebugEnabled()) {
            logger.debug("[{}].addListener({}): {}", getName(), listener, added);
        }
        return added;
    }

    @Override
    public boolean removeListener(Listener listener) {
        if (_listeners == null) {
            logger.warn("Attempted to remove listener onto TransformedInputColumn with null List of listeners");
            return false;
        }
        boolean removed = _listeners.remove(listener);
        if (logger.isDebugEnabled()) {
            logger.debug("[{}].removeListener({}): {}", getName(), listener, removed);
            logger.debug("[{}].listeners.size: {}", getName(), _listeners.size());
        }
        return removed;
    }
}
