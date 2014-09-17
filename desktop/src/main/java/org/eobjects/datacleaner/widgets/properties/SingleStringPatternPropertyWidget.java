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
package org.eobjects.datacleaner.widgets.properties;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.JButton;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.reference.StringPattern;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.MutableReferenceDataCatalog;
import org.eobjects.datacleaner.user.StringPatternChangeListener;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.widgets.DCComboBox;
import org.eobjects.datacleaner.widgets.DCComboBox.Listener;
import org.eobjects.datacleaner.widgets.ReferenceDataComboBoxListRenderer;
import org.eobjects.datacleaner.windows.ReferenceDataDialog;
import org.jdesktop.swingx.HorizontalLayout;

public class SingleStringPatternPropertyWidget extends AbstractPropertyWidget<StringPattern> implements
		StringPatternChangeListener {

	private final DCComboBox<StringPattern> _comboBox;
	private final MutableReferenceDataCatalog _referenceDataCatalog;
	private final Provider<ReferenceDataDialog> _referenceDataDialogProvider;

	@Inject
	public SingleStringPatternPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder, MutableReferenceDataCatalog referenceDataCatalog,
			Provider<ReferenceDataDialog> referenceDataDialogProvider) {
		super(beanJobBuilder, propertyDescriptor);
		_referenceDataCatalog = referenceDataCatalog;
		_referenceDataDialogProvider = referenceDataDialogProvider;

		_comboBox = new DCComboBox<StringPattern>();
		_comboBox.setRenderer(new ReferenceDataComboBoxListRenderer());
		_comboBox.setEditable(false);

		if (!propertyDescriptor.isRequired()) {
			_comboBox.addItem(null);
		}
		final String[] stringPatternNames = referenceDataCatalog.getStringPatternNames();
		for (String name : stringPatternNames) {
			_comboBox.addItem(referenceDataCatalog.getStringPattern(name));
		}

		StringPattern currentValue = getCurrentValue();
		_comboBox.setSelectedItem(currentValue);

		_comboBox.addListener(new Listener<StringPattern>() {
			@Override
			public void onItemSelected(StringPattern item) {
				fireValueChanged();
			}
		});

		final JButton dialogButton = WidgetFactory.createSmallButton(IconUtils.MENU_OPTIONS);
		dialogButton.setToolTipText("Configure synonym catalogs");
		dialogButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ReferenceDataDialog dialog = _referenceDataDialogProvider.get();
				dialog.selectStringPatternsTab();
				dialog.setVisible(true);
			}
		});

		final DCPanel outerPanel = new DCPanel();
		outerPanel.setLayout(new HorizontalLayout(2));
		outerPanel.add(_comboBox);
		outerPanel.add(dialogButton);

		add(outerPanel);
	}

	@Override
	public void onPanelAdd() {
		super.onPanelAdd();
		_referenceDataCatalog.addStringPatternListener(this);
	}

	@Override
	public void onPanelRemove() {
		super.onPanelRemove();
		_referenceDataCatalog.removeStringPatternListener(this);
	}

	@Override
	public StringPattern getValue() {
		return (StringPattern) _comboBox.getSelectedItem();
	}

	@Override
	protected void setValue(StringPattern value) {
		_comboBox.setEditable(true);
		_comboBox.setSelectedItem(value);
		_comboBox.setEditable(false);
	}

	@Override
	public void onAdd(StringPattern stringPattern) {
		_comboBox.addItem(stringPattern);
	}

	@Override
	public void onRemove(StringPattern stringPattern) {
		_comboBox.removeItem(stringPattern);
	}
}
