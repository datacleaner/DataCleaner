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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.reference.DatastoreDictionary;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.SimpleDictionary;
import org.eobjects.analyzer.reference.TextBasedDictionary;
import org.eobjects.datacleaner.user.DictionaryChangeListener;
import org.eobjects.datacleaner.user.MutableReferenceDataCatalog;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.windows.DatastoreDictionaryDialog;
import org.eobjects.datacleaner.windows.SimpleDictionaryDialog;
import org.eobjects.datacleaner.windows.TextFileDictionaryDialog;

public class DictionaryListPanel extends DCPanel implements DictionaryChangeListener {

	private static final long serialVersionUID = 1L;

	private static final ImageManager imageManager = ImageManager.getInstance();
	private final AnalyzerBeansConfiguration _configuration;
	private final MutableReferenceDataCatalog _catalog;
	private final DCPanel _dictionariesPanel;

	public DictionaryListPanel(AnalyzerBeansConfiguration configuration) {
		super();
		_configuration = configuration;
		_catalog = (MutableReferenceDataCatalog) configuration.getReferenceDataCatalog();
		_catalog.addDictionaryListener(this);
		_dictionariesPanel = new DCPanel();

		JToolBar toolBar = WidgetFactory.createToolBar();

		final JButton addDictionaryMenuItem = new JButton("New dictionary",
				imageManager.getImageIcon("images/actions/new.png"));
		addDictionaryMenuItem.setToolTipText("New dictionary");
		addDictionaryMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JPopupMenu popup = new JPopupMenu();

				final JMenuItem datastoreDictionaryMenuItem = WidgetFactory.createMenuItem("Datastore dictionary",
						imageManager.getImageIcon("images/model/datastore.png", IconUtils.ICON_SIZE_SMALL));
				datastoreDictionaryMenuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						new DatastoreDictionaryDialog(_catalog, _configuration.getDatastoreCatalog()).setVisible(true);
					}
				});

				final JMenuItem textFileDictionaryMenuItem = WidgetFactory.createMenuItem("Text file dictionary",
						imageManager.getImageIcon("images/datastore-types/csv.png", IconUtils.ICON_SIZE_SMALL));
				textFileDictionaryMenuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						new TextFileDictionaryDialog(_catalog).setVisible(true);
					}
				});

				final JMenuItem simpleDictionaryMenuItem = WidgetFactory.createMenuItem("Simple dictionary",
						imageManager.getImageIcon("images/actions/edit.png", IconUtils.ICON_SIZE_SMALL));
				simpleDictionaryMenuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						new SimpleDictionaryDialog(_catalog).setVisible(true);
					}
				});

				popup.add(datastoreDictionaryMenuItem);
				popup.add(textFileDictionaryMenuItem);
				popup.add(simpleDictionaryMenuItem);

				popup.show(addDictionaryMenuItem, 0, addDictionaryMenuItem.getHeight());
			}
		});
		toolBar.add(addDictionaryMenuItem);

		updateComponents();

		setLayout(new BorderLayout());
		add(toolBar, BorderLayout.NORTH);
		add(_dictionariesPanel, BorderLayout.CENTER);
	}

	private void updateComponents() {
		_dictionariesPanel.removeAll();

		String[] names = _catalog.getDictionaryNames();
		Arrays.sort(names);

		int row = 0;
		for (String name : names) {

			final Dictionary dictionary = _catalog.getDictionary(name);

			JButton editButton = WidgetFactory.createSmallButton("images/actions/edit.png");
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

			WidgetUtils.addToGridBag(new JLabel(name), _dictionariesPanel, 0, row);
			WidgetUtils.addToGridBag(editButton, _dictionariesPanel, 1, row);

			row++;
		}

		updateUI();
	}

	@Override
	public void onAdd(Dictionary dictionary) {
		updateComponents();
	}

	@Override
	public void onRemove(Dictionary dictionary) {
		updateComponents();
	}
}
