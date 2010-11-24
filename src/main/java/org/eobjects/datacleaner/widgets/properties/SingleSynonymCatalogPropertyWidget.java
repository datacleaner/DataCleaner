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
import org.eobjects.analyzer.reference.SynonymCatalog;
import org.eobjects.analyzer.util.CollectionUtils;
import org.eobjects.datacleaner.util.WindowManager;

public class SingleSynonymCatalogPropertyWidget extends AbstractPropertyWidget<SynonymCatalog> {

	private static final long serialVersionUID = 1L;
	private final JComboBox _comboBox;
	private final AnalyzerBeansConfiguration _configuration;

	public SingleSynonymCatalogPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		super(beanJobBuilder, propertyDescriptor);

		_configuration = WindowManager.getInstance().getMainWindow().getConfiguration();
		String[] synonymCatalogNames = _configuration.getReferenceDataCatalog().getSynonymCatalogNames();

		if (!propertyDescriptor.isRequired()) {
			synonymCatalogNames = CollectionUtils.array(new String[1], synonymCatalogNames);
		}
		_comboBox = new JComboBox(synonymCatalogNames);
		_comboBox.setEditable(false);

		SynonymCatalog currentValue = (SynonymCatalog) beanJobBuilder.getConfiguredProperty(propertyDescriptor);
		if (currentValue == null) {
			_comboBox.setSelectedItem(null);
		} else {
			_comboBox.setSelectedItem(currentValue.getName());
		}

		_comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fireValueChanged();
			}
		});

		add(_comboBox);
	}

	@Override
	public SynonymCatalog getValue() {
		String synonymCatalogName = (String) _comboBox.getSelectedItem();
		return _configuration.getReferenceDataCatalog().getSynonymCatalog(synonymCatalogName);
	}

	@Override
	protected void setValue(SynonymCatalog value) {
		if (value == null) {
			_comboBox.setSelectedItem(null);
			return;
		}
		_comboBox.setSelectedItem(value.getName());
	}

}
