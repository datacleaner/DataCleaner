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
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.datacleaner.bootstrap.DCWindowContext;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.ServerInformation;
import org.datacleaner.configuration.ServerInformationCatalog;
import org.datacleaner.configuration.ServerInformationCatalogImpl;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.panels.HadoopClusterPanel;
import org.datacleaner.panels.HadoopDirectoryConfigurationDialog;
import org.datacleaner.server.DirectConnectionHadoopClusterInformation;
import org.datacleaner.server.DirectoryBasedHadoopClusterInformation;
import org.datacleaner.server.EnvironmentBasedHadoopClusterInformation;
import org.datacleaner.user.MutableServerInformationCatalog;
import org.datacleaner.user.ServerInformationChangeListener;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.user.UserPreferencesImpl;
import org.datacleaner.util.HadoopResource;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.LookAndFeelManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.widgets.DescriptionLabel;

/**
 * Dialog for managing the Hadoop configurations
 */

public class HadoopConfigurationsOptionsDialog extends AbstractDialog implements ServerInformationChangeListener {

    private static final long serialVersionUID = 1L;

    private final MutableServerInformationCatalog _serverInformationCatalog;
    private final DCPanel _connectionsConfigurationsPanel;
    private final WindowContext _windowContext;
    private final JButton _addNamenodeConnection;
    private final JButton _addDirectoryBasedConnection;

    @Inject
    public HadoopConfigurationsOptionsDialog(WindowContext windowContext, DataCleanerConfiguration configuration,
            UserPreferences userPreferences) {
        super(windowContext, ImageManager.get().getImage(IconUtils.FILE_HDFS));

        _windowContext = windowContext;
        _serverInformationCatalog = (MutableServerInformationCatalog) configuration.getServerInformationCatalog();
        _connectionsConfigurationsPanel = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        _addNamenodeConnection = WidgetFactory.createDefaultButton("Add Namenode", IconUtils.ACTION_ADD_DARK);
        _addDirectoryBasedConnection = WidgetFactory.createDefaultButton("Add Directory", IconUtils.ACTION_ADD_DARK);

        _addNamenodeConnection.addActionListener(e -> {
            final HadoopConnectionToNamenodeDialog hadoopConnectionToNamenodeDialog = new HadoopConnectionToNamenodeDialog(
                    _windowContext, null, _serverInformationCatalog);
            hadoopConnectionToNamenodeDialog.setVisible(true);
        });

        _addDirectoryBasedConnection.addActionListener(e -> {
            final HadoopDirectoryConfigurationDialog hadoopDirectoryConfigurationDialog = new HadoopDirectoryConfigurationDialog(
                    _windowContext, null, _serverInformationCatalog);
            hadoopDirectoryConfigurationDialog.setVisible(true);
        });
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
        
        WidgetUtils.addToGridBag(DCPanel.flow(Alignment.RIGHT, _addNamenodeConnection, _addDirectoryBasedConnection), panel, 0, row);
        row++;

        final String[] serverNames = _serverInformationCatalog.getServerNames();
        for (int i = 0; i < serverNames.length; i++) {
            final String serverName = serverNames[i];
            final ServerInformation server = _serverInformationCatalog.getServer(serverName);
            final HadoopClusterPanel clusterPanel = new HadoopClusterPanel(_windowContext, server,
                    _serverInformationCatalog);
//            if (serverName.equals(HadoopResource.DEFAULT_CLUSTERREFERENCE)) {
//                _connectionPanels.add(0, clusterPanel);
//            } else {
//                _connectionPanels.add(clusterPanel);
//            }
            WidgetUtils.addToGridBag(clusterPanel, panel, 0, row + 1, 1.0, 0.0);
            row++;
        }

        return panel;
    }

    public String getWindowTitle() {
        return "Hadoop clusters";
    }

    @Override
    protected boolean isWindowResizable() {
        return true;
    }

    @Override
    protected String getBannerTitle() {
        return getWindowTitle();
    }

    @Override
    protected int getDialogWidth() {
        return 600;
    }

    @Override
    protected JComponent getDialogContent() {
        final JButton closeButton = WidgetFactory.createPrimaryButton("Close", IconUtils.ACTION_CLOSE_BRIGHT);
        closeButton.addActionListener(e -> {
            HadoopConfigurationsOptionsDialog.this.close();
            // _userPreferences.save();
            // TODO: see where can we save the server information catalog.
        });
        
        updateClusterList();
        
        final DCPanel panel = new DCPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new DescriptionLabel("For DataCleaner to connect to Apache Hadoop information is required about the cluster to connect and execute jobs on it. By default you can use the HADOOP_CONF_DIR and YARN_CONF_DIR environment variables, or you can register a custom cluster using the options available in this dialog."), BorderLayout.NORTH);
        panel.add(WidgetUtils.scrolleable(_connectionsConfigurationsPanel), BorderLayout.CENTER);
        panel.add(DCPanel.flow(Alignment.CENTER, closeButton), BorderLayout.SOUTH);
        panel.setPreferredSize(getDialogWidth(), 500);
        
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

    public static void main(String[] args) throws Exception {
        LookAndFeelManager.get().init();
        final List<ServerInformation> servers = new ArrayList<>();
        servers.add(new EnvironmentBasedHadoopClusterInformation(HadoopResource.DEFAULT_CLUSTERREFERENCE,
                "hadoop conf dir"));

        servers.add(new DirectoryBasedHadoopClusterInformation("directory", "directopry set up",
                "C:\\Users\\claudiap\\git\\vagrant-vms\\bigdatavm\\yarn_conf_client",
                "C:\\Users\\claudiap\\git\\vagrant-vms\\bigdatavm\\yarn_conf_client"));

        servers.add(new DirectConnectionHadoopClusterInformation("namenode", null, new URI(
                "hdfs://192.168.0.200:9000/")));
        servers.add(new DirectConnectionHadoopClusterInformation("namenode2", "directconnection", new URI(
                "hdfs://192.168.0.200:9000/")));
        final ServerInformationCatalog serverInformationCatalog = new ServerInformationCatalogImpl(servers);

        final MutableServerInformationCatalog mutableServerInformationCatalog = new MutableServerInformationCatalog(
                serverInformationCatalog, null);

        final UserPreferencesImpl userPreferencesImpl = new UserPreferencesImpl(null);
        final WindowContext windowContext = new DCWindowContext(null, null, null);

        final DataCleanerConfiguration dcConfig = new DataCleanerConfigurationImpl(null, null, null, null,
                mutableServerInformationCatalog);
        final HadoopConfigurationsOptionsDialog hadoopConfigurationDialog = new HadoopConfigurationsOptionsDialog(
                windowContext, dcConfig, userPreferencesImpl);
        hadoopConfigurationDialog.setVisible(true);
    }
}
