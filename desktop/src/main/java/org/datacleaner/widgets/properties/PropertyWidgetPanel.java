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
package org.datacleaner.widgets.properties;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;

import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.util.StringUtils;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.jdesktop.swingx.JXLabel;

/**
 * A panel which presents multiple property widgets and their labels in a
 * form-like view.
 * 
 * @author kasper
 */
public abstract class PropertyWidgetPanel extends DCPanel {

	private static final int FIELD_LABEL_WIDTH = 200;

	private static final long serialVersionUID = 1L;

	private static final Insets insets = new Insets(4, 4, 4, 4);;
	private int _rowCounter;

	public PropertyWidgetPanel() {
		super();
		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[] { FIELD_LABEL_WIDTH };
		setLayout(layout);
		_rowCounter = 0;
	}

	public boolean isEmpty() {
		return getComponentCount() == 0;
	}

	public void addProperties(Collection<ConfiguredPropertyDescriptor> properties) {
		for (ConfiguredPropertyDescriptor propertyDescriptor : properties) {
			final PropertyWidget<?> propertyWidget = getPropertyWidget(propertyDescriptor);

			// some properties may not have a PropertyWidget
			if (propertyWidget != null) {
				JComponent component = propertyWidget.getWidget();

				// some properties may have a PropertyWidget implementation that
				// is "invisible", ie. the JComponent is not returned
				if (component != null) {
					String propertyName = propertyDescriptor.getName();
					if (!propertyName.endsWith(":")) {
						propertyName += ":";
					}

					final DCLabel propertyLabel = DCLabel.dark(propertyName);
					propertyLabel.setFont(WidgetUtils.FONT_SMALL);

					add(propertyLabel, new GridBagConstraints(0, _rowCounter, 1, 1, 0d, 0d, GridBagConstraints.NORTHWEST,
							GridBagConstraints.BOTH, insets, 0, 0));

					final String description = propertyDescriptor.getDescription();
					if (!StringUtils.isNullOrEmpty(description)) {
						propertyLabel.setToolTipText(description);

						final JXLabel descriptionLabel = new JXLabel(description);
						descriptionLabel.setLineWrap(true);
						descriptionLabel.setFont(WidgetUtils.FONT_SMALL);
						descriptionLabel.setBorder(new EmptyBorder(0, 4, 0, 0));
						descriptionLabel.setVerticalAlignment(JXLabel.TOP);
						descriptionLabel.setPreferredSize(new Dimension(FIELD_LABEL_WIDTH - 4, 0));
						add(descriptionLabel, new GridBagConstraints(0, _rowCounter + 1, 1, 1, 0d, 1d,
								GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0));
					}

					add(component, new GridBagConstraints(1, _rowCounter, 1, 2, 1d, 1d, GridBagConstraints.NORTHEAST,
							GridBagConstraints.BOTH, insets, 0, 0));

					// each property spans two "rows"
					_rowCounter = _rowCounter + 2;
				}
			}
		}
	}

	protected abstract PropertyWidget<?> getPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor);
}
