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
package org.datacleaner.reference.regexswap;

import java.io.Serializable;
import java.util.List;

import org.apache.metamodel.util.BaseObject;

public final class Category extends BaseObject implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String _name;
    private final String _description;
    private final String _detailsUrl;

    public Category(final String name, final String description, final String detailsUrl) {
        _name = name;
        _description = description;
        _detailsUrl = detailsUrl;
    }

    public String getName() {
        return _name;
    }

    public String getDescription() {
        return _description;
    }

    public String getDetailsUrl() {
        return _detailsUrl;
    }

    @Override
    protected void decorateIdentity(final List<Object> identifiers) {
        identifiers.add(_name);
        identifiers.add(_description);
        identifiers.add(_detailsUrl);
    }
}
