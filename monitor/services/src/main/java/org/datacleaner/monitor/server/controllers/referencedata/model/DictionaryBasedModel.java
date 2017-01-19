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
package org.datacleaner.monitor.server.controllers.referencedata.model;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DictionaryBasedModel<T> {
    protected final boolean _caseSensitive;
    private final String _name;
    private final Collection<T> _entries;

    public DictionaryBasedModel(final String name, final Collection<T> entries,
            @JsonProperty("caseSensitive") final boolean caseSensitive) {
        _name = name;
        _entries = entries;
        _caseSensitive = caseSensitive;
    }

    public String getName() {
        return _name;
    }

    public Collection<T> getEntries() {
        return _entries;
    }

    public boolean isCaseSensitive() {
        return _caseSensitive;
    }
}
