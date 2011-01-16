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

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.reference.SimpleSynonymCatalog;
import org.eobjects.analyzer.reference.SynonymCatalog;
import org.eobjects.analyzer.reference.TextBasedSynonymCatalog;
import org.eobjects.datacleaner.user.MutableReferenceDataCatalog;
import org.eobjects.datacleaner.user.SynonymCatalogChangeListener;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.windows.DatastoreSynonymCatalogDialog;
import org.eobjects.datacleaner.windows.TextFileSynonymCatalogDialog;

public final class SynonymCatalogListPanel extends DCPanel implements SynonymCatalogChangeListener {

	private static final long serialVersionUID = 1L;

	private static final ImageManager imageManager = ImageManager.getInstance();
	private final MutableReferenceDataCatalog _catalog;
	private final DatastoreCatalog _datastoreCatalog;
	private final DCPanel _listPanel;

	public SynonymCatalogListPanel(AnalyzerBeansConfiguration configuration) {
		super(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		_catalog = (MutableReferenceDataCatalog) configuration.getReferenceDataCatalog();
		_catalog.addSynonymCatalogListener(this);
		_listPanel = new DCPanel();
		_datastoreCatalog = configuration.getDatastoreCatalog();

		JToolBar toolBar = WidgetFactory.createToolBar();

		final JButton addButton = new JButton("New synonym catalog", imageManager.getImageIcon("images/actions/new.png"));
		addButton.setToolTipText("New synonym catalog");
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JPopupMenu popup = new JPopupMenu();

				final JMenuItem textFileSynonymCatalogMenuItem = WidgetFactory.createMenuItem("Text file synonym catalog",
						imageManager.getImageIcon("images/datastore-types/csv.png", IconUtils.ICON_SIZE_SMALL));
				textFileSynonymCatalogMenuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						new TextFileSynonymCatalogDialog(_catalog).setVisible(true);
					}
				});

				final JMenuItem dataStoreSynonymCatalogMenuItem = WidgetFactory.createMenuItem("Datastore synonym catalog",
						imageManager.getImageIcon("images/datastore-types/csv.png", IconUtils.ICON_SIZE_SMALL));
				dataStoreSynonymCatalogMenuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						new DatastoreSynonymCatalogDialog(_catalog, _datastoreCatalog).setVisible(true);
						// new
						// TextFileSynonymCatalogDialog(_catalog).setVisible(true);
						// ToDO.. create a new dailog for data store
					}
				});

				final JMenuItem simpleSynonymCatalogMenuItem = WidgetFactory.createMenuItem("Simple synonym catalog",
						imageManager.getImageIcon("images/actions/edit.png", IconUtils.ICON_SIZE_SMALL));
				simpleSynonymCatalogMenuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO
					}
				});
				simpleSynonymCatalogMenuItem.setEnabled(false);
				popup.add(textFileSynonymCatalogMenuItem);
				popup.add(simpleSynonymCatalogMenuItem);
				popup.add(dataStoreSynonymCatalogMenuItem);

				popup.show(addButton, 0, addButton.getHeight());
			}
		});
		toolBar.add(addButton);

		updateComponents();

		setLayout(new BorderLayout());
		add(toolBar, BorderLayout.NORTH);
		add(_listPanel, BorderLayout.CENTER);
	}

	private void updateComponents() {
		_listPanel.removeAll();

		String[] names = _catalog.getSynonymCatalogNames();
		Arrays.sort(names);

		Icon icon = imageManager.getImageIcon("images/model/synonym.png", IconUtils.ICON_SIZE_SMALL);

		int row = 0;
		for (final String name : names) {

			final SynonymCatalog sc = _catalog.getSynonymCatalog(name);

			final JLabel scLabel = new JLabel(name, icon, JLabel.LEFT);

			final JButton editButton = WidgetFactory.createSmallButton("images/actions/edit.png");
			editButton.setToolTipText("Edit synonym catalog");

			if (sc instanceof TextBasedSynonymCatalog) {
				editButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						new TextFileSynonymCatalogDialog((TextBasedSynonymCatalog) sc, _catalog).setVisible(true);
					}
				});
			} else if (sc instanceof SimpleSynonymCatalog) {
				editButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO
					}
				});
			} else {
				editButton.setEnabled(false);
			}

			final JButton removeButton = WidgetFactory.createSmallButton("images/actions/remove.png");
			removeButton.setToolTipText("Remove synonym catalog");
			removeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int result = JOptionPane.showConfirmDialog(SynonymCatalogListPanel.this,
							"Are you sure you wish to remove the synonym catalog '" + name + "'?", "Confirm remove",
							JOptionPane.YES_NO_OPTION);
					if (result == JOptionPane.YES_OPTION) {
						_catalog.removeSynonymCatalog(sc);
					}
				}
			});

			WidgetUtils.addToGridBag(scLabel, _listPanel, 0, row, 1.0, 0.0);

			if (_catalog.isSynonymCatalogMutable(name)) {
				WidgetUtils.addToGridBag(editButton, _listPanel, 1, row);
				WidgetUtils.addToGridBag(removeButton, _listPanel, 2, row);
			}

			row++;
		}

		updateUI();
	}

	@Override
	public void onAdd(SynonymCatalog synonymCatalog) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateComponents();
			}
		});
	}

	@Override
	public void onRemove(SynonymCatalog synonymCatalog) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateComponents();
			}
		});
	}
}
