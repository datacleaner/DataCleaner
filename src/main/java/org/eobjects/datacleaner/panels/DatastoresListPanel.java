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

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.datacleaner.user.DatastoreChangeListener;
import org.eobjects.datacleaner.user.MutableDatastoreCatalog;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.windows.AnalysisJobBuilderWindow;
import org.eobjects.datacleaner.windows.OpenAccessDatabaseDialog;
import org.eobjects.datacleaner.windows.OpenCsvFileDialog;
import org.eobjects.datacleaner.windows.OpenExcelSpreadsheetDialog;

public final class DatastoresListPanel extends DCPanel implements DatastoreChangeListener {

	private static final long serialVersionUID = 1L;

	private AnalyzerBeansConfiguration _configuration;
	private final MutableDatastoreCatalog _catalog;
	private final DCPanel _datastoresPanel;

	public DatastoresListPanel(AnalyzerBeansConfiguration configuration) {
		super();
		_configuration = configuration;
		_catalog = (MutableDatastoreCatalog) configuration.getDatastoreCatalog();
		_catalog.addListener(this);
		_datastoresPanel = new DCPanel();

		ImageManager imageManager = ImageManager.getInstance();

		JToolBar toolBar = WidgetFactory.createToolBar();

		final JButton addDatastoreMenuItem = new JButton("New datastore",
				imageManager.getImageIcon("images/actions/new.png"));
		addDatastoreMenuItem.setToolTipText("New datastore");
		addDatastoreMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JPopupMenu popup = new JPopupMenu();

				ImageManager imageManager = ImageManager.getInstance();

				JMenuItem csvMenuItem = WidgetFactory.createMenuItem("Comma-separated file",
						imageManager.getImageIcon(IconUtils.CSV_IMAGEPATH, IconUtils.ICON_SIZE_SMALL));
				csvMenuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						new OpenCsvFileDialog(_catalog).setVisible(true);
					}
				});

				JMenuItem excelMenuItem = WidgetFactory.createMenuItem("Microsoft Excel spreadsheet",
						imageManager.getImageIcon(IconUtils.EXCEL_IMAGEPATH, IconUtils.ICON_SIZE_SMALL));
				excelMenuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						new OpenExcelSpreadsheetDialog(_catalog).setVisible(true);
					}
				});

				JMenuItem accessMenuItem = WidgetFactory.createMenuItem("Microsoft Access database-file",
						imageManager.getImageIcon(IconUtils.ACCESS_IMAGEPATH, IconUtils.ICON_SIZE_SMALL));
				accessMenuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						new OpenAccessDatabaseDialog(_catalog).setVisible(true);
					}
				});

				// TODO: Not yet functional
				JMenuItem dbaseMenuItem = WidgetFactory.createMenuItem("Dbase database-file",
						imageManager.getImageIcon(IconUtils.DBASE_IMAGEPATH, IconUtils.ICON_SIZE_SMALL));
				dbaseMenuItem.setEnabled(false);

				// TODO: Not yet functional
				JMenuItem odbMenuItem = WidgetFactory.createMenuItem("OpenOffice.org database-file",
						imageManager.getImageIcon(IconUtils.ODB_IMAGEPATH, IconUtils.ICON_SIZE_SMALL));
				odbMenuItem.setEnabled(false);

				// TODO: Not yet functional
				JMenuItem jdbcMenuItem = WidgetFactory.createMenuItem("Database connection",
						imageManager.getImageIcon(IconUtils.GENERIC_DATASTORE_IMAGEPATH, IconUtils.ICON_SIZE_SMALL));
				jdbcMenuItem.setEnabled(false);

				popup.add(jdbcMenuItem);
				popup.add(csvMenuItem);
				popup.add(excelMenuItem);
				popup.add(accessMenuItem);
				popup.add(dbaseMenuItem);
				popup.add(odbMenuItem);

				popup.show(addDatastoreMenuItem, 0, addDatastoreMenuItem.getHeight());
			}
		});
		toolBar.add(addDatastoreMenuItem);

		setLayout(new BorderLayout());
		add(toolBar, BorderLayout.NORTH);
		add(_datastoresPanel, BorderLayout.CENTER);

		updateComponents();

	}

	private void updateComponents() {
		_datastoresPanel.removeAll();

		String[] datastoreNames = _catalog.getDatastoreNames();

		for (int i = 0; i < datastoreNames.length; i++) {
			String name = datastoreNames[i];
			final Datastore datastore = _catalog.getDatastore(name);

			Icon icon = IconUtils.getDatastoreIcon(datastore, IconUtils.ICON_SIZE_SMALL);
			JLabel dsIcon = new JLabel(icon);

			if (name.length() > 15) {
				name = name.substring(0, 12) + "...";
			}
			JLabel dsLabel = new JLabel(name);

			JButton editButton = WidgetFactory.createSmallButton("images/actions/edit.png");
			editButton.setToolTipText("Edit datastore");
			editButton.setEnabled(false);

			JButton jobButton = WidgetFactory.createSmallButton("images/actions/new.png");
			jobButton.setToolTipText("Create job");
			jobButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					AnalysisJobBuilderWindow window = new AnalysisJobBuilderWindow(_configuration, datastore);
					window.setVisible(true);
				}
			});

			WidgetUtils.addToGridBag(dsIcon, _datastoresPanel, 0, i);
			WidgetUtils.addToGridBag(dsLabel, _datastoresPanel, 1, i);
			WidgetUtils.addToGridBag(editButton, _datastoresPanel, 2, i);
			WidgetUtils.addToGridBag(jobButton, _datastoresPanel, 3, i);
		}

		_datastoresPanel.updateUI();
	}

	@Override
	public void removeNotify() {
		_catalog.removeListener(this);
		super.removeNotify();
	}

	@Override
	public void onAdd(Datastore datastore) {
		updateComponents();
	}

	@Override
	public void onRemove(Datastore datastore) {
		updateComponents();
	}
}
