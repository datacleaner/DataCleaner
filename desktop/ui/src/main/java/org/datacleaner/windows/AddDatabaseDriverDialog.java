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
package org.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;

import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.database.DatabaseDriverCatalog;
import org.datacleaner.database.DatabaseDriverDescriptor;
import org.datacleaner.database.UserDatabaseDriver;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.panels.DatabaseDriversPanel;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.ExtensionFilter;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.AbstractResourceTextField;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.widgets.DCComboBox;
import org.datacleaner.widgets.DCComboBox.Listener;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.FileSelectionListener;
import org.datacleaner.widgets.FilenameTextField;
import org.jdesktop.swingx.VerticalLayout;

/**
 * Dialog for adding a database driver based on local JAR file(s).
 * 
 * @author Kasper Sørensen
 */
public class AddDatabaseDriverDialog extends AbstractDialog {

	private static final long serialVersionUID = 1L;

	private final ImageManager imageManager = ImageManager.get();
	private final List<FilenameTextField> _filenameTextFields;
	private final DCPanel _filesPanel;
	private final DCComboBox<String> _driverClassNameComboBox;
	private final DatabaseDriverCatalog _databaseDriverCatalog;
	private final JButton _addDriverButton;
	private final DCLabel _statusLabel = DCLabel.bright("");
	private final DatabaseDriversPanel _databaseDriversPanel;
	private final UserPreferences _userPreferences;

	public AddDatabaseDriverDialog(DatabaseDriverCatalog databaseDriverCatalog, DatabaseDriversPanel databaseDriversPanel,
			WindowContext windowContext, UserPreferences userPreferences) {
		super(windowContext);
		_databaseDriverCatalog = databaseDriverCatalog;
		_databaseDriversPanel = databaseDriversPanel;
		_userPreferences = userPreferences;
		_filenameTextFields = new ArrayList<>();
		_filesPanel = new DCPanel();
		_filesPanel.setLayout(new VerticalLayout(4));

		final Set<String> classNames = new TreeSet<String>();
		classNames.add("");
		final List<DatabaseDriverDescriptor> drivers = _databaseDriverCatalog.getDatabaseDrivers();
		for (DatabaseDriverDescriptor dd : drivers) {
			classNames.add(dd.getDriverClassName());
		}
		_driverClassNameComboBox = new DCComboBox<String>(classNames);
		_driverClassNameComboBox.setEditable(true);
		_driverClassNameComboBox.addListener(new Listener<String>() {
			@Override
			public void onItemSelected(String item) {
				updateStatus();
			}
		});

		_addDriverButton = WidgetFactory.createDefaultButton("Add database driver", IconUtils.FILE_ARCHIVE);
		_addDriverButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				UserDatabaseDriver userDatabaseDriver = new UserDatabaseDriver(getDriverFiles(), getDriverClassName());
				_userPreferences.getDatabaseDrivers().add(userDatabaseDriver);

				try {
					userDatabaseDriver.loadDriver();
					_databaseDriversPanel.updateDriverList();
					dispose();
				} catch (IllegalStateException e) {
					WidgetUtils.showErrorMessage("Error while loading driver", "Error message: " + e.getMessage(), e);
				}
			}
		});

		addFilenameTextField();

		updateStatus();
	}

	private File[] getDriverFiles() {
		List<File> files = new ArrayList<File>();
		for (AbstractResourceTextField filenameTextField : _filenameTextFields) {
			final String filename = filenameTextField.getFilename();
			if (!StringUtils.isNullOrEmpty(filename)) {
				files.add(new File(filename));
			}
		}
		return files.toArray(new File[files.size()]);
	}

	private String getDriverClassName() {
		Object selectedItem = _driverClassNameComboBox.getSelectedItem();
		if (selectedItem == null) {
			return null;
		}
		return selectedItem.toString();
	}

	private void addFilenameTextField() {
		final FilenameTextField filenameTextField = new FilenameTextField(_userPreferences.getConfiguredFileDirectory(),
				true);
		filenameTextField.setSelectedFileFilter(new ExtensionFilter("JDBC driver JAR file (.jar)", ".jar"));
		filenameTextField.addFileSelectionListener(new FileSelectionListener() {
			@Override
			public void onSelected(final FilenameTextField filenameTextField, final File file) {
				updateStatus();
			}
		});
		_filenameTextFields.add(filenameTextField);
		_filesPanel.add(filenameTextField);
		_filesPanel.updateUI();
	}

	private void removeFilenameTextField() {
		int index = _filenameTextFields.size() - 1;
		if (index > 0) {
			_filenameTextFields.remove(index);
			_filesPanel.remove(index);
			_filesPanel.updateUI();
		}
	}

	private void updateStatus() {
		final String driverClassName = getDriverClassName();
		if (StringUtils.isNullOrEmpty(driverClassName)) {
			_statusLabel.setText("Please enter or select a driver class name");
			_statusLabel.setIcon(imageManager.getImageIcon(IconUtils.STATUS_ERROR, IconUtils.ICON_SIZE_SMALL));
			_addDriverButton.setEnabled(false);
			return;
		}

		File[] files = getDriverFiles();
		if (files == null || files.length == 0) {
			_statusLabel.setText("Please enter or select one or more driver files");
			_statusLabel.setIcon(imageManager.getImageIcon(IconUtils.STATUS_ERROR, IconUtils.ICON_SIZE_SMALL));
			_addDriverButton.setEnabled(false);
			return;
		}

		for (File file : files) {
			if (!file.exists() || !file.isFile()) {
				_statusLabel.setText("The file " + file.getPath() + " does not exist or is not a valid file");
				_statusLabel.setIcon(imageManager.getImageIcon(IconUtils.STATUS_ERROR, IconUtils.ICON_SIZE_SMALL));
				_addDriverButton.setEnabled(false);
				return;
			}
		}

		_statusLabel.setText("Database driver ready");
		_statusLabel.setIcon(imageManager.getImageIcon(IconUtils.STATUS_VALID, IconUtils.ICON_SIZE_SMALL));
		_addDriverButton.setEnabled(true);
		return;
	}

	@Override
	protected String getBannerTitle() {
		return "Add database driver\nLocal JAR file(s)";
	}

	@Override
	protected int getDialogWidth() {
		return 400;
	}

	@Override
	protected boolean isWindowResizable() {
		return true;
	}

	@Override
	protected JComponent getDialogContent() {
		final DCPanel driverClassOuterPanel = new DCPanel().setTitledBorder("Driver class name");
		driverClassOuterPanel.add(_driverClassNameComboBox);

		final JButton addButton = WidgetFactory.createSmallButton(IconUtils.ACTION_ADD);
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addFilenameTextField();
			}
		});
		final JButton removeButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REMOVE);
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeFilenameTextField();
			}
		});

		final DCPanel filesButtonPanel = new DCPanel();
		filesButtonPanel.setBorder(new EmptyBorder(0, 4, 0, 0));
		filesButtonPanel.setLayout(new VerticalLayout(2));
		filesButtonPanel.add(addButton);
		filesButtonPanel.add(removeButton);

		final DCPanel filesOuterPanel = new DCPanel().setTitledBorder("Driver JAR file(s)");
		filesOuterPanel.setLayout(new BorderLayout());
		filesOuterPanel.add(_filesPanel, BorderLayout.CENTER);
		filesOuterPanel.add(filesButtonPanel, BorderLayout.EAST);

		final DCPanel buttonPanel = DCPanel.flow(Alignment.RIGHT, _addDriverButton);

		final DCPanel mainPanel = new DCPanel();
		mainPanel.setLayout(new VerticalLayout(4));
		mainPanel.add(driverClassOuterPanel);
		mainPanel.add(filesOuterPanel);
		mainPanel.add(buttonPanel);

		final DCPanel outerPanel = new DCPanel();
		outerPanel.setLayout(new BorderLayout());
		outerPanel.add(mainPanel, BorderLayout.CENTER);
		outerPanel.add(WidgetFactory.createStatusBar(_statusLabel), BorderLayout.SOUTH);

		outerPanel.setPreferredSize(400, 350);
		return outerPanel;
	}

	@Override
	public String getWindowTitle() {
		return "Add database driver | Local JAR file(s)";
	}

}
