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
package org.datacleaner.panels.tokenizer;

import org.datacleaner.beans.transform.TokenizerTransformer;
import org.datacleaner.beans.transform.TokenizerTransformer.TokenTarget;
import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.AbstractBeanJobBuilder;
import org.datacleaner.job.builder.TransformerJobBuilder;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.panels.TransformerJobBuilderPanel;
import org.datacleaner.panels.TransformerJobBuilderPresenter;
import org.datacleaner.widgets.DCComboBox.Listener;
import org.datacleaner.widgets.properties.PropertyWidget;
import org.datacleaner.widgets.properties.PropertyWidgetFactory;
import org.datacleaner.widgets.properties.SingleEnumPropertyWidget;
import org.datacleaner.widgets.properties.SingleNumberPropertyWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Specialized {@link TransformerJobBuilderPresenter} for the
 * {@link TokenizerTransformer}.
 * 
 * @author Kasper Sørensen
 */
class TokenizerJobBuilderPresenter extends TransformerJobBuilderPanel {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(TokenizerJobBuilderPresenter.class);

	private SingleNumberPropertyWidget _numTokensPropertyWidget;
	private SingleEnumPropertyWidget _tokenTargetPropertyWidget;

	public TokenizerJobBuilderPresenter(TransformerJobBuilder<?> transformerJobBuilder, WindowContext windowContext,
			PropertyWidgetFactory propertyWidgetFactory, AnalyzerBeansConfiguration configuration) {
		super(transformerJobBuilder, windowContext, propertyWidgetFactory, configuration);
	}

	@Override
	protected PropertyWidget<?> createPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			ConfiguredPropertyDescriptor propertyDescriptor) {
		PropertyWidget<?> propertyWidget = super.createPropertyWidget(beanJobBuilder, propertyDescriptor);
		String propertyName = propertyDescriptor.getName();
		if ("Token target".equals(propertyName)) {
			_tokenTargetPropertyWidget = (SingleEnumPropertyWidget) propertyWidget;
			_tokenTargetPropertyWidget.addComboListener(new Listener<Enum<?>>() {
				@Override
				public void onItemSelected(Enum<?> item) {
					if (item == TokenTarget.ROWS) {
						if (_numTokensPropertyWidget == null) {
							logger.warn("No property widget for 'num tokens' found!");
						} else if (!_numTokensPropertyWidget.isSet()) {
							_numTokensPropertyWidget.onValueTouched(10000);
						}
					}
				}
			});
		} else if ("Number of tokens".equals(propertyName)) {
			_numTokensPropertyWidget = (SingleNumberPropertyWidget) propertyWidget;
		}
		return propertyWidget;
	}
}
