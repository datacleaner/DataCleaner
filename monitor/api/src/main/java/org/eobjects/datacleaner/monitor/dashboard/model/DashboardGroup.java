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
package org.eobjects.datacleaner.monitor.dashboard.model;

import java.io.Serializable;

/**
 * Represents a logical grouping of dashboard widgets
 */
public class DashboardGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_GROUP_NAME = "(default)";

    private String _name;
    private String _description;

    public DashboardGroup() {
        this(null);
    }

    public DashboardGroup(String name) {
        _name = name;
    }

    /**
     * Determines if this {@link DashboardGroup} is the "default" group, i.e.
     * the first unnamed group shown.
     * 
     * @return
     */
    public boolean isDefaultGroup() {
        return DEFAULT_GROUP_NAME.equals(_name);
    }

    public String getDescription() {
        return _description;
    }

    public void setDescription(String description) {
        _description = description;
    }

    public String getName() {
        return _name;
    }

    @Override
    public String toString() {
        return "TimelineGroup[" + _name + "]";
    }
}
