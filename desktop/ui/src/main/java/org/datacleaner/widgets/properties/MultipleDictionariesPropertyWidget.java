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
package org.datacleaner.widgets.properties;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.JButton;

import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.user.DictionaryChangeListener;
import org.datacleaner.user.MutableReferenceDataCatalog;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.windows.ReferenceDataDialog;

public class MultipleDictionariesPropertyWidget extends AbstractMultipleCheckboxesPropertyWidget<Dictionary> implements
		DictionaryChangeListener {

	private final MutableReferenceDataCatalog _referenceDataCatalog;
	private Provider<ReferenceDataDialog> _referenceDataDialogProvider;

	@Inject
	public MultipleDictionariesPropertyWidget(ComponentBuilder componentBuilder,
			ConfiguredPropertyDescriptor propertyDescriptor, MutableReferenceDataCatalog referenceDataCatalog,
			Provider<ReferenceDataDialog> referenceDataDialogProvider) {
		super(componentBuilder, propertyDescriptor, Dictionary.class);
		_referenceDataCatalog = referenceDataCatalog;
		_referenceDataDialogProvider = referenceDataDialogProvider;
	}

	@Override
	public void onPanelAdd() {
		super.onPanelAdd();
		_referenceDataCatalog.addDictionaryListener(this);
	}

	@Override
	public void onPanelRemove() {
		super.onPanelRemove();
		_referenceDataCatalog.removeDictionaryListener(this);
	}

	@Override
	protected DCPanel createButtonPanel() {
		DCPanel buttonPanel = super.createButtonPanel();

		final JButton dialogButton = WidgetFactory.createSmallButton(IconUtils.MENU_OPTIONS);
		dialogButton.setToolTipText("Configure dictionaries");
		dialogButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ReferenceDataDialog dialog = _referenceDataDialogProvider.get();
				dialog.selectDictionariesTab();
				dialog.setVisible(true);
			}
		});

		buttonPanel.add(dialogButton);
		return buttonPanel;
	}

	@Override
	protected Dictionary[] getAvailableValues() {
		String[] names = _referenceDataCatalog.getDictionaryNames();
		Dictionary[] result = new Dictionary[names.length];
		for (int i = 0; i < names.length; i++) {
			result[i] = _referenceDataCatalog.getDictionary(names[i]);
		}
		return result;
	}

	@Override
	protected String getName(Dictionary item) {
		return item.getName();
	}

	@Override
	public void onAdd(Dictionary dictionary) {
		addCheckBox(dictionary, false);
	}

	@Override
	public void onRemove(Dictionary dictionary) {
		removeCheckBox(dictionary);
	}

	@Override
	protected String getNotAvailableText() {
		return "- no dictionaries available -";
	}

}
