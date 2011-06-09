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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.datacleaner.actions.DownloadFilesActionListener;
import org.eobjects.datacleaner.actions.FileDownloadListener;
import org.eobjects.datacleaner.database.DatabaseDriverCatalog;
import org.eobjects.datacleaner.database.DatabaseDriverDescriptor;
import org.eobjects.datacleaner.database.DatabaseDriverState;
import org.eobjects.datacleaner.user.UserDatabaseDriver;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.util.WindowManager;
import org.eobjects.datacleaner.widgets.Alignment;
import org.eobjects.datacleaner.widgets.table.DCTable;
import org.eobjects.datacleaner.windows.AddDatabaseDriverDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the panel in the Options dialog where the user can get an overview
 * and configure database drivers.
 * 
 * @author Kasper Sørensen
 */
public class DatabaseDriversPanel extends DCPanel {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(DatabaseDriversPanel.class);
	private final UserPreferences userPreferences = UserPreferences.getInstance();
	private final ImageManager imageManager = ImageManager.getInstance();
	private final Set<String> _usedDriverClassNames = new HashSet<String>();
	private final DatabaseDriverCatalog _databaseDriverCatalog = new DatabaseDriverCatalog();
	private final WindowManager _windowManager;

	public DatabaseDriversPanel(AnalyzerBeansConfiguration configuration, WindowManager windowManager) {
		super(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		_windowManager = windowManager;
		setLayout(new BorderLayout());

		DatastoreCatalog datastoreCatalog = configuration.getDatastoreCatalog();
		String[] datastoreNames = datastoreCatalog.getDatastoreNames();
		for (String name : datastoreNames) {
			Datastore datastore = datastoreCatalog.getDatastore(name);
			if (datastore instanceof JdbcDatastore) {
				String driverClass = ((JdbcDatastore) datastore).getDriverClass();
				if (driverClass != null) {
					_usedDriverClassNames.add(driverClass);
				}
			}
		}

		updateComponents();
	}

	private void updateComponents() {
		this.removeAll();
		final JToolBar toolBar = WidgetFactory.createToolBar();
		toolBar.add(WidgetFactory.createToolBarSeparator());

		final JButton addDriverButton = new JButton("Add database driver",
				imageManager.getImageIcon("images/actions/add.png"));
		addDriverButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				final JMenu menu = new JMenu("Automatic download and install");
				menu.setIcon(imageManager.getImageIcon("images/actions/download.png"));

				final List<DatabaseDriverDescriptor> drivers = _databaseDriverCatalog.getDatabaseDrivers();
				for (DatabaseDriverDescriptor dd : drivers) {
					final String[] urls = dd.getDownloadUrls();
					if (urls != null && _databaseDriverCatalog.getState(dd) == DatabaseDriverState.NOT_INSTALLED) {
						final JMenuItem downloadAndInstallMenuItem = WidgetFactory.createMenuItem(dd.getDisplayName(),
								dd.getIconImagePath());
						downloadAndInstallMenuItem.addActionListener(createDownloadActionListener(dd));
						menu.add(downloadAndInstallMenuItem);
					}
				}

				if (menu.getMenuComponentCount() == 0) {
					menu.setEnabled(false);
				}

				final JMenuItem localJarFilesMenuItem = WidgetFactory.createMenuItem("Local JAR file(s)...",
						"images/filetypes/archive.png");
				localJarFilesMenuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						AddDatabaseDriverDialog dialog = new AddDatabaseDriverDialog(_databaseDriverCatalog,
								DatabaseDriversPanel.this, _windowManager);
						dialog.setVisible(true);
					}
				});

				final JPopupMenu popup = new JPopupMenu();
				popup.add(menu);
				popup.add(localJarFilesMenuItem);
				popup.show(addDriverButton, 0, addDriverButton.getHeight());
			}
		});
		toolBar.add(addDriverButton);

		final DCTable table = getDatabaseDriverTable();
		this.add(toolBar, BorderLayout.NORTH);
		this.add(table.toPanel(), BorderLayout.CENTER);
	}

	/**
	 * Called by other components in case a driver list update is needed.
	 */
	public void updateDriverList() {
		updateComponents();
	}

	private DCTable getDatabaseDriverTable() {
		final List<DatabaseDriverDescriptor> databaseDrivers = _databaseDriverCatalog.getDatabaseDrivers();
		final TableModel tableModel = new DefaultTableModel(new String[] { "", "Database", "Driver class", "Installed?",
				"Used?" }, databaseDrivers.size());

		final DCTable table = new DCTable(tableModel);

		final Icon validIcon = imageManager.getImageIcon("images/status/valid.png", IconUtils.ICON_SIZE_SMALL);
		final Icon invalidIcon = imageManager.getImageIcon("images/status/error.png", IconUtils.ICON_SIZE_SMALL);

		final int installedCol = 3;
		int row = 0;
		for (final DatabaseDriverDescriptor dd : databaseDrivers) {
			final String driverClassName = dd.getDriverClassName();
			final String displayName = dd.getDisplayName();

			final Icon driverIcon = imageManager.getImageIcon(_databaseDriverCatalog.getIconImagePath(dd),
					IconUtils.ICON_SIZE_SMALL);

			tableModel.setValueAt(driverIcon, row, 0);
			tableModel.setValueAt(displayName, row, 1);
			tableModel.setValueAt(driverClassName, row, 2);
			tableModel.setValueAt("", row, 3);
			tableModel.setValueAt("", row, 4);

			final DatabaseDriverState state = _databaseDriverCatalog.getState(dd);
			if (state == DatabaseDriverState.INSTALLED_WORKING) {
				tableModel.setValueAt(validIcon, row, installedCol);
			} else if (state == DatabaseDriverState.INSTALLED_NOT_WORKING) {
				tableModel.setValueAt(invalidIcon, row, installedCol);
			} else if (state == DatabaseDriverState.NOT_INSTALLED) {
				final String[] downloadUrls = dd.getDownloadUrls();
				if (downloadUrls != null) {
					final DCPanel buttonPanel = new DCPanel();
					buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 4, 0));

					final JButton downloadButton = WidgetFactory.createSmallButton("images/actions/download.png");
					downloadButton.setToolTipText("Download and install the driver for " + dd.getDisplayName());

					downloadButton.addActionListener(createDownloadActionListener(dd));
					buttonPanel.add(downloadButton);

					tableModel.setValueAt(buttonPanel, row, installedCol);
				}
			}

			if (isUsed(driverClassName)) {
				tableModel.setValueAt(validIcon, row, 4);
			}

			row++;
		}

		table.setAlignment(installedCol, Alignment.CENTER);

		table.setRowHeight(IconUtils.ICON_SIZE_SMALL + 4);
		table.getColumn(0).setMaxWidth(IconUtils.ICON_SIZE_SMALL + 4);
		table.getColumn(installedCol).setMaxWidth(84);
		table.getColumn(4).setMaxWidth(70);
		table.setColumnControlVisible(false);
		return table;
	}

	private boolean isUsed(String driverClassName) {
		return _usedDriverClassNames.contains(driverClassName);
	}

	private ActionListener createDownloadActionListener(final DatabaseDriverDescriptor dd) {
		return new DownloadFilesActionListener(dd.getDownloadUrls(), new FileDownloadListener() {
			@Override
			public void onFilesDownloaded(File[] files) {
				final String driverClassName = dd.getDriverClassName();

				logger.info("Registering and loading driver '{}' in files '{}'", driverClassName, files);

				final UserDatabaseDriver userDatabaseDriver = new UserDatabaseDriver(files, driverClassName);
				userPreferences.getDatabaseDrivers().add(userDatabaseDriver);

				try {
					userDatabaseDriver.loadDriver();
				} catch (IllegalStateException e) {
					WidgetUtils.showErrorMessage("Error while loading driver", "Error message: " + e.getMessage(), e);
				}
				updateDriverList();
			}
		}, _windowManager);
	}
}
