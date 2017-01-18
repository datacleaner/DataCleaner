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
package org.datacleaner.monitor.server.hotfolder;

import org.springframework.stereotype.Component;

/**
 * A spring bean used to define the preferences of the hot-folder functionality.
 */
@Component("hotFolderPreferences")
public class HotFolderPreferences {
    protected static final String FILE_PATH_PLACEHOLDER = "_FILE_PATH_PLACEHOLDER_";

    private int _waitTimeoutMinutes = 30;
    private String _waitStrategyClass = "org.datacleaner.monitor.server.hotfolder.DefaultWaitForCompleteFileStrategy";
    private String _unixCommandTemplate = "lsof | grep \" " + FILE_PATH_PLACEHOLDER
            + "\" | sed s/\\ \\ */\\ /g | cut -d' ' -f 4";

    public int getWaitTimeoutMinutes() {
        return _waitTimeoutMinutes;
    }

    public void setWaitTimeoutMinutes(final int waitTimeoutMinutes) {
        _waitTimeoutMinutes = waitTimeoutMinutes;
    }

    public String getWaitStrategyClass() {
        return _waitStrategyClass;
    }

    public void setWaitStrategyClass(final String waitStrategyClass) {
        _waitStrategyClass = waitStrategyClass;
    }

    public String getUnixCommandTemplate() {
        return _unixCommandTemplate;
    }

    public void setUnixCommandTemplate(final String unixCommandTemplate) {
        _unixCommandTemplate = unixCommandTemplate;
    }
}
