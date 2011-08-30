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

import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.JButton;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.DictionaryChangeListener;
import org.eobjects.datacleaner.user.MutableReferenceDataCatalog;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.windows.ReferenceDataDialog;

public class MultipleDictionariesPropertyWidget extends AbstractMultipleCheckboxesPropertyWidget<Dictionary> implements
		DictionaryChangeListener {

	private static final long serialVersionUID = 1L;
	private final MutableReferenceDataCatalog _referenceDataCatalog;
	private Provider<ReferenceDataDialog> _referenceDataDialogProvider;

	@Inject
	public MultipleDictionariesPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			ConfiguredPropertyDescriptor propertyDescriptor, MutableReferenceDataCatalog referenceDataCatalog,
			Provider<ReferenceDataDialog> referenceDataDialogProvider) {
		super(beanJobBuilder, propertyDescriptor, Dictionary.class);
		_referenceDataCatalog = referenceDataCatalog;
		_referenceDataDialogProvider = referenceDataDialogProvider;
	}

	@Override
	public void addNotify() {
		super.addNotify();
		_referenceDataCatalog.addDictionaryListener(this);
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
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
		return "- no dictionaries available - ";
	}

}
