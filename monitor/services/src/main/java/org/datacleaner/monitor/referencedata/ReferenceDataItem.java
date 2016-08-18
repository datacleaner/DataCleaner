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
package org.datacleaner.monitor.referencedata;

import java.io.Serializable;

public class ReferenceDataItem implements Serializable {
    public enum Type {
        DICTIONARY,
        SYNONYM_CATALOG,
        STRING_PATTERN,
    }

    private String _name;
    private Type _type;

    public ReferenceDataItem() {
    }

    public ReferenceDataItem(Type type, String name) {
        _type = type;
        _name = name;
    }

    public Type getType() {
        return _type;
    }

    public void setType(final Type type) {
        _type = type;
    }

    public String getName() {
        return _name;
    }

    public void setName(final String name) {
        _name = name;
    }
}
