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
package org.eobjects.datacleaner.panels;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;

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
import org.eobjects.analyzer.reference.TextBasedDictionary;
import org.eobjects.datacleaner.user.DictionaryChangeListener;
import org.eobjects.datacleaner.user.MutableReferenceDataCatalog;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.widgets.DCPopupBubble;
import org.eobjects.datacleaner.widgets.HelpIcon;
import org.eobjects.datacleaner.windows.DatastoreDictionaryDialog;
import org.eobjects.datacleaner.windows.SimpleDictionaryDialog;
import org.eobjects.datacleaner.windows.TextFileDictionaryDialog;
import org.jdesktop.swingx.VerticalLayout;

public class DictionaryListPanel extends DCPanel implements DictionaryChangeListener {

	private static final long serialVersionUID = 1L;

	private static final ImageManager imageManager = ImageManager.getInstance();
	private final AnalyzerBeansConfiguration _configuration;
	private final MutableReferenceDataCatalog _catalog;
	private final DCPanel _listPanel;
	private final DCGlassPane _glassPane;

	public DictionaryListPanel(DCGlassPane glassPane, AnalyzerBeansConfiguration configuration) {
		super(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		_glassPane = glassPane;
		_configuration = configuration;
		_catalog = (MutableReferenceDataCatalog) _configuration.getReferenceDataCatalog();
		_catalog.addDictionaryListener(this);

		_listPanel = new DCPanel();
		_listPanel.setLayout(new VerticalLayout(4));

		updateComponents();

		final DCLabel newDictionariesLabel = DCLabel.dark("Create new dictionary:");
		newDictionariesLabel.setFont(WidgetUtils.FONT_HEADER);

		final DCLabel existingDictionariesLabel = DCLabel.dark("Existing dictionaries:");
		existingDictionariesLabel.setFont(WidgetUtils.FONT_HEADER);

		setLayout(new VerticalLayout(10));
		add(newDictionariesLabel);
		add(createNewDictionariesPanel());
		add(Box.createVerticalStrut(10));
		add(existingDictionariesLabel);
		setBorder(new EmptyBorder(10, 10, 10, 0));
		add(_listPanel);
	}

	private DCPanel createNewDictionariesPanel() {

		final JButton textFileDictionaryButton = createButton("images/datastore-types/csv.png",
				"<html><b>Text file dictionary</b><br/>A dictionary based on a text file on your filesystem.</html>");
		textFileDictionaryButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new TextFileDictionaryDialog(_catalog).setVisible(true);
			}
		});

		final JButton simpleDictionaryButton = createButton("images/actions/edit.png",
				"<html><b>Simple dictionary</b><br/>A dictionary written and stored directly in DataCleaner.</html>");
		simpleDictionaryButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new SimpleDictionaryDialog(_catalog).setVisible(true);
			}
		});

		final JButton datastoreDictionaryButton = createButton("images/model/datastore.png",
				"<html><b>Datastore dictionary</b><br/>Dictionary based on a column in a datastore.</html>");
		datastoreDictionaryButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new DatastoreDictionaryDialog(_catalog, _configuration.getDatastoreCatalog()).setVisible(true);
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
		JButton button = new JButton(imageManager.getImageIcon(imagePath));
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setBorder(null);
		button.setOpaque(false);

		DCPopupBubble popupBubble = new DCPopupBubble(_glassPane, description, 0, 0, imagePath);
		popupBubble.attachTo(button);

		return button;
	}

	private void updateComponents() {
		_listPanel.removeAll();

		final String[] names = _catalog.getDictionaryNames();
		Arrays.sort(names);

		final Icon icon = imageManager.getImageIcon("images/model/dictionary.png");

		for (final String name : names) {
			final Dictionary dictionary = _catalog.getDictionary(name);

			final DCLabel dictLabel = DCLabel
					.dark("<html><b>" + name + "</b><br/>" + getDescription(dictionary) + "</html>");
			dictLabel.setIcon(icon);

			final JButton editButton = WidgetFactory.createSmallButton("images/actions/edit.png");
			editButton.setToolTipText("Edit dictionary");

			if (dictionary instanceof DatastoreDictionary) {
				editButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						DatastoreDictionaryDialog dialog = new DatastoreDictionaryDialog((DatastoreDictionary) dictionary,
								_catalog, _configuration.getDatastoreCatalog());
						dialog.setVisible(true);
					}
				});
			} else if (dictionary instanceof TextBasedDictionary) {
				editButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						TextFileDictionaryDialog dialog = new TextFileDictionaryDialog((TextBasedDictionary) dictionary,
								_catalog);
						dialog.setVisible(true);
					}
				});
			} else if (dictionary instanceof SimpleDictionary) {
				editButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						SimpleDictionaryDialog dialog = new SimpleDictionaryDialog((SimpleDictionary) dictionary, _catalog);
						dialog.setVisible(true);
					}
				});
			} else {
				editButton.setEnabled(false);
			}

			final JButton removeButton = WidgetFactory.createSmallButton("images/actions/remove.png");
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

		updateUI();
	}

	private String getDescription(Dictionary dictionary) {
		if (dictionary instanceof TextBasedDictionary) {
			return ((TextBasedDictionary) dictionary).getFilename();
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
