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
package org.eobjects.datacleaner.monitor.pentaho;

import org.eobjects.analyzer.util.StringUtils;

public class PentahoTransformation {

    private final String _id;
    private final String _name;

    public PentahoTransformation(String id, String name) {
        _id = id;
        _name = name;
    }

    public String getId() {
        return _id;
    }

    public String getName() {
        return _name;
    }

    /**
     * Determines if this {@link PentahoTransformation} matches the queried id
     * and/or name
     * 
     * @param id
     * @param name
     * @return
     */
    public boolean matches(String id, String name) {
        if (id == null && name == null) {
            return false;
        }

        if (!StringUtils.isNullOrEmpty(id)) {
            if (!id.equals(_id)) {
                return false;
            }
        }

        if (!StringUtils.isNullOrEmpty(name)) {
            if (!name.equals(_name)) {
                return false;
            }
        }

        return true;
    }
}
