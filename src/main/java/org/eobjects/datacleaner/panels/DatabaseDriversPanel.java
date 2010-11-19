package org.eobjects.datacleaner.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.datacleaner.actions.DownloadFileActionListener;
import org.eobjects.datacleaner.actions.FileDownloadListener;
import org.eobjects.datacleaner.database.DatabaseDescriptorCatalog;
import org.eobjects.datacleaner.database.DatabaseDriverDescriptor;
import org.eobjects.datacleaner.user.UserDatabaseDriver;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.table.DCTable;
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
	private final DatabaseDescriptorCatalog _databaseDescriptorCatalog = new DatabaseDescriptorCatalog();

	public DatabaseDriversPanel(AnalyzerBeansConfiguration configuration) {
		super(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
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
		JToolBar toolBar = WidgetFactory.createToolBar();
		toolBar.add(WidgetFactory.createToolBarSeparator());

		JButton addDriverButton = WidgetFactory.createButton("Add database driver",
				imageManager.getImageIcon("images/actions/add.png"));

		toolBar.add(addDriverButton);

		DCTable table = getDatabaseDriverTable();
		this.add(toolBar, BorderLayout.NORTH);
		this.add(table.toPanel(), BorderLayout.CENTER);
	}

	private DCTable getDatabaseDriverTable() {
		final List<DatabaseDriverDescriptor> descriptors = _databaseDescriptorCatalog.getDescriptors();
		final TableModel tableModel = new DefaultTableModel(new String[] { "", "Database", "Driver class", "Installed?",
				"Used?" }, descriptors.size());

		final DCTable table = new DCTable(tableModel);

		final Icon validIcon = imageManager.getImageIcon("images/status/valid.png", IconUtils.ICON_SIZE_SMALL);

		int row = 0;
		for (final DatabaseDriverDescriptor dd : descriptors) {
			String driverClassName = dd.getDriverClassName();
			String displayName = dd.getDisplayName();

			Icon driverIcon = imageManager.getImageIcon("images/model/datastore.png", IconUtils.ICON_SIZE_SMALL);

			if (dd.getIconImagePath() != null) {
				driverIcon = imageManager.getImageIcon(dd.getIconImagePath(), IconUtils.ICON_SIZE_SMALL);
			}

			tableModel.setValueAt(driverIcon, row, 0);
			tableModel.setValueAt(displayName, row, 1);
			tableModel.setValueAt(driverClassName, row, 2);
			tableModel.setValueAt("", row, 3);
			tableModel.setValueAt("", row, 4);

			final int installedRow = row;
			final int installedCol = 3;

			if (isInstalled(driverClassName)) {
				tableModel.setValueAt(validIcon, installedRow, installedCol);
			} else {
				String[] downloadUrl = dd.getDownloadUrls();
				if (downloadUrl != null) {
					final DCPanel buttonPanel = new DCPanel();
					buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 4, 0));

					final JButton downloadButton = WidgetFactory.createSmallButton("images/actions/download.png");
					downloadButton.setToolTipText("Download and install the driver for " + dd.getDisplayName());

					downloadButton.addActionListener(new DownloadFileActionListener(dd.getDownloadUrls(),
							new FileDownloadListener() {
								@Override
								public void onFilesDownloaded(File[] files) {
									String driverClassName = dd.getDriverClassName();

									logger.info("Registering and loading driver '{}' in files '{}'", driverClassName, files);

									UserDatabaseDriver userDatabaseDriver = new UserDatabaseDriver(files, driverClassName);
									userPreferences.getDatabaseDrivers().add(userDatabaseDriver);

									userDatabaseDriver.loadDriver();
									updateComponents();
								}
							}));
					buttonPanel.add(downloadButton);

					tableModel.setValueAt(buttonPanel, installedRow, installedCol);
				}
			}

			if (isUsed(driverClassName)) {
				tableModel.setValueAt(validIcon, row, 4);
			}

			row++;
		}

		table.setRowHeight(IconUtils.ICON_SIZE_SMALL + 4);
		table.getColumn(0).setMaxWidth(IconUtils.ICON_SIZE_SMALL + 4);
		table.getColumn(3).setMaxWidth(84);
		table.getColumn(4).setMaxWidth(70);
		table.setColumnControlVisible(false);
		return table;
	}

	private boolean isUsed(String driverClassName) {
		return _usedDriverClassNames.contains(driverClassName);
	}

	private boolean isInstalled(String driverClassName) {
		List<UserDatabaseDriver> drivers = userPreferences.getDatabaseDrivers();
		for (UserDatabaseDriver userDatabaseDriver : drivers) {
			if (userDatabaseDriver.getDriverClassName().equals(driverClassName)) {
				return true;
			}
		}
		try {
			Class.forName(driverClassName);
			return true;
		} catch (ClassNotFoundException e) {
			// do nothing
		}
		return false;
	}

}
