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
package org.eobjects.datacleaner.testtools.ui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.eobjects.datacleaner.testtools.EmailConfiguration;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;

public class EmailConfigurationRenderer extends DefaultListCellRenderer
		implements ListCellRenderer {

	private static final long serialVersionUID = 1L;

	private static final ImageManager imageManager = ImageManager.getInstance();

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		final Component result = super.getListCellRendererComponent(list,
				value, index, isSelected, cellHasFocus);
		if (value instanceof EmailConfiguration) {
			final EmailConfiguration emailConfiguration = (EmailConfiguration) value;
			final JLabel label = (JLabel) result;

			label.setText(emailConfiguration.getName());
			label.setIcon(imageManager.getImageIcon(
					"images/testtools/EmailConfiguration.png",
					IconUtils.ICON_SIZE_MEDIUM, getClass().getClassLoader()));
		}
		return result;
	}

}
