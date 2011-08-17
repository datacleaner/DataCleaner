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

import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.AccessDatastore;
import org.eobjects.analyzer.connection.CsvDatastore;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DbaseDatastore;
import org.eobjects.analyzer.connection.ExcelDatastore;
import org.eobjects.analyzer.connection.FileDatastore;
import org.eobjects.analyzer.connection.FixedWidthDatastore;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.connection.OdbDatastore;
import org.eobjects.analyzer.connection.SasDatastore;
import org.eobjects.analyzer.connection.XmlDatastore;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.database.DatabaseDriverCatalog;
import org.eobjects.datacleaner.database.DatabaseDriverDescriptor;
import org.eobjects.datacleaner.guice.InjectorBuilder;
import org.eobjects.datacleaner.user.DatastoreChangeListener;
import org.eobjects.datacleaner.user.MutableDatastoreCatalog;
import org.eobjects.datacleaner.util.DCDocumentListener;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.Alignment;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.widgets.DCPopupBubble;
import org.eobjects.datacleaner.windows.AbstractFileBasedDatastoreDialog;
import org.eobjects.datacleaner.windows.AccessDatastoreDialog;
import org.eobjects.datacleaner.windows.AnalysisJobBuilderWindow;
import org.eobjects.datacleaner.windows.CompositeDatastoreDialog;
import org.eobjects.datacleaner.windows.CsvDatastoreDialog;
import org.eobjects.datacleaner.windows.DbaseDatastoreDialog;
import org.eobjects.datacleaner.windows.ExcelDatastoreDialog;
import org.eobjects.datacleaner.windows.FixedWidthDatastoreDialog;
import org.eobjects.datacleaner.windows.JdbcDatastoreDialog;
import org.eobjects.datacleaner.windows.OdbDatastoreDialog;
import org.eobjects.datacleaner.windows.OptionsDialog;
import org.eobjects.datacleaner.windows.SasDatastoreDialog;
import org.eobjects.datacleaner.windows.XmlDatastoreDialog;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.VerticalLayout;

import com.google.inject.Injector;

/**
 * Panel to select which datastore to use. Shown in the "source" tab, if no
 * datastore has been selected to begin with.
 * 
 * @author Kasper Sørensen
 */
public class DatastoreListPanel extends DCPanel implements DatastoreChangeListener {

	private static final long serialVersionUID = 1L;

	private static final ImageManager imageManager = ImageManager.getInstance();
	private final MutableDatastoreCatalog _datastoreCatalog;
	private final AnalysisJobBuilderWindow _analysisJobBuilderWindow;
	private final Provider<OptionsDialog> _optionsDialogProvider;
	private final DatabaseDriverCatalog _databaseDriverCatalog;
	private final List<DatastorePanel> _datastorePanels = new ArrayList<DatastorePanel>();
	private final DCGlassPane _glassPane;
	private final JButton _analyzeButton;
	private final DCPanel _listPanel;
	private final JXTextField _searchDatastoreTextField;
	private final InjectorBuilder _injectorBuilder;

	@Inject
	protected DatastoreListPanel(AnalyzerBeansConfiguration configuration,
			AnalysisJobBuilderWindow analysisJobBuilderWindow, DCGlassPane glassPane,
			Provider<OptionsDialog> optionsDialogProvider, InjectorBuilder injectorBuilder,
			DatabaseDriverCatalog databaseDriverCatalog) {
		super();
		_datastoreCatalog = (MutableDatastoreCatalog) configuration.getDatastoreCatalog();
		_analysisJobBuilderWindow = analysisJobBuilderWindow;
		_glassPane = glassPane;
		_optionsDialogProvider = optionsDialogProvider;
		_injectorBuilder = injectorBuilder;
		_databaseDriverCatalog = databaseDriverCatalog;

		_datastoreCatalog.addListener(this);

		// initialize "analyze" button
		_analyzeButton = new JButton("Analyze!", imageManager.getImageIcon("images/filetypes/analysis_job.png"));
		_analyzeButton.setMargin(new Insets(1, 1, 1, 1));
		_analyzeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int i = 0;
				for (DatastorePanel datastorePanel : _datastorePanels) {
					if (datastorePanel.isSelected()) {
						Datastore datastore = datastorePanel.getDatastore();

						// open the connection here, to make any connection
						// issues apparent early
						DataContextProvider dataContextProvider = datastore.getDataContextProvider();
						dataContextProvider.getDataContext().getSchemaNames();
						_analysisJobBuilderWindow.setDatastore(datastore);
						dataContextProvider.close();
						return;
					}
					i++;
				}
			}
		});

		// initialize search text field
		_searchDatastoreTextField = WidgetFactory.createTextField("Search/filter datastores");
		_searchDatastoreTextField.setBorder(new CompoundBorder(new EmptyBorder(4, 0, 0, 0), WidgetUtils.BORDER_THIN));
		_searchDatastoreTextField.setOpaque(false);
		_searchDatastoreTextField.getDocument().addDocumentListener(new DCDocumentListener() {
			@Override
			protected void onChange(DocumentEvent event) {
				String text = _searchDatastoreTextField.getText();
				if (StringUtils.isNullOrEmpty(text)) {
					// when there is no search query, set all datastores visible
					for (DatastorePanel datastorePanel : _datastorePanels) {
						datastorePanel.setVisible(true);
					}
				} else {
					// do a case insensitive search
					text = text.trim().toLowerCase();
					for (DatastorePanel datastorePanel : _datastorePanels) {
						String name = datastorePanel.getDatastore().getName().toLowerCase();
						datastorePanel.setVisible(name.indexOf(text) != -1);
					}
					selectFirstVisibleDatastore();
				}
			}
		});
		_searchDatastoreTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					clickAnalyzeButton();
				} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
					selectNextVisibleDatastore();
				} else if (e.getKeyCode() == KeyEvent.VK_UP) {
					selectPreviousVisibleDatastore();
				}
			}
		});

		setLayout(new VerticalLayout(4));

		final DCLabel headerLabel = DCLabel.dark("Select datastore for analysis");
		headerLabel.setFont(WidgetUtils.FONT_HEADER1);
		add(headerLabel);

		final DCLabel createNewDatastoreLabel = DCLabel.dark("Create a new datastore:");
		createNewDatastoreLabel.setFont(WidgetUtils.FONT_HEADER1);

		final DCPanel newDatastorePanel = new DCPanel();
		newDatastorePanel.setLayout(new VerticalLayout(4));
		newDatastorePanel.setBorder(new EmptyBorder(10, 10, 10, 0));
		newDatastorePanel.add(createNewDatastoreLabel);
		newDatastorePanel.add(createNewDatastorePanel());

		add(newDatastorePanel);

		_listPanel = new DCPanel();
		_listPanel.setLayout(new VerticalLayout(4));
		_listPanel.setBorder(new EmptyBorder(10, 10, 10, 0));
		add(_listPanel);
		updateDatastores();

		final DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		buttonPanel.setBorder(new EmptyBorder(0, 10, 0, 0));
		buttonPanel.add(_analyzeButton);

		add(buttonPanel);
	}

	private void updateDatastores() {
		_listPanel.removeAll();
		_datastorePanels.clear();

		final DCLabel existingDatastoresLabel = DCLabel.dark("Analyze an existing datastore:");
		existingDatastoresLabel.setFont(WidgetUtils.FONT_HEADER1);

		final DCPanel searchDatastorePanel = DCPanel.around(_searchDatastoreTextField);
		searchDatastorePanel.setBorder(WidgetUtils.BORDER_SHADOW);

		final DCPanel headerPanel = new DCPanel();
		headerPanel.setLayout(new FlowLayout(Alignment.LEFT.getFlowLayoutAlignment(), 0, 0));
		headerPanel.add(existingDatastoresLabel);
		headerPanel.add(Box.createHorizontalStrut(20));
		headerPanel.add(searchDatastorePanel);

		_listPanel.add(headerPanel);

		String[] datastoreNames = _datastoreCatalog.getDatastoreNames();
		for (int i = 0; i < datastoreNames.length; i++) {
			final Datastore datastore = _datastoreCatalog.getDatastore(datastoreNames[i]);
			DatastorePanel datastorePanel = new DatastorePanel(datastore, _datastoreCatalog, this,
					_analysisJobBuilderWindow.getWindowContext(), _injectorBuilder);
			_datastorePanels.add(datastorePanel);
			_listPanel.add(datastorePanel);
		}

		selectFirstVisibleDatastore();
	}

	private DCPanel createNewDatastorePanel() {
		final DCPanel panel = new DCPanel();
		panel.setBorder(WidgetUtils.BORDER_LIST_ITEM);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
		panel.add(createNewDatastoreButton("CSV file", "Comma-separated values (CSV) file (or file with other separators)",
				IconUtils.CSV_IMAGEPATH, CsvDatastore.class, CsvDatastoreDialog.class));
		panel.add(createNewDatastoreButton("Excel spreadsheet",
				"Microsoft Excel spreadsheet. Either .xls (97-2003) or .xlsx (2007+) format.", IconUtils.EXCEL_IMAGEPATH,
				ExcelDatastore.class, ExcelDatastoreDialog.class));
		panel.add(createNewDatastoreButton("Access database", "Microsoft Access database file (.mdb).",
				IconUtils.ACCESS_IMAGEPATH, AccessDatastore.class, AccessDatastoreDialog.class));
		panel.add(createNewDatastoreButton("SAS library", "A directory of SAS library files (.sas7bdat).",
				IconUtils.SAS_IMAGEPATH, SasDatastore.class, SasDatastoreDialog.class));
		panel.add(createNewDatastoreButton("DBase database", "DBase database file (.dbf)", IconUtils.DBASE_IMAGEPATH,
				DbaseDatastore.class, DbaseDatastoreDialog.class));
		panel.add(createNewDatastoreButton("Fixed width file",
				"Text file with fixed width values. Each value spans a fixed amount of text characters.",
				IconUtils.FIXEDWIDTH_IMAGEPATH, FixedWidthDatastore.class, FixedWidthDatastoreDialog.class));
		panel.add(createNewDatastoreButton("XML file", "Extensible Markup Language file (.xml)", IconUtils.XML_IMAGEPATH,
				XmlDatastore.class, XmlDatastoreDialog.class));
		panel.add(createNewDatastoreButton("OpenOffice.org Base database", "OpenOffice.org Base database file (.odb)",
				IconUtils.ODB_IMAGEPATH, OdbDatastore.class, OdbDatastoreDialog.class));

		panel.add(Box.createHorizontalStrut(20));

		// set of databases that are displayed directly on panel
		final Set<String> databaseNames = new HashSet<String>();

		createDefaultDatabaseButtons(panel, databaseNames);

		final JButton moreDatastoreTypesButton = new JButton("more");
		moreDatastoreTypesButton.setMargin(new Insets(1, 1, 1, 1));
		moreDatastoreTypesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JPopupMenu popup = new JPopupMenu();

				final List<DatabaseDriverDescriptor> databaseDrivers = _databaseDriverCatalog
						.getInstalledWorkingDatabaseDrivers();
				for (DatabaseDriverDescriptor databaseDriver : databaseDrivers) {
					final String databaseName = databaseDriver.getDisplayName();
					if (!databaseNames.contains(databaseName)) {
						final String imagePath = databaseDriver.getIconImagePath();
						final ImageIcon icon = imageManager.getImageIcon(imagePath, IconUtils.ICON_SIZE_SMALL);
						final JMenuItem menuItem = WidgetFactory.createMenuItem(databaseName, icon);
						menuItem.addActionListener(createJdbcActionListener(databaseName));
						popup.add(menuItem);
					}
				}

				final JMenuItem compositeMenuItem = WidgetFactory.createMenuItem("Composite datastore",
						imageManager.getImageIcon(IconUtils.COMPOSITE_IMAGEPATH, IconUtils.ICON_SIZE_SMALL));
				compositeMenuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						new CompositeDatastoreDialog(_datastoreCatalog, _analysisJobBuilderWindow.getWindowContext())
								.setVisible(true);
					}
				});

				final JMenuItem databaseDriversMenuItem = WidgetFactory.createMenuItem("Manage database drivers...",
						imageManager.getImageIcon("images/menu/options.png", IconUtils.ICON_SIZE_SMALL));
				databaseDriversMenuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						OptionsDialog dialog = _optionsDialogProvider.get();
						dialog.selectDatabaseDriversTab();
						dialog.setVisible(true);
					}
				});

				popup.add(databaseDriversMenuItem);
				popup.add(new JSeparator(JSeparator.HORIZONTAL));
				popup.add(compositeMenuItem);
				popup.setBorder(WidgetUtils.BORDER_THIN);

				popup.show(moreDatastoreTypesButton, 0, moreDatastoreTypesButton.getHeight());
			}
		});

		panel.add(Box.createHorizontalStrut(10));
		panel.add(moreDatastoreTypesButton);

		return panel;
	}

	private void createDefaultDatabaseButtons(DCPanel panel, Set<String> databaseNames) {
		if (_databaseDriverCatalog.isInstalled(DatabaseDriverCatalog.DATABASE_NAME_MYSQL)) {
			panel.add(createNewJdbcDatastoreButton("MySQL connection", "Connect to a MySQL database",
					"images/datastore-types/databases/mysql.png", DatabaseDriverCatalog.DATABASE_NAME_MYSQL, databaseNames));
		}
		if (_databaseDriverCatalog.isInstalled(DatabaseDriverCatalog.DATABASE_NAME_POSTGRESQL)) {
			panel.add(createNewJdbcDatastoreButton("PostgreSQL connection", "Connect to a PostgreSQL database",
					"images/datastore-types/databases/postgresql.png", DatabaseDriverCatalog.DATABASE_NAME_POSTGRESQL,
					databaseNames));
		}
		if (_databaseDriverCatalog.isInstalled(DatabaseDriverCatalog.DATABASE_NAME_ORACLE)) {
			panel.add(createNewJdbcDatastoreButton("Oracle connection", "Connect to a Oracle database",
					"images/datastore-types/databases/oracle.png", DatabaseDriverCatalog.DATABASE_NAME_ORACLE, databaseNames));
		}
		if (_databaseDriverCatalog.isInstalled(DatabaseDriverCatalog.DATABASE_NAME_MICROSOFT_SQL_SERVER_JTDS)) {
			panel.add(createNewJdbcDatastoreButton("Microsoft SQL Server connection",
					"Connect to a Microsoft SQL Server database", "images/datastore-types/databases/microsoft.png",
					DatabaseDriverCatalog.DATABASE_NAME_MICROSOFT_SQL_SERVER_JTDS, databaseNames));
		}
	}

	private <D extends FileDatastore> JButton createNewDatastoreButton(final String title, final String description,
			final String imagePath, final Class<D> datastoreClass,
			final Class<? extends AbstractFileBasedDatastoreDialog<D>> dialogClass) {
		final ImageIcon icon = imageManager.getImageIcon(imagePath);
		final JButton button = WidgetFactory.createImageButton(icon);

		final DCPopupBubble popupBubble = new DCPopupBubble(_glassPane, "<html><b>" + title + "</b><br/>" + description
				+ "</html>", 0, 0, imagePath);
		popupBubble.attachTo(button);

		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Injector injectorWithNullDatastore = _injectorBuilder.with(datastoreClass, null).createInjector();
				AbstractFileBasedDatastoreDialog<D> dialog = injectorWithNullDatastore.getInstance(dialogClass);
				dialog.setVisible(true);
			}
		});
		return button;
	}

	private JButton createNewJdbcDatastoreButton(final String title, final String description, final String imagePath,
			final String databaseName, Set<String> databaseNames) {

		databaseNames.add(databaseName);

		final ImageIcon icon = imageManager.getImageIcon(imagePath);
		final JButton button = WidgetFactory.createImageButton(icon);

		final DCPopupBubble popupBubble = new DCPopupBubble(_glassPane, "<html><b>" + title + "</b><br/>" + description
				+ "</html>", 0, 0, imagePath);
		popupBubble.attachTo(button);

		button.addActionListener(createJdbcActionListener(databaseName));

		return button;
	}

	private ActionListener createJdbcActionListener(final String databaseName) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Injector injectorWithDatastore = _injectorBuilder.with(JdbcDatastore.class, null).createInjector();
				JdbcDatastoreDialog dialog = injectorWithDatastore.getInstance(JdbcDatastoreDialog.class);
				dialog.setSelectedDatabase(databaseName);
				dialog.setVisible(true);
			}
		};
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		_datastoreCatalog.removeListener(this);
	}

	@Override
	public void onAdd(Datastore datastore) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateDatastores();
			}
		});
	}

	@Override
	public void onRemove(Datastore datastore) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateDatastores();
			}
		});
	}

	private void selectFirstVisibleDatastore() {
		boolean found = false;

		for (DatastorePanel datastorePanel : _datastorePanels) {
			if (datastorePanel.isVisible()) {
				setSelectedDatastorePanel(datastorePanel);
				found = true;
				break;
			}
		}

		_analyzeButton.setEnabled(found);
	}

	private void selectNextVisibleDatastore() {
		DatastorePanel selectedDatastorePanel = getSelectedDatastorePanel();
		if (selectedDatastorePanel != null) {

			int indexOf = _datastorePanels.indexOf(selectedDatastorePanel);
			for (int i = indexOf + 1; i < _datastorePanels.size(); i++) {
				DatastorePanel panel = _datastorePanels.get(i);
				if (panel.isVisible()) {
					setSelectedDatastorePanel(panel);
					break;
				}
			}
		}
	}

	private void selectPreviousVisibleDatastore() {
		DatastorePanel selectedDatastorePanel = getSelectedDatastorePanel();
		if (selectedDatastorePanel != null) {

			int indexOf = _datastorePanels.indexOf(selectedDatastorePanel);
			for (int i = indexOf - 1; i >= 0; i--) {
				DatastorePanel panel = _datastorePanels.get(i);
				if (panel.isVisible()) {
					setSelectedDatastorePanel(panel);
					break;
				}
			}
		}
	}

	public void setSelectedDatastorePanel(DatastorePanel datastorePanel) {
		for (DatastorePanel panel : _datastorePanels) {
			if (datastorePanel == panel) {
				panel.setSelected(true);
			} else {
				panel.setSelected(false);
			}
		}
		requestSearchFieldFocus();
	}

	public DatastorePanel getSelectedDatastorePanel() {
		for (DatastorePanel panel : _datastorePanels) {
			if (panel.isVisible() && panel.isSelected()) {
				return panel;
			}
		}
		return null;
	}

	public void clickAnalyzeButton() {
		if (_analyzeButton.isEnabled()) {
			_analyzeButton.doClick();
		}
	}

	public void requestSearchFieldFocus() {
		_searchDatastoreTextField.requestFocus();
	}
}
