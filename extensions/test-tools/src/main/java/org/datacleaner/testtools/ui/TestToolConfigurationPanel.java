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
package org.datacleaner.testtools.ui;

import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.AbstractBeanJobBuilder;
import org.datacleaner.job.builder.AnalyzerJobBuilder;
import org.datacleaner.panels.AnalyzerJobBuilderPanel;
import org.datacleaner.testtools.EmailConfiguration;
import org.datacleaner.widgets.properties.PropertyWidget;
import org.datacleaner.widgets.properties.PropertyWidgetFactory;

public class TestToolConfigurationPanel extends AnalyzerJobBuilderPanel {

	private static final long serialVersionUID = 1L;

	public TestToolConfigurationPanel(AnalyzerJobBuilder<?> analyzerJobBuilder,
			PropertyWidgetFactory propertyWidgetFactory) {
		super(analyzerJobBuilder, propertyWidgetFactory);
	}

	@Override
	protected PropertyWidget<?> createPropertyWidget(
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			ConfiguredPropertyDescriptor propertyDescriptor) {
		if (propertyDescriptor.getBaseType() == EmailConfiguration.class) {
			return new EmailConfigurationPropertyWidget(beanJobBuilder,
					propertyDescriptor);
		}
		return super.createPropertyWidget(beanJobBuilder, propertyDescriptor);
	}

}
