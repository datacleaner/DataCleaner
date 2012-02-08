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
package org.eobjects.datacleaner.panels.datastructures;

import java.util.HashMap;
import java.util.Map;

import org.eobjects.analyzer.beans.datastructures.SelectFromMapTransformer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.panels.TransformerJobBuilderPanel;
import org.eobjects.datacleaner.panels.TransformerJobBuilderPresenter;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;

/**
 * Spectialized {@link TransformerJobBuilderPresenter} for the
 * {@link SelectFromMapTransformer}.
 */
final class SelectFromMapJobBuilderPresenter extends TransformerJobBuilderPanel {

	private static final long serialVersionUID = 1L;
	
	private final Map<ConfiguredPropertyDescriptor, PropertyWidget<?>> _overriddenPropertyWidgets;

	public SelectFromMapJobBuilderPresenter(TransformerJobBuilder<SelectFromMapTransformer> tjb,
			WindowContext windowContext, PropertyWidgetFactory propertyWidgetFactory,
			AnalyzerBeansConfiguration configuration) {
		super(tjb, windowContext, propertyWidgetFactory, configuration);
		
		_overriddenPropertyWidgets = new HashMap<ConfiguredPropertyDescriptor, PropertyWidget<?>>();
		
		final TransformerBeanDescriptor<SelectFromMapTransformer> descriptor = tjb.getDescriptor();
		final ConfiguredPropertyDescriptor keysProperty = descriptor.getConfiguredProperty("Keys");
		final ConfiguredPropertyDescriptor typesProperty = descriptor.getConfiguredProperty("Types");
		
		KeysAndTypesPropertyWidget propertyWidget = new KeysAndTypesPropertyWidget(keysProperty, typesProperty, tjb);
		_overriddenPropertyWidgets.put(keysProperty, propertyWidget);
		_overriddenPropertyWidgets.put(typesProperty, propertyWidget.getTypesPropertyWidget());
	}

	@Override
	protected PropertyWidget<?> createPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			ConfiguredPropertyDescriptor propertyDescriptor) {
		if (_overriddenPropertyWidgets.containsKey(propertyDescriptor)) {
			return _overriddenPropertyWidgets.get(propertyDescriptor);
		}
		return super.createPropertyWidget(beanJobBuilder, propertyDescriptor);
	}
}
