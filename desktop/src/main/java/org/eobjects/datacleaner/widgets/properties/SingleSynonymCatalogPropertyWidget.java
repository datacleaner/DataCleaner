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
import org.eobjects.analyzer.reference.SynonymCatalog;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.MutableReferenceDataCatalog;
import org.eobjects.datacleaner.user.SynonymCatalogChangeListener;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.widgets.DCComboBox;
import org.eobjects.datacleaner.widgets.DCComboBox.Listener;
import org.eobjects.datacleaner.widgets.ReferenceDataComboBoxListRenderer;
import org.eobjects.datacleaner.windows.ReferenceDataDialog;
import org.jdesktop.swingx.HorizontalLayout;

public class SingleSynonymCatalogPropertyWidget extends AbstractPropertyWidget<SynonymCatalog> implements
		SynonymCatalogChangeListener {

	private final DCComboBox<SynonymCatalog> _comboBox;
	private final MutableReferenceDataCatalog _referenceDataCatalog;
	private final Provider<ReferenceDataDialog> _referenceDataDialogProvider;

	@Inject
	public SingleSynonymCatalogPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder, MutableReferenceDataCatalog referenceDataCatalog,
			Provider<ReferenceDataDialog> referenceDataDialogProvider) {
		super(beanJobBuilder, propertyDescriptor);
		_referenceDataCatalog = referenceDataCatalog;
		_referenceDataDialogProvider = referenceDataDialogProvider;

		_comboBox = new DCComboBox<SynonymCatalog>();
		_comboBox.setRenderer(new ReferenceDataComboBoxListRenderer());
		_comboBox.setEditable(false);

		if (!propertyDescriptor.isRequired()) {
			_comboBox.addItem(null);
		}

		final String[] synonymCatalogNames = referenceDataCatalog.getSynonymCatalogNames();
		for (String name : synonymCatalogNames) {
			_comboBox.addItem(referenceDataCatalog.getSynonymCatalog(name));
		}

		SynonymCatalog currentValue = getCurrentValue();
		_comboBox.setSelectedItem(currentValue);

		_comboBox.addListener(new Listener<SynonymCatalog>() {
			@Override
			public void onItemSelected(SynonymCatalog item) {
				fireValueChanged();
			}
		});

		final JButton dialogButton = WidgetFactory.createSmallButton(IconUtils.MENU_OPTIONS);
		dialogButton.setToolTipText("Configure synonym catalogs");
		dialogButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ReferenceDataDialog dialog = _referenceDataDialogProvider.get();
				dialog.selectSynonymsTab();
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
		_referenceDataCatalog.addSynonymCatalogListener(this);
	}

	@Override
	public void onPanelRemove() {
		super.onPanelRemove();
		_referenceDataCatalog.removeSynonymCatalogListener(this);
	}

	@Override
	public SynonymCatalog getValue() {
		return (SynonymCatalog) _comboBox.getSelectedItem();
	}

	@Override
	protected void setValue(SynonymCatalog value) {
		_comboBox.setEditable(true);
		_comboBox.setSelectedItem(value);
		_comboBox.setEditable(false);
	}

	@Override
	public void onAdd(SynonymCatalog synonymCatalog) {
		_comboBox.addItem(synonymCatalog);
	}

	@Override
	public void onRemove(SynonymCatalog synonymCatalog) {
		_comboBox.removeItem(synonymCatalog);
	}

}
