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
package org.datacleaner.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Provider;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;

import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.connection.AccessDatastore;
import org.datacleaner.connection.CassandraDatastore;
import org.datacleaner.connection.CouchDbDatastore;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.connection.DbaseDatastore;
import org.datacleaner.connection.ElasticSearchDatastore;
import org.datacleaner.connection.ExcelDatastore;
import org.datacleaner.connection.FixedWidthDatastore;
import org.datacleaner.connection.HBaseDatastore;
import org.datacleaner.connection.JdbcDatastore;
import org.datacleaner.connection.JsonDatastore;
import org.datacleaner.connection.MongoDbDatastore;
import org.datacleaner.connection.OdbDatastore;
import org.datacleaner.connection.SalesforceDatastore;
import org.datacleaner.connection.SasDatastore;
import org.datacleaner.connection.SugarCrmDatastore;
import org.datacleaner.connection.XmlDatastore;
import org.datacleaner.database.DatabaseDriverCatalog;
import org.datacleaner.database.DatabaseDriverDescriptor;
import org.datacleaner.guice.InjectorBuilder;
import org.datacleaner.user.DatastoreChangeListener;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.DCPopupBubble;
import org.datacleaner.widgets.PopupButton;
import org.datacleaner.widgets.PopupButton.MenuPosition;
import org.datacleaner.windows.AbstractDialog;
import org.datacleaner.windows.AccessDatastoreDialog;
import org.datacleaner.windows.AnalysisJobBuilderWindow;
import org.datacleaner.windows.CassandraDatastoreDialog;
import org.datacleaner.windows.CompositeDatastoreDialog;
import org.datacleaner.windows.CouchDbDatastoreDialog;
import org.datacleaner.windows.CsvDatastoreDialog;
import org.datacleaner.windows.DbaseDatastoreDialog;
import org.datacleaner.windows.ElasticSearchDatastoreDialog;
import org.datacleaner.windows.ExcelDatastoreDialog;
import org.datacleaner.windows.FixedWidthDatastoreDialog;
import org.datacleaner.windows.HBaseDatastoreDialog;
import org.datacleaner.windows.JdbcDatastoreDialog;
import org.datacleaner.windows.JsonDatastoreDialog;
import org.datacleaner.windows.MongoDbDatastoreDialog;
import org.datacleaner.windows.OdbDatastoreDialog;
import org.datacleaner.windows.OptionsDialog;
import org.datacleaner.windows.SalesforceDatastoreDialog;
import org.datacleaner.windows.SasDatastoreDialog;
import org.datacleaner.windows.SugarCrmDatastoreDialog;
import org.datacleaner.windows.XmlDatastoreDialog;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.VerticalLayout;

import com.google.inject.Injector;

/**
 * Panel to select which job or datastore to use. Shown in the "source" tab, if
 * no datastore or job has been selected to begin with.
 */
public class DatastoreManagementPanel extends DCSplashPanel implements DatastoreChangeListener {

    private static final long serialVersionUID = 1L;

    private static final ImageManager imageManager = ImageManager.get();

    private final MutableDatastoreCatalog _datastoreCatalog;
    private final AnalysisJobBuilderWindow _analysisJobBuilderWindow;
    private final Provider<OptionsDialog> _optionsDialogProvider;
    private final DatabaseDriverCatalog _databaseDriverCatalog;
    private final List<DatastorePanel> _datastorePanels;
    private final DCGlassPane _glassPane;
    private final JButton _analyzeButton;
    private final DCPanel _datastoreListPanel;
    private final JXTextField _searchDatastoreTextField;
    private final InjectorBuilder _injectorBuilder;
    private final UserPreferences _userPreferences;

    public DatastoreManagementPanel(AnalyzerBeansConfiguration configuration,
            AnalysisJobBuilderWindow analysisJobBuilderWindow, DCGlassPane glassPane,
            Provider<OptionsDialog> optionsDialogProvider, InjectorBuilder injectorBuilder,
            DatabaseDriverCatalog databaseDriverCatalog, UserPreferences userPreferences) {
        super();

        _datastorePanels = new ArrayList<DatastorePanel>();
        _datastoreCatalog = (MutableDatastoreCatalog) configuration.getDatastoreCatalog();
        _analysisJobBuilderWindow = analysisJobBuilderWindow;
        _glassPane = glassPane;
        _optionsDialogProvider = optionsDialogProvider;
        _injectorBuilder = injectorBuilder;
        _databaseDriverCatalog = databaseDriverCatalog;
        _userPreferences = userPreferences;

        // initialize "Build job" button
        _analyzeButton = WidgetFactory.createPrimaryButton("Build job", IconUtils.MODEL_JOB);
        _analyzeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (DatastorePanel datastorePanel : _datastorePanels) {
                    if (datastorePanel.isSelected()) {
                        Datastore datastore = datastorePanel.getDatastore();

                        // open the connection here, to make any connection
                        // issues apparent early
                        try (DatastoreConnection datastoreConnection = datastore.openConnection()) {
                            datastoreConnection.getDataContext().getSchemaNames();
                            _analysisJobBuilderWindow.setDatastore(datastore);
                        }
                        return;
                    }
                }
            }
        });

        // initialize search text field
        _searchDatastoreTextField = WidgetFactory.createTextField("Search/filter datastores");
        _searchDatastoreTextField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                String text = _searchDatastoreTextField.getText();
                if (StringUtils.isNullOrEmpty(text)) {
                    // when there is no search query, set all datastores
                    // visible
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

        setLayout(new BorderLayout());

        final DCLabel titleLabel = createTitleLabel("Datastore Management");
        add(titleLabel, BorderLayout.NORTH);

        final DCPanel containerPanel = new DCPanel();
        containerPanel.setLayout(new VerticalLayout(4));

        final DCLabel registerNewDatastoreLabel = DCLabel.bright("Register new:");
        registerNewDatastoreLabel.setFont(WidgetUtils.FONT_HEADER2);

        _datastoreListPanel = new DCPanel();
        _datastoreListPanel.setLayout(new VerticalLayout(4));
        _datastoreListPanel.setBorder(new EmptyBorder(10, 10, 4, 0));
        containerPanel.add(_datastoreListPanel);
        updateDatastores();

        final DCPanel buttonPanel = new DCPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
        buttonPanel.setBorder(new EmptyBorder(0, 10, 0, 0));
        buttonPanel.add(_analyzeButton);
        containerPanel.add(buttonPanel);

        add(wrapContentInScrollerWithMaxWidth(containerPanel), BorderLayout.CENTER);

        final DCPanel newDatastorePanel = new DCPanel();
        newDatastorePanel.setLayout(new BorderLayout());
        newDatastorePanel.setBorder(new EmptyBorder(10, 10, 10, 0));
        newDatastorePanel.add(registerNewDatastoreLabel, BorderLayout.NORTH);
        newDatastorePanel.add(createNewDatastorePanel(), BorderLayout.CENTER);
        
        add(createBackToWelcomeScreenButton(analysisJobBuilderWindow), BorderLayout.WEST);

        add(wrapContentInScrollerWithMaxWidth(newDatastorePanel), BorderLayout.SOUTH);
    }

    private void updateDatastores() {
        Datastore selectedDatastore = getSelectedDatastore();
        _datastoreListPanel.removeAll();
        _datastorePanels.clear();

        final DCLabel existingDatastoresLabel = DCLabel.bright("Existing datastores:");
        existingDatastoresLabel.setFont(WidgetUtils.FONT_HEADER2);

        final DCPanel headerPanel = new DCPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.add(existingDatastoresLabel, BorderLayout.WEST);
        headerPanel.add(_searchDatastoreTextField, BorderLayout.EAST);

        _datastoreListPanel.add(headerPanel);

        boolean selectFirst = true;

        String[] datastoreNames = _datastoreCatalog.getDatastoreNames();
        for (int i = 0; i < datastoreNames.length; i++) {
            final Datastore datastore = _datastoreCatalog.getDatastore(datastoreNames[i]);
            DatastorePanel datastorePanel = new DatastorePanel(datastore, _datastoreCatalog, this,
                    _analysisJobBuilderWindow.getWindowContext(), _userPreferences, _injectorBuilder);
            _datastorePanels.add(datastorePanel);
            _datastoreListPanel.add(datastorePanel);

            if (selectedDatastore != null && selectedDatastore.getName().equals(datastore.getName())) {
                selectFirst = false;
                setSelectedDatastore(datastore);
            }
        }

        if (selectFirst) {
            selectFirstVisibleDatastore();
        }
    }

    public void setSelectedDatastore(Datastore datastore) {
        if (datastore != null) {
            for (DatastorePanel panel : _datastorePanels) {
                if (datastore.equals(panel.getDatastore())) {
                    panel.setSelected(true);
                } else {
                    panel.setSelected(false);
                }
            }
        }
    }

    public Datastore getSelectedDatastore() {
        DatastorePanel datastorePanel = getSelectedDatastorePanel();
        if (datastorePanel == null) {
            return null;
        }
        return datastorePanel.getDatastore();
    }

    private DCPanel createNewDatastorePanel() {
        final int alignment = Alignment.CENTER.getFlowLayoutAlignment();
        final DCPanel panel1 = new DCPanel();
        panel1.setLayout(new FlowLayout(alignment, 10, 10));
        panel1.add(createNewDatastoreButton("CSV file",
                "Comma-separated values (CSV) file (or file with other separators)", IconUtils.CSV_IMAGEPATH,
                CsvDatastore.class, CsvDatastoreDialog.class, DCPopupBubble.Position.BOTTOM));
        panel1.add(createNewDatastoreButton("Excel spreadsheet",
                "Microsoft Excel spreadsheet. Either .xls (97-2003) or .xlsx (2007+) format.",
                IconUtils.EXCEL_IMAGEPATH, ExcelDatastore.class, ExcelDatastoreDialog.class,
                DCPopupBubble.Position.BOTTOM));
        panel1.add(createNewDatastoreButton("Access database", "Microsoft Access database file (.mdb).",
                IconUtils.ACCESS_IMAGEPATH, AccessDatastore.class, AccessDatastoreDialog.class,
                DCPopupBubble.Position.BOTTOM));
        panel1.add(createNewDatastoreButton("SAS library", "A directory of SAS library files (.sas7bdat).",
                IconUtils.SAS_IMAGEPATH, SasDatastore.class, SasDatastoreDialog.class, DCPopupBubble.Position.BOTTOM));
        panel1.add(createNewDatastoreButton("DBase database", "DBase database file (.dbf)", IconUtils.DBASE_IMAGEPATH,
                DbaseDatastore.class, DbaseDatastoreDialog.class, DCPopupBubble.Position.BOTTOM));
        panel1.add(createNewDatastoreButton("Fixed width file",
                "Text file with fixed width values. Each value spans a fixed amount of text characters.",
                IconUtils.FIXEDWIDTH_IMAGEPATH, FixedWidthDatastore.class, FixedWidthDatastoreDialog.class,
                DCPopupBubble.Position.BOTTOM));
        panel1.add(createNewDatastoreButton("XML file", "Extensible Markup Language file (.xml)",
                IconUtils.XML_IMAGEPATH, XmlDatastore.class, XmlDatastoreDialog.class, DCPopupBubble.Position.BOTTOM));
        panel1.add(createNewDatastoreButton("JSON file", "JavaScript Object NOtation file (.json).",
                IconUtils.JSON_IMAGEPATH, JsonDatastore.class, JsonDatastoreDialog.class, DCPopupBubble.Position.BOTTOM));
        panel1.add(createNewDatastoreButton("OpenOffice.org Base database", "OpenOffice.org Base database file (.odb)",
                IconUtils.ODB_IMAGEPATH, OdbDatastore.class, OdbDatastoreDialog.class, DCPopupBubble.Position.BOTTOM));

        panel1.add(Box.createHorizontalStrut(10));
        panel1.add(createNewDatastoreButton("Salesforce.com", "Connect to a Salesforce.com account",
                IconUtils.SALESFORCE_IMAGEPATH, SalesforceDatastore.class, SalesforceDatastoreDialog.class,
                DCPopupBubble.Position.BOTTOM));
        panel1.add(createNewDatastoreButton("SugarCRM", "Connect to a SugarCRM system", IconUtils.SUGAR_CRM_IMAGEPATH,
                SugarCrmDatastore.class, SugarCrmDatastoreDialog.class, DCPopupBubble.Position.BOTTOM));

        final DCPanel panel2 = new DCPanel();
        panel2.setLayout(new FlowLayout(alignment, 10, 10));

        panel2.add(createNewDatastoreButton("MongoDB database", "Connect to a MongoDB database",
                IconUtils.MONGODB_IMAGEPATH, MongoDbDatastore.class, MongoDbDatastoreDialog.class,
                DCPopupBubble.Position.TOP));

        panel2.add(createNewDatastoreButton("CouchDB database", "Connect to an Apache CouchDB database",
                IconUtils.COUCHDB_IMAGEPATH, CouchDbDatastore.class, CouchDbDatastoreDialog.class,
                DCPopupBubble.Position.TOP));

        panel2.add(createNewDatastoreButton("ElasticSearch index", "Connect to an ElasticSearch index",
                IconUtils.ELASTICSEARCH_IMAGEPATH, ElasticSearchDatastore.class, ElasticSearchDatastoreDialog.class,
                DCPopupBubble.Position.TOP));

        panel2.add(createNewDatastoreButton("Cassandra database", "Connect to an Apache Cassandra database",
                IconUtils.CASSANDRA_IMAGEPATH, CassandraDatastore.class, CassandraDatastoreDialog.class,
                DCPopupBubble.Position.TOP));

        panel2.add(createNewDatastoreButton("HBase database", "Connect to an Apache HBase database",
                IconUtils.HBASE_IMAGEPATH, HBaseDatastore.class, HBaseDatastoreDialog.class, DCPopupBubble.Position.TOP));

        // set of databases that are displayed directly on panel
        final Set<String> databaseNames = new HashSet<String>();

        createDefaultDatabaseButtons(panel2, databaseNames);

        panel2.add(Box.createHorizontalStrut(10));
        panel2.add(createMoreDatabasesButton(databaseNames));

        final DCPanel containerPanel = new DCPanel();
        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
        containerPanel.add(panel1);
        containerPanel.add(panel2);

        return containerPanel;
    }

    private Component createMoreDatabasesButton(Set<String> databaseNames) {
        final PopupButton moreDatastoreTypesButton = WidgetFactory.createDarkPopupButton("More databases",
                IconUtils.GENERIC_DATASTORE_IMAGEPATH);
        moreDatastoreTypesButton.setMenuPosition(MenuPosition.TOP);

        final JPopupMenu moreDatastoreTypesMenu = moreDatastoreTypesButton.getMenu();
        // installed databases
        final List<DatabaseDriverDescriptor> databaseDrivers = _databaseDriverCatalog
                .getInstalledWorkingDatabaseDrivers();
        for (DatabaseDriverDescriptor databaseDriver : databaseDrivers) {
            final String databaseName = databaseDriver.getDisplayName();
            if (!databaseNames.contains(databaseName)) {
                final String imagePath = databaseDriver.getIconImagePath();
                final ImageIcon icon = imageManager.getImageIcon(imagePath, IconUtils.ICON_SIZE_SMALL);
                final JMenuItem menuItem = WidgetFactory.createMenuItem(databaseName, icon);
                menuItem.addActionListener(createJdbcActionListener(databaseName));
                moreDatastoreTypesMenu.add(menuItem);
            }
        }

        // custom/other jdbc connection
        final ImageIcon icon = imageManager.getImageIcon(IconUtils.GENERIC_DATASTORE_IMAGEPATH,
                IconUtils.ICON_SIZE_SMALL);
        final JMenuItem menuItem = WidgetFactory.createMenuItem("Other database", icon);
        menuItem.addActionListener(createJdbcActionListener(null));
        moreDatastoreTypesMenu.add(menuItem);

        // composite datastore
        final JMenuItem compositeMenuItem = WidgetFactory.createMenuItem("Composite datastore",
                imageManager.getImageIcon(IconUtils.COMPOSITE_IMAGEPATH, IconUtils.ICON_SIZE_SMALL));
        compositeMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final CompositeDatastoreDialog dialog = new CompositeDatastoreDialog(_datastoreCatalog,
                        _analysisJobBuilderWindow.getWindowContext(), _userPreferences);
                dialog.open();
            }
        });

        final JMenuItem databaseDriversMenuItem = WidgetFactory.createMenuItem("Manage database drivers...",
                imageManager.getImageIcon(IconUtils.MENU_OPTIONS, IconUtils.ICON_SIZE_SMALL));
        databaseDriversMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OptionsDialog dialog = _optionsDialogProvider.get();
                dialog.selectDatabaseDriversTab();
                dialog.setVisible(true);
            }
        });

        moreDatastoreTypesMenu.add(databaseDriversMenuItem);
        moreDatastoreTypesMenu.add(new JSeparator(JSeparator.HORIZONTAL));
        moreDatastoreTypesMenu.add(compositeMenuItem);
        return moreDatastoreTypesButton;
    }

    private void createDefaultDatabaseButtons(DCPanel panel, Set<String> databaseNames) {
        if (_databaseDriverCatalog.isInstalled(DatabaseDriverCatalog.DATABASE_NAME_MYSQL)) {
            panel.add(createNewJdbcDatastoreButton("MySQL connection", "Connect to a MySQL database",
                    "images/datastore-types/databases/mysql.png", DatabaseDriverCatalog.DATABASE_NAME_MYSQL,
                    databaseNames));
        }
        if (_databaseDriverCatalog.isInstalled(DatabaseDriverCatalog.DATABASE_NAME_POSTGRESQL)) {
            panel.add(createNewJdbcDatastoreButton("PostgreSQL connection", "Connect to a PostgreSQL database",
                    "images/datastore-types/databases/postgresql.png", DatabaseDriverCatalog.DATABASE_NAME_POSTGRESQL,
                    databaseNames));
        }
        if (_databaseDriverCatalog.isInstalled(DatabaseDriverCatalog.DATABASE_NAME_ORACLE)) {
            panel.add(createNewJdbcDatastoreButton("Oracle connection", "Connect to a Oracle database",
                    "images/datastore-types/databases/oracle.png", DatabaseDriverCatalog.DATABASE_NAME_ORACLE,
                    databaseNames));
        }
        if (_databaseDriverCatalog.isInstalled(DatabaseDriverCatalog.DATABASE_NAME_MICROSOFT_SQL_SERVER_JTDS)) {
            panel.add(createNewJdbcDatastoreButton("Microsoft SQL Server connection",
                    "Connect to a Microsoft SQL Server database", "images/datastore-types/databases/microsoft.png",
                    DatabaseDriverCatalog.DATABASE_NAME_MICROSOFT_SQL_SERVER_JTDS, databaseNames));
        }
    }

    private <D extends Datastore> JButton createNewDatastoreButton(final String title, final String description,
            final String imagePath, final Class<D> datastoreClass, final Class<? extends AbstractDialog> dialogClass,
            DCPopupBubble.Position popupPosition) {
        final ImageIcon icon = imageManager.getImageIcon(imagePath);
        final JButton button = WidgetFactory.createImageButton(icon);

        final DCPopupBubble popupBubble = new DCPopupBubble(_glassPane, "<html><b>" + title + "</b><br/>" + description
                + "</html>", 0, 0, icon, popupPosition);
        popupBubble.attachTo(button);

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                Injector injectorWithNullDatastore = _injectorBuilder.with(datastoreClass, null).createInjector();
                AbstractDialog dialog = injectorWithNullDatastore.getInstance(dialogClass);
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
                + "</html>", 0, 0, icon, DCPopupBubble.Position.TOP);
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
    public void addNotify() {
        super.addNotify();
        _datastoreCatalog.addListener(this);
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
