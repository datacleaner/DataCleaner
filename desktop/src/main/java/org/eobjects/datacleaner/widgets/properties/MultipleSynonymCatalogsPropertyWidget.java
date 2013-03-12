/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
import org.eobjects.datacleaner.windows.ReferenceDataDialog;

public class MultipleSynonymCatalogsPropertyWidget extends AbstractMultipleCheckboxesPropertyWidget<SynonymCatalog>
		implements SynonymCatalogChangeListener {

	private final MutableReferenceDataCatalog _referenceDataCatalog;
	private final Provider<ReferenceDataDialog> _referenceDataDialogProvider;

	@Inject
	public MultipleSynonymCatalogsPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			ConfiguredPropertyDescriptor propertyDescriptor, MutableReferenceDataCatalog referenceDataCatalog,
			Provider<ReferenceDataDialog> referenceDataDialogProvider) {
		super(beanJobBuilder, propertyDescriptor, SynonymCatalog.class);
		_referenceDataCatalog = referenceDataCatalog;
		_referenceDataDialogProvider = referenceDataDialogProvider;
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
	protected DCPanel createButtonPanel() {
		DCPanel buttonPanel = super.createButtonPanel();

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

		buttonPanel.add(dialogButton);
		return buttonPanel;
	}

	@Override
	protected SynonymCatalog[] getAvailableValues() {
		String[] names = _referenceDataCatalog.getSynonymCatalogNames();
		SynonymCatalog[] result = new SynonymCatalog[names.length];
		for (int i = 0; i < names.length; i++) {
			result[i] = _referenceDataCatalog.getSynonymCatalog(names[i]);
		}
		return result;
	}

	@Override
	protected String getName(SynonymCatalog item) {
		return item.getName();
	}

	@Override
	public void onAdd(SynonymCatalog synonymCatalog) {
		addCheckBox(synonymCatalog, false);
	}

	@Override
	public void onRemove(SynonymCatalog synonymCatalog) {
		removeCheckBox(synonymCatalog);
	}

	@Override
	protected String getNotAvailableText() {
		return "- no synonym catalogs available - ";
	}
}
