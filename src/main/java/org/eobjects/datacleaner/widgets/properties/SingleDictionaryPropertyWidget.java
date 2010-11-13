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
package org.eobjects.datacleaner.widgets.properties;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.util.CollectionUtils;
import org.eobjects.datacleaner.util.WindowManager;

public class SingleDictionaryPropertyWidget extends AbstractPropertyWidget<Dictionary> {

	private static final long serialVersionUID = 1L;
	private final JComboBox _comboBox;
	private final AnalyzerBeansConfiguration _configuration;

	public SingleDictionaryPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		super(beanJobBuilder, propertyDescriptor);

		_configuration = WindowManager.getInstance().getMainWindow().getConfiguration();
		String[] dictionaryNames = _configuration.getReferenceDataCatalog().getDictionaryNames();

		if (!propertyDescriptor.isRequired()) {
			dictionaryNames = CollectionUtils.array(new String[1], dictionaryNames);
		}
		_comboBox = new JComboBox(dictionaryNames);
		_comboBox.setEditable(false);

		Dictionary currentValue = (Dictionary) beanJobBuilder.getConfiguredProperty(propertyDescriptor);
		if (currentValue != null) {
			_comboBox.setSelectedItem(currentValue.getName());
		}

		_comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fireValueChanged();
			}
		});
	}

	@Override
	public Dictionary getValue() {
		String dictionaryName = (String) _comboBox.getSelectedItem();
		return _configuration.getReferenceDataCatalog().getDictionary(dictionaryName);
	}

	@Override
	protected void setValue(Dictionary value) {
		if (value == null) {
			_comboBox.setSelectedItem(null);
			return;
		}
		
		_comboBox.setSelectedItem(value.getName());
	}
}
