/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.monitor.scheduling.model;

import java.io.Serializable;

/**
 * Represents information about how a job's variable values are provided
 */
public class VariableProviderDefinition implements Serializable {

    private static final long serialVersionUID = 1L;

    private String _className;

    public String getClassName() {
        return _className;
    }

    public void setClassName(String className) {
        _className = className;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_className == null) ? 0 : _className.hashCode());
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
        VariableProviderDefinition other = (VariableProviderDefinition) obj;
        if (_className == null) {
            if (other._className != null)
                return false;
        } else if (!_className.equals(other._className))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "VariableProviderDefinition[" + _className + "]";
    }
}
