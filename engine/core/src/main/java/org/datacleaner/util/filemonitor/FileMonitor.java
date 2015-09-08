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
package org.datacleaner.util.filemonitor;

import java.nio.file.WatchService;

/**
 * A monitoring mechanism for file change events. Typically used by file-based
 * reference data to monitor changes to the reference data files.
 * 
 * @deprecated as of Java 7 the preferred way to monitor files is to use
 *             {@link WatchService}.
 */
@Deprecated
public interface FileMonitor {

    public boolean hasChanged();
}
