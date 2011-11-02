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
package org.eobjects.datacleaner.panels;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;

import org.eobjects.analyzer.beans.transform.DatastoreLookupTransformer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.widgets.DCCheckBox;
import org.eobjects.datacleaner.widgets.properties.MultipleInputColumnsPropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;
import org.jdesktop.swingx.JXTextField;

/**
 * Specialized {@link TransformerJobBuilderPresenter} for the
 * {@link DatastoreLookupTransformer}.
 * 
 * @author Kasper Sørensen
 */
public class DatastoreLookupTransformerJobBuilderPresenter extends TransformerJobBuilderPanel {

	private static final long serialVersionUID = 1L;
	
	private final Map<InputColumn<?>, JXTextField> _textFields = new HashMap<InputColumn<?>, JXTextField>();

	public DatastoreLookupTransformerJobBuilderPresenter(TransformerJobBuilder<?> transformerJobBuilder,
			WindowContext windowContext, PropertyWidgetFactory propertyWidgetFactory,
			AnalyzerBeansConfiguration configuration) {
		super(transformerJobBuilder, windowContext, propertyWidgetFactory, configuration);

		assert transformerJobBuilder.getDescriptor().getComponentClass() == DatastoreLookupTransformer.class;
	}

	@Override
	protected PropertyWidget<?> createPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			ConfiguredPropertyDescriptor propertyDescriptor) {
		if ("Condition columns".equals(propertyDescriptor.getName())) {
			return null;
		} else if ("Condition values".equals(propertyDescriptor.getName())) {
			return new MultipleInputColumnsPropertyWidget(beanJobBuilder, propertyDescriptor) {

				private static final long serialVersionUID = 1L;

				@Override
				protected boolean isAllInputColumnsSelectedIfNoValueExist() {
					return false;
				}

				@Override
				protected JComponent decorateCheckBox(final DCCheckBox<InputColumn<?>> checkBox) {
					final JXTextField textField = WidgetFactory.createTextField("Mapped column name");
					checkBox.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							textField.setVisible(checkBox.isSelected());
						}
					});
					textField.setVisible(checkBox.isSelected());
					_textFields.put(checkBox.getValue(), textField);

					final DCPanel panel = new DCPanel();
					panel.setLayout(new BorderLayout());
					panel.add(checkBox, BorderLayout.WEST);
					panel.add(textField, BorderLayout.EAST);
					return panel;
				}
			};
		}
		return super.createPropertyWidget(beanJobBuilder, propertyDescriptor);
	}
}
