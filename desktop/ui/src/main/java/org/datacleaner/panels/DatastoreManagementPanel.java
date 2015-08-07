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
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;

import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.connection.DatastoreDescriptor;
import org.datacleaner.connection.DatastoreDescriptorDesktopBindings;
import org.datacleaner.connection.JdbcDatastore;
import org.datacleaner.database.DatabaseDriverCatalog;
import org.datacleaner.database.DatabaseDriverDescriptor;
import org.datacleaner.guice.DCModule;
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
import org.datacleaner.windows.AnalysisJobBuilderWindow;
import org.datacleaner.windows.CompositeDatastoreDialog;
import org.datacleaner.windows.JdbcDatastoreDialog;
import org.datacleaner.windows.OptionsDialog;
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
    private final Provider<OptionsDialog> _optionsDialogProvider;
    private final DatabaseDriverCatalog _databaseDriverCatalog;
    private final List<DatastorePanel> _datastorePanels;
    private final DCGlassPane _glassPane;
    private final JButton _analyzeButton;
    private final DCPanel _datastoreListPanel;
    private final JXTextField _searchDatastoreTextField;
    private final DCModule _dcModule;
    private final UserPreferences _userPreferences;

    public DatastoreManagementPanel(DataCleanerConfiguration configuration,
            AnalysisJobBuilderWindow analysisJobBuilderWindow, DCGlassPane glassPane,
            Provider<OptionsDialog> optionsDialogProvider, DCModule dcModule,
            DatabaseDriverCatalog databaseDriverCatalog, UserPreferences userPreferences) {
        super(analysisJobBuilderWindow);

        _datastorePanels = new ArrayList<DatastorePanel>();
        _datastoreCatalog = (MutableDatastoreCatalog) configuration.getDatastoreCatalog();
        _glassPane = glassPane;
        _optionsDialogProvider = optionsDialogProvider;
        _dcModule = dcModule;
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
                            getWindow().setDatastore(datastore);
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

        final JComponent titleLabel = createTitleLabel("Datastore Management", true);
        add(titleLabel, BorderLayout.NORTH);

        final DCPanel containerPanel = new DCPanel();
        containerPanel.setLayout(new VerticalLayout(4));

        final DCLabel registerNewDatastoreLabel = DCLabel.dark("Register new:");
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

        add(wrapContent(containerPanel), BorderLayout.CENTER);

        final DCPanel newDatastorePanel = new DCPanel();
        newDatastorePanel.setLayout(new BorderLayout());
        newDatastorePanel.setBorder(new EmptyBorder(10, 10, 10, 0));
        newDatastorePanel.add(registerNewDatastoreLabel, BorderLayout.NORTH);
        newDatastorePanel.add(createNewDatastorePanel(), BorderLayout.CENTER);

        add(wrapContent(newDatastorePanel), BorderLayout.SOUTH);
    }

    private void updateDatastores() {
        Datastore selectedDatastore = getSelectedDatastore();
        _datastoreListPanel.removeAll();
        _datastorePanels.clear();

        final DCLabel existingDatastoresLabel = DCLabel.dark("Existing datastores:");
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
            DatastorePanel datastorePanel = new DatastorePanel(datastore, _datastoreCatalog, this, getWindow()
                    .getWindowContext(), _userPreferences, _dcModule);
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
        final int alignment = Alignment.LEFT.getFlowLayoutAlignment();
        final DCPanel panel1 = new DCPanel();
        
        panel1.setLayout(new FlowLayout(alignment, 10, 10));
        
        // set of databases that are displayed directly on panel
        final Set<String> databaseNames = new HashSet<String>();
        
        final int panelItemsCount = 10;
        
        for (int i = 0; i < Math.min(_datastoreCatalog.getAvailableDatastoreDescriptors().size(), panelItemsCount); i++) {
            DatastoreDescriptor datastoreDescriptor = _datastoreCatalog.getAvailableDatastoreDescriptors().get(i);
            panel1.add(createNewDatastoreButton(datastoreDescriptor.getName(),
                    datastoreDescriptor.getDescription(), DatastoreDescriptorDesktopBindings.getIconPath(datastoreDescriptor),
                    datastoreDescriptor.getDatastoreClass(), DatastoreDescriptorDesktopBindings.getDialogClass(datastoreDescriptor), DCPopupBubble.Position.BOTTOM));
            databaseNames.add(datastoreDescriptor.getName());
        }
        
        final DCPanel panel2 = new DCPanel();
        panel2.setLayout(new FlowLayout(alignment, 10, 10));

        for (int i = panelItemsCount; i < Math.min(_datastoreCatalog.getAvailableDatastoreDescriptors().size(), 2 * panelItemsCount); i++) {
            DatastoreDescriptor datastoreDescriptor = _datastoreCatalog.getAvailableDatastoreDescriptors().get(i);
            panel2.add(createNewDatastoreButton(datastoreDescriptor.getName(),
                    datastoreDescriptor.getDescription(), DatastoreDescriptorDesktopBindings.getIconPath(datastoreDescriptor),
                    datastoreDescriptor.getDatastoreClass(), DatastoreDescriptorDesktopBindings.getDialogClass(datastoreDescriptor), DCPopupBubble.Position.TOP));
            databaseNames.add(datastoreDescriptor.getName());
        }


        panel2.add(Box.createHorizontalStrut(10));
        panel2.add(createMoreDatabasesButton(databaseNames));

        final DCPanel containerPanel = new DCPanel();
        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
        containerPanel.add(panel1);
        containerPanel.add(panel2);

        return containerPanel;
    }

    private Component createMoreDatabasesButton(Set<String> databaseNames) {
        final PopupButton moreDatastoreTypesButton = WidgetFactory.createDefaultPopupButton("More databases",
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
                final CompositeDatastoreDialog dialog = new CompositeDatastoreDialog(_datastoreCatalog, getWindow()
                        .getWindowContext(), _userPreferences);
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
                final Injector injectorWithNullDatastore = _dcModule.createInjectorBuilder().with(datastoreClass, null).createInjector();
                final AbstractDialog dialog = injectorWithNullDatastore.getInstance(dialogClass);
                dialog.setVisible(true);
            }
        });
        return button;
    }

    private ActionListener createJdbcActionListener(final String databaseName) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                Injector injectorWithDatastore = _dcModule.createInjectorBuilder().with(JdbcDatastore.class, null).createInjector();
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
