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
package org.eobjects.datacleaner.monitor.timeline.model;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Identifying object for a timeline definition saved in the repository.
 */
public class TimelineIdentifier implements IsSerializable {

    private String _name;
    private String _path;

    public TimelineIdentifier(String name, String path) {
        _name = name;
        _path = path;
    }

    public TimelineIdentifier() {
        this(null, null);
    }

    public String getPath() {
        return _path;
    }

    public void setPath(String path) {
        _path = path;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    @Override
    public String toString() {
        return "TimelineIdentifier[name=" + _name + ",path=" + _path + "]";
    }
}
