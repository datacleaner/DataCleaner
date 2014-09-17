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
package org.eobjects.datacleaner.widgets;

import java.awt.Dimension;
import java.util.Map;

import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.UserPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A panel that stores it's preferred size in the user preferences.
 * 
 * @author Kasper Sørensen
 */
public class DCPersistentSizedPanel extends DCPanel {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(DCPersistentSizedPanel.class);
	
	private final String _identifier;
	private final int _defaultWidth;
	private final int _defaultHeight;
	private final UserPreferences _userPreferences;

	public DCPersistentSizedPanel(UserPreferences userPreferences, String identifier, int defaultWidth, int defaultHeight) {
		_identifier = identifier;
		_defaultWidth = defaultWidth;
		_defaultHeight = defaultHeight;
		_userPreferences = userPreferences;

		setPreferredSize(getPreferredSizeFromUserPreferences());
	}
	
	@Override
	public void removeNotify() {
		super.removeNotify();
		
		Map<String, String> properties = _userPreferences.getAdditionalProperties();

		Dimension size = getSize();
		logger.info("Persisting panel size: {}", size);
		properties.put(getWidthPropertyKey(), "" + size.width);
		properties.put(getHeightPropertyKey(), "" + size.height);
	}

	private Dimension getPreferredSizeFromUserPreferences() {
		Map<String, String> properties = _userPreferences.getAdditionalProperties();
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

	private String getHeightPropertyKey() {
		return getClass().getName() + "." + _identifier + ".height";
	}

	private String getWidthPropertyKey() {
		return getClass().getName() + "." + _identifier + ".width";
	}
}
