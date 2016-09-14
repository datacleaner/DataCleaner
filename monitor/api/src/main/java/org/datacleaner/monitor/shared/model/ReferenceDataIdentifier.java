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
package org.datacleaner.monitor.shared.model;

import java.io.Serializable;

/**
 * Identifies reference data in the repository.
 */
public class ReferenceDataIdentifier implements Serializable, Comparable<ReferenceDataIdentifier>, HasName {

    private static final long serialVersionUID = 1L;

    private String _name;

    public ReferenceDataIdentifier() {
        this(null);
    }

    public ReferenceDataIdentifier(String name) {
        _name = name;
    }

    @Override
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    @Override
    public String toString() {
        return "ReferenceDataIdentifier[name=" + _name + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_name == null) ? 0 : _name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ReferenceDataIdentifier other = (ReferenceDataIdentifier) obj;
        if (_name == null) {
            if (other._name != null)
                return false;
        } else if (!_name.equals(other._name))
            return false;
        return true;
    }

    @Override
    public int compareTo(ReferenceDataIdentifier o) {
        if (o == null) {
            return 1;
        }
        
        String name = getName();
        
        if (name == null) {
            return -1;
        }
        
        return name.compareTo(o.getName());
    }
}
