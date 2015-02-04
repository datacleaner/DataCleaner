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
package org.datacleaner.widgets;

import java.awt.Dimension;
import java.util.Map;

import org.datacleaner.panels.DCPanel;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.UserPreferencesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A panel that stores it's preferred size in the user preferences.
 * 
 * @author Kasper Sørensen
 */
public class DCPersistentSizedPanel extends DCPanel {

    private static final long serialVersionUID = 1L;

   // private static final Logger logger = LoggerFactory.getLogger(DCPersistentSizedPanel.class);

    private final UserPreferencesUtils _userPreferenceUtils; 

    public DCPersistentSizedPanel(final UserPreferencesUtils userPreferenceUtils) {

        _userPreferenceUtils = userPreferenceUtils;
        final Dimension preferredSizeFromUserPreferences = _userPreferenceUtils.getPreferredSizeFromUserPreferences();
        setPreferredSize(preferredSizeFromUserPreferences);

    }
    @Override
    public void removeNotify() {
        super.removeNotify();
    }
}
