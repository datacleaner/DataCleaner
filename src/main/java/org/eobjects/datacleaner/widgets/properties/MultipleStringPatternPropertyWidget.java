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

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.reference.StringPattern;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.DCConfiguration;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;

public class MultipleStringPatternPropertyWidget extends AbstractPropertyWidget<StringPattern[]> {

	private final ActionListener CHANGE_LISTENER = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			fireValueChanged();
		}
	};

	private final ActionListener selectAllActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			for (JCheckBox cb : _checkBoxes) {
				cb.setSelected(true);
			}
			fireValueChanged();
		}
	};

	private final ActionListener selectNoneActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			for (JCheckBox cb : _checkBoxes) {
				cb.setSelected(false);
			}
			fireValueChanged();
		}
	};

	private static final long serialVersionUID = 1L;

	private final AnalyzerBeansConfiguration _configuration;
	private volatile JCheckBox[] _checkBoxes;

	public MultipleStringPatternPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			ConfiguredPropertyDescriptor propertyDescriptor) {
		super(beanJobBuilder, propertyDescriptor);
		_configuration = DCConfiguration.get();
		setLayout(new VerticalLayout(2));
		updateComponents();
	}

	private void updateComponents() {
		removeAll();
		String[] stringPatternNames = _configuration.getReferenceDataCatalog().getStringPatternNames();
		StringPattern[] currentValue = (StringPattern[]) getBeanJobBuilder().getConfiguredProperty(getPropertyDescriptor());

		DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new HorizontalLayout(2));

		JButton selectAllButton = new JButton("Select all");
		selectAllButton.addActionListener(selectAllActionListener);
		buttonPanel.add(selectAllButton);

		JButton selectNoneButton = new JButton("Select none");
		selectNoneButton.addActionListener(selectNoneActionListener);
		buttonPanel.add(selectNoneButton);

		add(buttonPanel);

		_checkBoxes = new JCheckBox[stringPatternNames.length];
		if (_checkBoxes.length == 0) {
			_checkBoxes = new JCheckBox[1];
			_checkBoxes[0] = new JCheckBox("- no string patterns available -");
			_checkBoxes[0].setOpaque(false);
			_checkBoxes[0].setEnabled(false);
			add(_checkBoxes[0]);
		} else {
			int i = 0;
			for (String stringPatternName : stringPatternNames) {
				JCheckBox checkBox = new JCheckBox(stringPatternName, isEnabled(stringPatternName, currentValue));
				checkBox.setOpaque(false);
				checkBox.addActionListener(CHANGE_LISTENER);
				_checkBoxes[i] = checkBox;
				add(checkBox);
				i++;
			}
		}
		fireValueChanged();
	}

	private boolean isEnabled(String stringPatternName, StringPattern[] currentValue) {
		if (currentValue == null || currentValue.length == 0) {
			return false;
		}
		for (StringPattern stringPattern : currentValue) {
			if (stringPatternName.equals(stringPattern.getName())) {
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
	public StringPattern[] getValue() {
		List<StringPattern> result = new ArrayList<StringPattern>();
		for (int i = 0; i < _checkBoxes.length; i++) {
			if (_checkBoxes[i].isSelected()) {
				String stringPatternName = _checkBoxes[i].getText();
				result.add(_configuration.getReferenceDataCatalog().getStringPattern(stringPatternName));
			}
		}
		return result.toArray(new StringPattern[result.size()]);
	}

	@Override
	protected void setValue(StringPattern[] value) {
		updateComponents();
	}
}
