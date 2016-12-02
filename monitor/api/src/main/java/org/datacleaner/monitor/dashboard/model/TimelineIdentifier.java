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
package org.datacleaner.monitor.dashboard.model;

import java.io.Serializable;

/**
 * Identifying object for a timeline definition saved in the repository.
 */
public class TimelineIdentifier implements Serializable {

    private static final long serialVersionUID = 1L;

    private String _name;
    private String _path;
    private DashboardGroup _group;

    public TimelineIdentifier(final String name, final String path, final DashboardGroup group) {
        _name = name;
        _path = path;
        _group = group;
    }

    public TimelineIdentifier() {
        this(null, null, null);
    }

    public DashboardGroup getGroup() {
        return _group;
    }

    public String getPath() {
        return _path;
    }

    public void setPath(final String path) {
        _path = path;
    }

    public String getName() {
        return _name;
    }

    public void setName(final String name) {
        _name = name;
    }

    @Override
    public String toString() {
        return "TimelineIdentifier[name=" + _name + ",path=" + _path + "]";
    }
}
