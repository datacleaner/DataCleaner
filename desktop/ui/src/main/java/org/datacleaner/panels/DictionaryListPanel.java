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
package org.datacleaner.panels;

import java.awt.GridBagConstraints;
import java.util.Arrays;
import java.util.Collection;

import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.guice.InjectorBuilder;
import org.datacleaner.reference.DatastoreDictionary;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.reference.SimpleDictionary;
import org.datacleaner.reference.TextFileDictionary;
import org.datacleaner.user.MutableReferenceDataCatalog;
import org.datacleaner.user.ReferenceDataChangeListener;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.DCPopupBubble;
import org.datacleaner.widgets.HelpIcon;
import org.datacleaner.windows.DatastoreDictionaryDialog;
import org.datacleaner.windows.ReferenceDataDialog;
import org.datacleaner.windows.SimpleDictionaryDialog;
import org.datacleaner.windows.TextFileDictionaryDialog;
import org.jdesktop.swingx.VerticalLayout;

import com.google.inject.Injector;

public class DictionaryListPanel extends DCPanel implements ReferenceDataChangeListener<Dictionary> {

    private static final long serialVersionUID = 1L;

    private static final ImageManager imageManager = ImageManager.get();
    private final DataCleanerConfiguration _configuration;
    private final MutableReferenceDataCatalog _catalog;
    private final DCPanel _listPanel;
    private final DCGlassPane _glassPane;
    private final InjectorBuilder _injectorBuilder;

    @Inject
    protected DictionaryListPanel(final DCGlassPane glassPane, final DataCleanerConfiguration configuration,
            final InjectorBuilder injectorBuilder) {
        super(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        _glassPane = glassPane;
        _configuration = configuration;
        _injectorBuilder = injectorBuilder;
        _catalog = (MutableReferenceDataCatalog) _configuration.getReferenceDataCatalog();
        _catalog.addDictionaryListener(this);

        _listPanel = new DCPanel();
        _listPanel.setLayout(new VerticalLayout(4));

        updateComponents();

        final DCLabel newDictionariesLabel = DCLabel.dark("Create new dictionary:");
        newDictionariesLabel.setFont(WidgetUtils.FONT_HEADER1);

        final DCLabel existingDictionariesLabel = DCLabel.dark("Existing dictionaries:");
        existingDictionariesLabel.setFont(WidgetUtils.FONT_HEADER1);

        setLayout(new VerticalLayout(10));
        add(newDictionariesLabel);
        add(createNewDictionariesPanel());
        add(Box.createVerticalStrut(10));
        add(existingDictionariesLabel);
        setBorder(new EmptyBorder(10, 10, 10, 0));
        add(_listPanel);
    }

    private static String getDescription(final Dictionary dictionary) {
        if (dictionary.getDescription() != null) {
            return dictionary.getDescription();
        }
        if (dictionary instanceof TextFileDictionary) {
            return ((TextFileDictionary) dictionary).getFilename();
        } else if (dictionary instanceof DatastoreDictionary) {
            final DatastoreDictionary datastoreDictionary = (DatastoreDictionary) dictionary;
            return datastoreDictionary.getDatastoreName() + ": " + datastoreDictionary.getQualifiedColumnName();
        } else if (dictionary instanceof SimpleDictionary) {
            final SimpleDictionary simpleDictionary = (SimpleDictionary) dictionary;
            final Collection<String> values = simpleDictionary.getValueSet();
            final StringBuilder sb = new StringBuilder();
            for (final String value : values) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(value);
                if (sb.length() > 20) {
                    break;
                }
            }
            return sb.toString();
        }
        return "";
    }

    private DCPanel createNewDictionariesPanel() {

        final JButton textFileDictionaryButton = createButton(IconUtils.DICTIONARY_TEXTFILE_IMAGEPATH,
                "<html><b>Text file dictionary</b><br/>A dictionary based on a text file on your filesystem.</html>");
        textFileDictionaryButton.addActionListener(e -> {
            final Injector injector = _injectorBuilder.with(TextFileDictionary.class, null).createInjector();
            final TextFileDictionaryDialog dialog = injector.getInstance(TextFileDictionaryDialog.class);
            dialog.open();
        });

        final JButton simpleDictionaryButton = createButton(IconUtils.DICTIONARY_SIMPLE_IMAGEPATH,
                "<html><b>Simple dictionary</b><br/>A dictionary written and stored directly in DataCleaner.</html>");
        simpleDictionaryButton.addActionListener(e -> {
            final Injector injector = _injectorBuilder.with(SimpleDictionary.class, null).createInjector();
            final SimpleDictionaryDialog dialog = injector.getInstance(SimpleDictionaryDialog.class);
            dialog.open();
        });

        final JButton datastoreDictionaryButton = createButton(IconUtils.DICTIONARY_DATASTORE_IMAGEPATH,
                "<html><b>Datastore dictionary</b><br/>Dictionary based on a column in a datastore.</html>");
        datastoreDictionaryButton.addActionListener(e -> {
            final Injector injector = _injectorBuilder.with(DatastoreDictionary.class, null).createInjector();
            final DatastoreDictionaryDialog dialog = injector.getInstance(DatastoreDictionaryDialog.class);
            dialog.open();
        });

        final HelpIcon helpIcon = new HelpIcon("<b>Dictionaries</b><br>"
                + "A dictionary is a set of values that grouped together represent a named set of values,"
                + "for example valid values or blacklisted values for a given type of data.<br>"
                + "Dictionaries can be used throughout DataCleaner for filtering, matching and more.");

        final DCPanel panel = DCPanel.flow(textFileDictionaryButton, simpleDictionaryButton, datastoreDictionaryButton,
                Box.createHorizontalStrut(100), helpIcon);
        panel.setBorder(WidgetUtils.BORDER_LIST_ITEM);
        return panel;
    }

    private JButton createButton(final String imagePath, final String description) {
        final JButton button = WidgetFactory.createImageButton(imageManager.getImageIcon(imagePath));

        final DCPopupBubble popupBubble = new DCPopupBubble(_glassPane, description, 0, 0, imagePath);
        popupBubble.attachTo(button);

        return button;
    }

    private void updateComponents() {
        _listPanel.removeAll();

        final String[] names = _catalog.getDictionaryNames();
        Arrays.sort(names);

        final Icon icon = imageManager.getImageIcon(IconUtils.DICTIONARY_IMAGEPATH);

        for (final String name : names) {
            final Dictionary dictionary = _catalog.getDictionary(name);

            final DCLabel dictLabel =
                    DCLabel.dark("<html><b>" + name + "</b><br/>" + getDescription(dictionary) + "</html>");
            dictLabel.setIcon(icon);
            dictLabel.setMaximumWidth(ReferenceDataDialog.REFERENCE_DATA_ITEM_MAX_WIDTH);

            final JButton editButton = WidgetFactory.createSmallButton(IconUtils.ACTION_EDIT);
            editButton.setToolTipText("Edit dictionary");

            if (dictionary instanceof DatastoreDictionary) {
                editButton.addActionListener(e -> {
                    final Injector injector =
                            _injectorBuilder.with(DatastoreDictionary.class, dictionary).createInjector();
                    final DatastoreDictionaryDialog dialog = injector.getInstance(DatastoreDictionaryDialog.class);
                    dialog.open();
                });
            } else if (dictionary instanceof TextFileDictionary) {
                editButton.addActionListener(e -> {
                    final Injector injector =
                            _injectorBuilder.with(TextFileDictionary.class, dictionary).createInjector();
                    final TextFileDictionaryDialog dialog = injector.getInstance(TextFileDictionaryDialog.class);
                    dialog.open();
                });
            } else if (dictionary instanceof SimpleDictionary) {
                editButton.addActionListener(e -> {
                    final Injector injector =
                            _injectorBuilder.with(SimpleDictionary.class, dictionary).createInjector();
                    final SimpleDictionaryDialog dialog = injector.getInstance(SimpleDictionaryDialog.class);
                    dialog.open();
                });
            } else {
                editButton.setEnabled(false);
            }

            final JButton removeButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REMOVE_DARK);
            removeButton.setToolTipText("Remove dictionary");
            removeButton.addActionListener(e -> {
                final int result = JOptionPane.showConfirmDialog(DictionaryListPanel.this,
                        "Are you sure you wish to remove the dictionary '" + name + "'?", "Confirm remove",
                        JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    _catalog.removeDictionary(dictionary);
                }
            });

            final DCPanel dictionaryPanel = new DCPanel();
            dictionaryPanel.setBorder(WidgetUtils.BORDER_LIST_ITEM);
            WidgetUtils.addToGridBag(dictLabel, dictionaryPanel, 0, 0, 1.0, 0.0);
            WidgetUtils.addToGridBag(editButton, dictionaryPanel, 1, 0, GridBagConstraints.EAST);
            WidgetUtils.addToGridBag(removeButton, dictionaryPanel, 2, 0, GridBagConstraints.EAST);
            _listPanel.add(dictionaryPanel);
        }

        if (names.length == 0) {
            _listPanel.add(DCLabel.dark("(none)"));
        }

        updateUI();
    }

    @Override
    public void onAdd(final Dictionary dictionary) {
        SwingUtilities.invokeLater(this::updateComponents);
    }

    @Override
    public void onRemove(final Dictionary dictionary) {
        SwingUtilities.invokeLater(this::updateComponents);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        _catalog.removeDictionaryListener(this);
    }

    @Override
    public void onChange(final Dictionary oldPattern, final Dictionary newPattern) {
        SwingUtilities.invokeLater(this::updateComponents);
    }
}
