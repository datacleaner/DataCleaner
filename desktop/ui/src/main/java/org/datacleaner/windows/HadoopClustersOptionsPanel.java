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
import java.awt.GridBagLayout;

import javax.inject.Inject;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.ServerInformation;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.panels.DirectoryBasedHadoopClusterDialog;
import org.datacleaner.panels.HadoopClusterPanel;
import org.datacleaner.user.MutableServerInformationCatalog;
import org.datacleaner.user.ServerInformationChangeListener;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.HadoopResource;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.widgets.DescriptionLabel;
import org.datacleaner.widgets.PopupButton;

/**
 * Dialog for managing the Hadoop configurations
 */
public class HadoopClustersOptionsPanel extends DCPanel implements ServerInformationChangeListener {

    private static final long serialVersionUID = 1L;

    private final MutableServerInformationCatalog _serverInformationCatalog;
    private final DCPanel _connectionsConfigurationsPanel;
    private final WindowContext _windowContext;
    private final PopupButton _addClusterButton;

    @Inject
    public HadoopClustersOptionsPanel(WindowContext windowContext, DataCleanerConfiguration configuration,
            UserPreferences userPreferences) {
        super(WidgetUtils.COLOR_DEFAULT_BACKGROUND);

        _windowContext = windowContext;
        _connectionsConfigurationsPanel = new DCPanel();
        _connectionsConfigurationsPanel.setLayout(new BorderLayout());
        _serverInformationCatalog = (MutableServerInformationCatalog) configuration.getServerInformationCatalog();
        
        final JMenuItem directoryMenuItem = WidgetFactory.createMenuItem("Using configuration directory", IconUtils.FILE_HDFS);
        final JMenuItem directConnectionMenuItem = WidgetFactory.createMenuItem("Using direct namenode connection", IconUtils.FILE_HDFS);
        
        _addClusterButton = WidgetFactory.createDefaultPopupButton("Add Hadoop cluster", IconUtils.ACTION_ADD_DARK);
        _addClusterButton.getMenu().add(directoryMenuItem);
        _addClusterButton.getMenu().add(directConnectionMenuItem);

        directConnectionMenuItem.addActionListener(e -> {
            final DirectConnectionHadoopClusterDialog hadoopConnectionToNamenodeDialog = new DirectConnectionHadoopClusterDialog(
                    _windowContext, null, _serverInformationCatalog);
            hadoopConnectionToNamenodeDialog.setVisible(true);
        });

        directoryMenuItem.addActionListener(e -> {
            final DirectoryBasedHadoopClusterDialog hadoopDirectoryConfigurationDialog = new DirectoryBasedHadoopClusterDialog(
                    _windowContext, null, _serverInformationCatalog);
            hadoopDirectoryConfigurationDialog.setVisible(true);
        });
        
        updateClusterList();
        setLayout(new BorderLayout());
        add(new DescriptionLabel("For DataCleaner to connect to Apache Hadoop information is required about the cluster to connect and execute jobs on it. By default you can use the HADOOP_CONF_DIR and YARN_CONF_DIR environment variables, or you can register a custom cluster using the options available in this dialog."), BorderLayout.NORTH);
        add(WidgetUtils.scrolleable(_connectionsConfigurationsPanel), BorderLayout.CENTER);
    }

    @Override
    public void onAdd(ServerInformation serverInformation) {
        SwingUtilities.invokeLater(this::updateClusterList);
    }

    @Override
    public void onRemove(ServerInformation serverInformation) {
        SwingUtilities.invokeLater(this::updateClusterList);
    }

    private DCPanel getConnectionsConfigurationsPanel() {
        int row = 0;

        final DCPanel panel = new DCPanel();
        panel.setLayout(new GridBagLayout());
        
        WidgetUtils.addToGridBag(DCPanel.flow(Alignment.RIGHT, _addClusterButton), panel, 0, row);
        row++;

        final ServerInformation defaultServer = _serverInformationCatalog.getServer(HadoopResource.DEFAULT_CLUSTERREFERENCE); 
        if (defaultServer != null) {
            final HadoopClusterPanel clusterPanel = new HadoopClusterPanel(_windowContext, defaultServer,
                    _serverInformationCatalog);
            WidgetUtils.addToGridBag(clusterPanel, panel, 0, row + 1, 1.0, 0.0);
            row++;
        }
        
        final String[] serverNames = _serverInformationCatalog.getServerNames();
        for (int i = 0; i < serverNames.length; i++) {
            final String serverName = serverNames[i];
            if (serverName != HadoopResource.DEFAULT_CLUSTERREFERENCE) {
                final ServerInformation server = _serverInformationCatalog.getServer(serverName);
                final HadoopClusterPanel clusterPanel = new HadoopClusterPanel(_windowContext, server,
                        _serverInformationCatalog);
                WidgetUtils.addToGridBag(clusterPanel, panel, 0, row + 1, 1.0, 0.0);
                row++;
            }
        }

        return panel;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        _serverInformationCatalog.addListener(this);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        _serverInformationCatalog.removeListener(this);
    }
    
    private void updateClusterList() {
        _connectionsConfigurationsPanel.removeAll();
        _connectionsConfigurationsPanel.add(getConnectionsConfigurationsPanel());
        _connectionsConfigurationsPanel.updateUI();
    }
}
