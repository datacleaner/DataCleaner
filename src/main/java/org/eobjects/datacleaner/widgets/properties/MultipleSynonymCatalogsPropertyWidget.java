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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.reference.SynonymCatalog;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.WindowManager;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;

public class MultipleSynonymCatalogsPropertyWidget extends AbstractPropertyWidget<SynonymCatalog[]> {

	private final ChangeListener CHANGE_LISTENER = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			fireValueChanged();
		}
	};

	private final ActionListener selectAllActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			for (JCheckBox cb : _checkBoxes) {
				cb.setSelected(true);
			}
		}
	};

	private final ActionListener selectNoneActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			for (JCheckBox cb : _checkBoxes) {
				cb.setSelected(false);
			}
		}
	};

	private static final long serialVersionUID = 1L;

	private final AbstractBeanJobBuilder<?, ?, ?> _beanJobBuilder;
	private final AnalyzerBeansConfiguration _configuration;
	private volatile JCheckBox[] _checkBoxes;

	public MultipleSynonymCatalogsPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			ConfiguredPropertyDescriptor propertyDescriptor) {
		super(beanJobBuilder, propertyDescriptor);
		_beanJobBuilder = beanJobBuilder;
		_configuration = WindowManager.getInstance().getMainWindow().getConfiguration();
		setLayout(new VerticalLayout(2));
		updateComponents();
	}

	private void updateComponents() {
		removeAll();
		String[] synonymCatalogNames = _configuration.getReferenceDataCatalog().getSynonymCatalogNames();
		SynonymCatalog[] currentValue = (SynonymCatalog[]) _beanJobBuilder.getConfiguredProperty(getPropertyDescriptor());

		DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new HorizontalLayout(2));

		JButton selectAllButton = new JButton("Select all");
		selectAllButton.addActionListener(selectAllActionListener);
		buttonPanel.add(selectAllButton);

		JButton selectNoneButton = new JButton("Select none");
		selectNoneButton.addActionListener(selectNoneActionListener);
		buttonPanel.add(selectNoneButton);

		add(buttonPanel);

		_checkBoxes = new JCheckBox[synonymCatalogNames.length];
		if (_checkBoxes.length == 0) {
			_checkBoxes = new JCheckBox[1];
			_checkBoxes[0] = new JCheckBox("- no synonym catalogs available -");
			_checkBoxes[0].setOpaque(false);
			_checkBoxes[0].setEnabled(false);
			add(_checkBoxes[0]);
		} else {
			int i = 0;
			for (String synonymCatalogName : synonymCatalogNames) {
				JCheckBox checkBox = new JCheckBox(synonymCatalogName, isEnabled(synonymCatalogName, currentValue));
				checkBox.setOpaque(false);
				checkBox.addChangeListener(CHANGE_LISTENER);
				_checkBoxes[i] = checkBox;
				add(checkBox);
				i++;
			}
		}
		fireValueChanged();
	}

	private boolean isEnabled(String synonymCatalogName, SynonymCatalog[] currentValue) {
		if (currentValue == null || currentValue.length == 0) {
			// set all to true if this is the only inputcolumn property
			BeanDescriptor<?> beanDescriptor = getPropertyDescriptor().getBeanDescriptor();
			if (beanDescriptor.getConfiguredPropertiesForInput().size() == 1) {
				return true;
			}
			return false;
		}
		for (SynonymCatalog syn : currentValue) {
			if (synonymCatalogName.equals(syn.getName())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isSet() {
		for (JCheckBox checkBox : _checkBoxes) {
			if (checkBox.isSelected()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public SynonymCatalog[] getValue() {
		List<SynonymCatalog> result = new ArrayList<SynonymCatalog>();
		for (int i = 0; i < _checkBoxes.length; i++) {
			if (_checkBoxes[i].isSelected()) {
				String synonymCatalogName = _checkBoxes[i].getText();
				result.add(_configuration.getReferenceDataCatalog().getSynonymCatalog(synonymCatalogName));
			}
		}
		return result.toArray(new SynonymCatalog[result.size()]);
	}
	
	@Override
	protected void setValue(SynonymCatalog[] value) {
		updateComponents();
	}
}
