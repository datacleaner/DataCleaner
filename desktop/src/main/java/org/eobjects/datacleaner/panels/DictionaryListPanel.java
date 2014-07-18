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
package org.eobjects.datacleaner.panels;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;

import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.reference.DatastoreDictionary;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.SimpleDictionary;
import org.eobjects.analyzer.reference.TextFileDictionary;
import org.eobjects.datacleaner.guice.InjectorBuilder;
import org.eobjects.datacleaner.user.DictionaryChangeListener;
import org.eobjects.datacleaner.user.MutableReferenceDataCatalog;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.widgets.DCPopupBubble;
import org.eobjects.datacleaner.widgets.HelpIcon;
import org.eobjects.datacleaner.windows.DatastoreDictionaryDialog;
import org.eobjects.datacleaner.windows.ReferenceDataDialog;
import org.eobjects.datacleaner.windows.SimpleDictionaryDialog;
import org.eobjects.datacleaner.windows.TextFileDictionaryDialog;
import org.jdesktop.swingx.VerticalLayout;

import com.google.inject.Injector;

public class DictionaryListPanel extends DCPanel implements DictionaryChangeListener {

	private static final long serialVersionUID = 1L;

	private static final ImageManager imageManager = ImageManager.get();
	private final AnalyzerBeansConfiguration _configuration;
	private final MutableReferenceDataCatalog _catalog;
	private final DCPanel _listPanel;
	private final DCGlassPane _glassPane;
	private final InjectorBuilder _injectorBuilder;

	@Inject
	protected DictionaryListPanel(DCGlassPane glassPane, AnalyzerBeansConfiguration configuration,
			InjectorBuilder injectorBuilder) {
		super(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
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

	private DCPanel createNewDictionariesPanel() {

		final JButton textFileDictionaryButton = createButton(IconUtils.DICTIONARY_TEXTFILE_IMAGEPATH,
				"<html><b>Text file dictionary</b><br/>A dictionary based on a text file on your filesystem.</html>");
		textFileDictionaryButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Injector injector = _injectorBuilder.with(TextFileDictionary.class, null).createInjector();
				TextFileDictionaryDialog dialog = injector.getInstance(TextFileDictionaryDialog.class);
				dialog.open();
			}
		});

		final JButton simpleDictionaryButton = createButton(IconUtils.DICTIONARY_SIMPLE_IMAGEPATH,
				"<html><b>Simple dictionary</b><br/>A dictionary written and stored directly in DataCleaner.</html>");
		simpleDictionaryButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Injector injector = _injectorBuilder.with(SimpleDictionary.class, null).createInjector();
				SimpleDictionaryDialog dialog = injector.getInstance(SimpleDictionaryDialog.class);
				dialog.open();
			}
		});

		final JButton datastoreDictionaryButton = createButton(IconUtils.DICTIONARY_DATASTORE_IMAGEPATH,
				"<html><b>Datastore dictionary</b><br/>Dictionary based on a column in a datastore.</html>");
		datastoreDictionaryButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Injector injector = _injectorBuilder.with(DatastoreDictionary.class, null).createInjector();
				DatastoreDictionaryDialog dialog = injector.getInstance(DatastoreDictionaryDialog.class);
				dialog.open();
			}
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

	private JButton createButton(String imagePath, String description) {
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

			final DCLabel dictLabel = DCLabel
					.dark("<html><b>" + name + "</b><br/>" + getDescription(dictionary) + "</html>");
			dictLabel.setIcon(icon);
			dictLabel.setMaximumWidth(ReferenceDataDialog.REFERENCE_DATA_ITEM_MAX_WIDTH);

			final JButton editButton = WidgetFactory.createSmallButton("images/actions/edit.png");
			editButton.setToolTipText("Edit dictionary");

			if (dictionary instanceof DatastoreDictionary) {
				editButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Injector injector = _injectorBuilder.with(DatastoreDictionary.class, dictionary).createInjector();
						DatastoreDictionaryDialog dialog = injector.getInstance(DatastoreDictionaryDialog.class);
						dialog.open();
					}
				});
			} else if (dictionary instanceof TextFileDictionary) {
				editButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Injector injector = _injectorBuilder.with(TextFileDictionary.class, dictionary).createInjector();
						TextFileDictionaryDialog dialog = injector.getInstance(TextFileDictionaryDialog.class);
						dialog.open();
					}
				});
			} else if (dictionary instanceof SimpleDictionary) {
				editButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Injector injector = _injectorBuilder.with(SimpleDictionary.class, dictionary).createInjector();
						SimpleDictionaryDialog dialog = injector.getInstance(SimpleDictionaryDialog.class);
						dialog.open();
					}
				});
			} else {
				editButton.setEnabled(false);
			}

			final JButton removeButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REMOVE);
			removeButton.setToolTipText("Remove dictionary");
			removeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int result = JOptionPane.showConfirmDialog(DictionaryListPanel.this,
							"Are you sure you wish to remove the dictionary '" + name + "'?", "Confirm remove",
							JOptionPane.YES_NO_OPTION);
					if (result == JOptionPane.YES_OPTION) {
						_catalog.removeDictionary(dictionary);
					}
				}
			});

			if (!_catalog.isDictionaryMutable(name)) {
				editButton.setEnabled(false);
				removeButton.setEnabled(false);
			}

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

	private static String getDescription(Dictionary dictionary) {
		if (dictionary.getDescription() != null) {
			return dictionary.getDescription();
		}
		if (dictionary instanceof TextFileDictionary) {
			return ((TextFileDictionary) dictionary).getFilename();
		} else if (dictionary instanceof DatastoreDictionary) {
			DatastoreDictionary datastoreDictionary = (DatastoreDictionary) dictionary;
			return datastoreDictionary.getDatastoreName() + ": " + datastoreDictionary.getQualifiedColumnName();
		} else if (dictionary instanceof SimpleDictionary) {
			Collection<String> values = dictionary.getValues().getValues();
			StringBuilder sb = new StringBuilder();
			for (String value : values) {
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

	@Override
	public void onAdd(Dictionary dictionary) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateComponents();
			}
		});
	}

	@Override
	public void onRemove(Dictionary dictionary) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateComponents();
			}
		});
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		_catalog.removeDictionaryListener(this);
	}
}
