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
package org.datacleaner.util;

import java.awt.Dimension;
import java.util.Map;

import org.datacleaner.user.UserPreferences;

public class WindowSizePreferences {

    private final UserPreferences _userPreferences;
    private final String _identifier;
    private final int _defaultWidth;
    private final int _defaultHeight;

    /**
     *
     * @param userPreferences
     *            represents the settings provided by the user at runtime @{link
     *            UserPreferences}
     * @param windowClass
     *            the window class that calls this class
     * @param defaultWidth
     *            represents the default width
     * @param defaultHeight
     *            represents the default height
     */
    public WindowSizePreferences(final UserPreferences userPreferences, final Class<?> windowClass,
            final int defaultWidth, final int defaultHeight) {
        _identifier = windowClass.getName();
        _defaultWidth = defaultWidth;
        _defaultHeight = defaultHeight;
        _userPreferences = userPreferences;
    }

    public UserPreferences getUserPreferences() {
        return _userPreferences;
    }

    public boolean isWindowMaximized() {
        final Map<String, String> properties = _userPreferences.getAdditionalProperties();
        final String isMaximized = properties.get(getIsMaximizedWindowPropertyKey());
        if (isMaximized == null) {
            return true;
        }
        return Boolean.parseBoolean(isMaximized);
    }

    public Dimension getUserPreferredSize() {
        final Map<String, String> properties = _userPreferences.getAdditionalProperties();
        String widthStr = properties.get(getWidthPropertyKey());
        if (widthStr == null) {
            widthStr = "" + _defaultWidth;
        }
        String heightStr = properties.get(getHeightPropertyKey());
        if (heightStr == null) {
            heightStr = "" + _defaultHeight;
        }
        return new Dimension(Integer.parseInt(widthStr), Integer.parseInt(heightStr));
    }

    public void setUserPreferredSize(final Dimension size, final boolean isMaximized) {
        final Map<String, String> properties = _userPreferences.getAdditionalProperties();
        if (size != null) {
            properties.put(getWidthPropertyKey(), "" + size.width);
            properties.put(getHeightPropertyKey(), "" + size.height);
        }

        properties.put(getIsMaximizedWindowPropertyKey(), "" + isMaximized);
        _userPreferences.save();
    }

    private String getHeightPropertyKey() {
        return _identifier + "." + _identifier + ".height";
    }

    private String getWidthPropertyKey() {
        return _identifier + "." + _identifier + ".width";
    }

    private String getIsMaximizedWindowPropertyKey() {
        return _identifier + "." + _identifier + ".maximized";
    }

}
