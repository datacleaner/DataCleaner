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

import java.io.File;
import java.nio.file.WatchService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory that creates file monitors.
 *
 * @deprecated use {@link WatchService} instead
 */
@Deprecated
public class FileMonitorFactory {

    private static final Logger logger = LoggerFactory.getLogger(FileMonitorFactory.class);

    public static FileMonitor getFileMonitor(final File file) {
        logger.debug("getFileMonitor({})", file);
        return new PollingBasedFileMonitor(file);
    }
}
