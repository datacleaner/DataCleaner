/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.datacleaner.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import dk.eobjects.datacleaner.gui.DataCleanerGui;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.model.DatabaseDriver;
import dk.eobjects.datacleaner.gui.panels.GeneralSettingsPanel;
import dk.eobjects.datacleaner.gui.setup.GuiConfiguration;
import dk.eobjects.datacleaner.gui.setup.GuiSettings;
import dk.eobjects.datacleaner.util.WeakObservable;
import dk.eobjects.datacleaner.util.WeakObserver;
import dk.eobjects.thirdparty.tabs.CloseableTabbedPane;

public class SettingsDialog extends BanneredDialog implements WeakObserver {

	private static final ImageIcon ICON_SUCCESS = GuiHelper
			.getImageIcon("images/driver_success.png");
	private static final ImageIcon ICON_ERROR = GuiHelper
			.getImageIcon("images/driver_error.png");
	private static final long serialVersionUID = -2821019697454011890L;
	private GeneralSettingsPanel _generalPanel;
	private GuiSettings _settings;
	private CloseableTabbedPane _tabbedPane;
	private JPanel _driversPanel;

	public SettingsDialog() {
		super(470, 500);
	}

	@Override
	protected Component getContent() {
		JPanel content = GuiHelper.createPanel().applyBorderLayout()
				.applyDarkBlueBackground().toComponent();
		_settings = GuiSettings.getSettings();
		_settings.addObserver(this);

		// tabbed pane with settings categories
		_tabbedPane = new CloseableTabbedPane();
		_tabbedPane.setUnclosableTab(0).setUnclosableTab(1);
		_tabbedPane.setName("categoriesTab");
		_tabbedPane.addTab("General", getGeneralTab());
		_driversPanel = GuiHelper.createPanel().toComponent();
		_driversPanel.setBackground(GuiHelper.BG_COLOR_LIGHT);
		_driversPanel.setName("driversPanel");
		JScrollPane scrollPane = new JScrollPane(_driversPanel);
		scrollPane.setBorder(null);
		_tabbedPane.addTab("Database drivers", scrollPane);
		updateDatabaseDriversTab();
		content.add(_tabbedPane, BorderLayout.CENTER);

		// toolbar with save settings button
		JToolBar toolBar = getToolbar();
		content.add(toolBar, BorderLayout.SOUTH);

		return content;
	}

	private JToolBar getToolbar() {
		JToolBar toolbar = GuiHelper.createToolBar();
		JButton saveButton = GuiHelper.createButton("Save settings",
				"images/toolbar_save.png").toComponent();
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				saveSettings();
				setVisible(false);
				dispose();
			}
		});

		toolbar.add(saveButton);
		return toolbar;
	}

	private Component getGeneralTab() {
		_generalPanel = new GeneralSettingsPanel(_settings);
		JScrollPane scrollPane = new JScrollPane(_generalPanel);
		scrollPane.setBorder(null);
		return scrollPane;
	}

	private void updateDatabaseDriversTab() {
		_driversPanel.removeAll();

		JLabel header = new JLabel("Database drivers");
		header.setFont(GuiHelper.FONT_HEADER);
		GuiHelper.addToGridBag(header, _driversPanel, 0, 0);

		final JButton registerDriverButton = new JButton(
				"Register database driver", GuiHelper
						.getImageIcon("images/toolbar_database.png"));
		registerDriverButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JPopupMenu popupMenu = new JPopupMenu();
				List<JMenuItem> menuItems = DatabaseDriverDialog.getMenuItems();
				for (JMenuItem menuItem : menuItems) {
					popupMenu.add(menuItem);
				}
				popupMenu.show(registerDriverButton, 0, registerDriverButton
						.getHeight());
			}

		});
		registerDriverButton.setName("registerDriverButton");
		GuiHelper.addToGridBag(registerDriverButton, _driversPanel, 1, 0);

		final List<DatabaseDriver> drivers = new ArrayList<DatabaseDriver>(
				GuiConfiguration.getBeansOfClass(DatabaseDriver.class));
		drivers.addAll(_settings.getDatabaseDrivers());

		for (int i = 0; i < drivers.size(); i++) {
			final DatabaseDriver driver = drivers.get(i);
			final JPanel driverPanel = GuiHelper.createPanel().applyBorder()
					.toComponent();
			JLabel nameLabel;
			if (driver.getName() == null) {
				nameLabel = new JLabel(beautifyPath(driver.getFilename()));
			} else {
				nameLabel = new JLabel(driver.getName());
			}
			if (driver.isLoaded()) {
				nameLabel.setIcon(ICON_SUCCESS);
			} else {
				nameLabel.setIcon(ICON_ERROR);
				nameLabel
						.setToolTipText("An error occurred while loading the database driver");
			}
			GuiHelper.addToGridBag(nameLabel, driverPanel, 0, 0, 2, 1);
			GuiHelper.addToGridBag(new JLabel(driver.getDriverClass()),
					driverPanel, 0, 1, 1, 1);

			if (driver.getFilename() != null) {
				driverPanel.setName("driverPanel" + i);
				JButton removeButton = GuiHelper.createButton(null,
						"images/toolbar_remove.png").applySize(22, 22)
						.toComponent();
				removeButton.setToolTipText("Remove driver");
				removeButton.setName("removeButton" + i);
				removeButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						driver.unloadDriver();
						_settings.getDatabaseDrivers().remove(driver);
						GuiSettings.saveSettings(_settings);
						_driversPanel.remove(driverPanel);
						_driversPanel.updateUI();
					}
				});
				GuiHelper.addToGridBag(removeButton, driverPanel, 1, 1, 1, 1);
			}

			GridBagLayout layout = (GridBagLayout) driverPanel.getLayout();
			layout.columnWidths = new int[] { 380, 30 };

			GuiHelper.addToGridBag(driverPanel, _driversPanel, 0, i + 1, 2, 1);
		}
	}

	public static String beautifyPath(String filename) {
		if (filename != null && filename.length() > 50) {

			int lastSeparator = filename.lastIndexOf(File.separatorChar);
			String lastToken = "..." + filename.substring(lastSeparator);
			StringBuilder sb = new StringBuilder(filename);
			sb.delete(50 - lastToken.length(), sb.length());
			sb.append(lastToken);

			return sb.toString();
		}
		return filename;
	}

	private void saveSettings() {
		try {
			LookAndFeel originalLaf = UIManager.getLookAndFeel();
			String newLafClassName = _generalPanel.getLookAndFeelClassName();
			if (!originalLaf.getClass().getName().equals(newLafClassName)) {
				UIManager.setLookAndFeel(newLafClassName);
				DataCleanerGui.getMainWindow().repaintAll();
			}
			_settings.setLookAndFeelClassName(newLafClassName);
			_settings.setHorisontalMatrixTables(_generalPanel
					.isTableLayoutHorizontal());
		} catch (Exception e) {
			GuiHelper.showErrorMessage("Could not apply Look and feel", e
					.getMessage(), e);
		}
		GuiSettings.saveSettings(_settings);
	}

	@Override
	protected String getDialogTitle() {
		return "Settings";
	}

	public void update(WeakObservable o) {
		if (o instanceof GuiSettings) {
			updateDatabaseDriversTab();
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		_log.debug("dispose()");
		_settings.deleteObserver(this);
		_settings = null;
	}
}