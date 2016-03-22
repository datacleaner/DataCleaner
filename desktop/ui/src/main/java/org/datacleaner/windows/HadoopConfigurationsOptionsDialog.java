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
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.datacleaner.bootstrap.DCWindowContext;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.ServerInformation;
import org.datacleaner.configuration.ServerInformationCatalog;
import org.datacleaner.configuration.ServerInformationCatalogImpl;
import org.datacleaner.panels.DCBannerPanel;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.panels.HadoopConnectionPanel;
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
import org.datacleaner.widgets.DCLabel;

/**
 * Dialog for managing the Hadoop configurations
 */

public class HadoopConfigurationsOptionsDialog extends AbstractWindow implements ServerInformationChangeListener {

    private static final long serialVersionUID = 1L;

    private final ImageManager imageManager = ImageManager.get();
    private final MutableServerInformationCatalog _serverInformationCatalog;
    private final DCPanel _connectionsConfigurationsPanel;
    private final WindowContext _windowContext;
    private final List<HadoopConnectionPanel> _connectionPanels;
    private final JButton _addNamenodeConnection;
    private final JButton _addDirectoryBasedConnection;

    @Inject
    public HadoopConfigurationsOptionsDialog(WindowContext windowContext, DataCleanerConfiguration configuration,
            UserPreferences userPreferences) {
        super(windowContext);

        _windowContext = windowContext;
        _serverInformationCatalog = (MutableServerInformationCatalog) configuration.getServerInformationCatalog();
        _connectionPanels = new LinkedList<>();
        _connectionsConfigurationsPanel = new DCPanel();
        _addNamenodeConnection = WidgetFactory.createPrimaryButton("Add Namenode", IconUtils.ACTION_ADD);
        _addDirectoryBasedConnection = WidgetFactory.createPrimaryButton("Add Directory", IconUtils.ACTION_ADD);

        _addNamenodeConnection.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final HadoopConnectionToNamenodeDialog hadoopConnectionToNamenodeDialog = new HadoopConnectionToNamenodeDialog(
                        _windowContext, null, _serverInformationCatalog);
                hadoopConnectionToNamenodeDialog.setVisible(true);
            }
        });

        _addDirectoryBasedConnection.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final HadoopDirectoryConfigurationDialog hadoopDirectoryConfigurationDialog = new HadoopDirectoryConfigurationDialog(
                        _windowContext, null, _serverInformationCatalog);
                hadoopDirectoryConfigurationDialog.setVisible(true);
            }
        });
    }

    @Override
    public void onAdd(ServerInformation serverInformation) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                updatePanel(serverInformation);
            }

        });
    }

    @Override
    public void onRemove(ServerInformation serverInformation) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                updatePanel(serverInformation);
            }
        });
    }

    private DCPanel getConnectionsConfigurationsPanel() {
        final DCPanel directConnectionsPanel = new DCPanel();
        directConnectionsPanel.setLayout(new GridBagLayout());

        final String[] serverNames = _serverInformationCatalog.getServerNames();
        for (int i = 0; i < serverNames.length; i++) {
            final String serverName = serverNames[i];
            // create panel with this server;
            final ServerInformation server = _serverInformationCatalog.getServer(serverName);
            final HadoopConnectionPanel panel = new HadoopConnectionPanel(_windowContext, server,
                    _serverInformationCatalog);
            if (serverName.equals(HadoopResource.DEFAULT_CLUSTERREFERENCE)) {
                _connectionPanels.add(0, panel);
            } else {
                _connectionPanels.add(panel);
            }
        }

        int row = 0;
        for (row = 0; row < _connectionPanels.size(); row++) {
            WidgetUtils.addToGridBag(_connectionPanels.get(row), directConnectionsPanel, 1, row + 1);
        }

        return directConnectionsPanel;
    }

    @Override
    public String getWindowTitle() {
        return "Hadoop configurations";
    }

    @Override
    public Image getWindowIcon() {
        return imageManager.getImage(IconUtils.MENU_OPTIONS);
    }

    @Override
    protected boolean isWindowResizable() {
        return true;
    }

    @Override
    protected boolean isCentered() {
        return true;
    }

    @Override
    protected JComponent getWindowContent() {
        final JButton closeButton = WidgetFactory.createPrimaryButton("Close", IconUtils.ACTION_CLOSE_BRIGHT);
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HadoopConfigurationsOptionsDialog.this.close();
                // _userPreferences.save();
                // TODO: see where can we save the server information catalog.
            }
        });

        final DCPanel buttonPanel = DCPanel.flow(Alignment.CENTER, closeButton);
        final Image hadoopImage = imageManager.getImage(IconUtils.FILE_HDFS);
        final DCBannerPanel banner = new DCBannerPanel(hadoopImage, "Set Hadoop Clusters");
        final DCPanel outerPanel = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        outerPanel.setLayout(new BorderLayout());
        outerPanel.add(banner, BorderLayout.NORTH);

        final DCPanel contentPanel = new DCPanel();
        contentPanel.setLayout(new BorderLayout());
        final DCLabel subtitle = DCLabel.dark("Existing configurations:");
        subtitle.setFont(WidgetUtils.FONT_HEADER2);
        contentPanel.add(subtitle, BorderLayout.NORTH);
        _connectionsConfigurationsPanel.setLayout(new BorderLayout());
        _connectionsConfigurationsPanel.add(getConnectionsConfigurationsPanel(), BorderLayout.CENTER);
        contentPanel.add(_connectionsConfigurationsPanel, BorderLayout.CENTER);
        contentPanel.add(DCPanel.flow(Alignment.CENTER, _addNamenodeConnection, _addDirectoryBasedConnection),
                BorderLayout.SOUTH);
        contentPanel.setBorder(WidgetUtils.BORDER_EMPTY);

        final JScrollPane scroll = WidgetUtils.scrolleable(contentPanel);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        outerPanel.add(scroll, BorderLayout.CENTER);
        outerPanel.add(buttonPanel, BorderLayout.SOUTH);
        outerPanel.setPreferredSize(600, 500);
        return outerPanel;
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

    private void updatePanel(ServerInformation serverInformation) {
        _connectionPanels.clear();
        _connectionsConfigurationsPanel.removeAll();
        _connectionsConfigurationsPanel.add(getConnectionsConfigurationsPanel());
        _connectionsConfigurationsPanel.revalidate();
        _connectionsConfigurationsPanel.repaint();
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
