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
package org.eobjects.datacleaner.panels.maxrows;

import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;

import org.eobjects.analyzer.beans.filter.MaxRowsFilter;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.panels.FilterJobBuilderPanel;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.widgets.properties.MinimalPropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;

/**
 * Specialized {@link FilterJobBuilderPanel} for the {@link MaxRowsFilter} when
 * toggled through the {@link MaxRowsFilterShortcutPanel}.
 */
public class MaxRowsFilterShortcutJobBuilderPanel extends FilterJobBuilderPanel {

	private static final long serialVersionUID = 1L;

	public MaxRowsFilterShortcutJobBuilderPanel(
			FilterJobBuilder<?, ?> filterJobBuilder,
			WindowContext windowContext,
			PropertyWidgetFactory propertyWidgetFactory) {
		super(filterJobBuilder, windowContext, propertyWidgetFactory);
	}

	@Override
	protected PropertyWidget<?> createPropertyWidget(
			final AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			final ConfiguredPropertyDescriptor propertyDescriptor) {
		if (MaxRowsFilterShortcutPanel.MAX_ROWS_PROPERTY_NAME.equals(propertyDescriptor.getName())) {
			// insert a simplified property widget, in order to delegate to
			// shortcut panel.
			return new MinimalPropertyWidget<Integer>(beanJobBuilder,
					propertyDescriptor) {

				@Override
				public JComponent getWidget() {
					DCLabel label = DCLabel.dark("Configured in 'Source' tab");
					label.setBorder(new EmptyBorder(4, 4, 4, 4));
					return label;
				}

				@Override
				public Integer getValue() {
					return getCurrentValue();
				}

				@Override
				protected void setValue(Integer value) {
					// ignore
				}
			};
		}
		return super.createPropertyWidget(beanJobBuilder, propertyDescriptor);
	}
}
