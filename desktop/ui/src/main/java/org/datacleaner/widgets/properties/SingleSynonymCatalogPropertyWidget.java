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

import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.JButton;

import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.reference.SynonymCatalog;
import org.datacleaner.user.MutableReferenceDataCatalog;
import org.datacleaner.user.ReferenceDataChangeListener;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.widgets.DCComboBox;
import org.datacleaner.widgets.ReferenceDataComboBoxListRenderer;
import org.datacleaner.windows.ReferenceDataDialog;
import org.jdesktop.swingx.HorizontalLayout;

public class SingleSynonymCatalogPropertyWidget extends AbstractPropertyWidget<SynonymCatalog>
        implements ReferenceDataChangeListener<SynonymCatalog> {

    private final DCComboBox<SynonymCatalog> _comboBox;
    private final MutableReferenceDataCatalog _referenceDataCatalog;
    private final Provider<ReferenceDataDialog> _referenceDataDialogProvider;

    @Inject
    public SingleSynonymCatalogPropertyWidget(final ConfiguredPropertyDescriptor propertyDescriptor,
            final ComponentBuilder componentBuilder, final MutableReferenceDataCatalog referenceDataCatalog,
            final Provider<ReferenceDataDialog> referenceDataDialogProvider) {
        super(componentBuilder, propertyDescriptor);
        _referenceDataCatalog = referenceDataCatalog;
        _referenceDataDialogProvider = referenceDataDialogProvider;

        _comboBox = new DCComboBox<>();
        _comboBox.setRenderer(new ReferenceDataComboBoxListRenderer());
        _comboBox.setEditable(false);

        if (!propertyDescriptor.isRequired()) {
            _comboBox.addItem(null);
        }

        final String[] synonymCatalogNames = referenceDataCatalog.getSynonymCatalogNames();
        for (final String name : synonymCatalogNames) {
            _comboBox.addItem(referenceDataCatalog.getSynonymCatalog(name));
        }

        final SynonymCatalog currentValue = getCurrentValue();
        _comboBox.setSelectedItem(currentValue);

        _comboBox.addListener(item -> fireValueChanged());

        final JButton dialogButton = WidgetFactory.createSmallButton(IconUtils.MENU_OPTIONS);
        dialogButton.setToolTipText("Configure synonym catalogs");
        dialogButton.addActionListener(e -> {
            final ReferenceDataDialog dialog = _referenceDataDialogProvider.get();
            dialog.selectSynonymsTab();
            dialog.setVisible(true);
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
    protected void setValue(final SynonymCatalog value) {
        _comboBox.setEditable(true);
        _comboBox.setSelectedItem(value);
        _comboBox.setEditable(false);
    }

    @Override
    public void onAdd(final SynonymCatalog synonymCatalog) {
        _comboBox.addItem(synonymCatalog);
    }

    @Override
    public void onRemove(final SynonymCatalog synonymCatalog) {
        _comboBox.removeItem(synonymCatalog);
    }

    @Override
    public void onChange(final SynonymCatalog oldSynonymCatalog, final SynonymCatalog newSynonymCatalog) {
        final SynonymCatalog selectedItem = _comboBox.getSelectedItem();
        _comboBox.removeItem(oldSynonymCatalog);
        _comboBox.addItem(newSynonymCatalog);
        if (selectedItem.equals(oldSynonymCatalog)) {
            _comboBox.setSelectedItem(newSynonymCatalog);
        }
        fireValueChanged();
    }
}
