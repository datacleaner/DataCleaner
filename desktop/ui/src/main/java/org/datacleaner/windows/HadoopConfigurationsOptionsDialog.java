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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.Resource;
import org.datacleaner.bootstrap.DCWindowContext;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DatastoreXmlExternalizer;
import org.datacleaner.configuration.ServerInformation;
import org.datacleaner.configuration.ServerInformationCatalog;
import org.datacleaner.configuration.ServerInformationCatalogImpl;
import org.datacleaner.panels.DCBannerPanel;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.panels.HadoopDirectConnectionPanel;
import org.datacleaner.panels.HadoopDirectoryConfigurationPanel;
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
import org.jfree.ui.tabbedui.VerticalLayout;

/**
 * Dialog for managing the Hadoop configurations
 */

public class HadoopConfigurationsOptionsDialog extends AbstractWindow  implements ServerInformationChangeListener{

    private static final long serialVersionUID = 1L;

    private final ImageManager imageManager = ImageManager.get();
    private final UserPreferences _userPreferences;
    private final DataCleanerConfiguration _configuration;
    private final MutableServerInformationCatalog _serverInformationCatalog;
    private final DCPanel _defaultConfigurationPanel;
    private final JPanel _directoriesConfigurationsPanel;
    private DCPanel _directConnectionsConfigurationsPanel;
    private final WindowContext _windowContext; 
    private final List<HadoopDirectoryConfigurationPanel> _directoriesConfigurationsPanels; 
    private final List<HadoopDirectConnectionPanel> _directConnectionsPanels; 
    public static final int WIDTH_CONTENT = 400;
    public static final int MARGIN_LEFT = 20;


    @Inject
    public HadoopConfigurationsOptionsDialog(WindowContext windowContext, DataCleanerConfiguration configuration,
            UserPreferences userPreferences, final MutableServerInformationCatalog serverInformationCatalog) {
        super(windowContext);

        _windowContext = windowContext; 
        _userPreferences = userPreferences;
        _configuration = configuration;
        _serverInformationCatalog = serverInformationCatalog;
        _defaultConfigurationPanel = getDefaultConfigurationPanel();
         _directoriesConfigurationsPanels = new ArrayList<>(); 
        _directoriesConfigurationsPanel = new DCPanel().setTitledBorder("Load configuration from directories:"); 
        _directConnectionsPanels = new ArrayList<>(); 
        _directConnectionsConfigurationsPanel = new DCPanel().setTitledBorder("Direct connections to namenode"); 
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

    private DCPanel getDefaultConfigurationPanel() {

        final DCPanel defaultConfigPanel = new DCPanel().setTitledBorder(
                "Default configuration HADOOP_CONF_DIR/YARN_CONF_DIR");
        defaultConfigPanel.setLayout(new GridBagLayout());
        final DCLabel label = DCLabel.dark("Environment variables:");
        final EnvironmentBasedHadoopClusterInformation defaultServer = (EnvironmentBasedHadoopClusterInformation) _serverInformationCatalog
                .getServer(HadoopResource.DEFAULT_CLUSTERREFERENCE);
        final String[] directories = defaultServer.getDirectories();
        JList<String> list = null;
        if (directories.length > 0) {
            list = new JList<String>(directories);
        }

        defaultConfigPanel.add(label);
        WidgetUtils.addToGridBag(label, defaultConfigPanel, 0, 0, GridBagConstraints.WEST);
        if (list != null) {
            WidgetUtils.addToGridBag(list, defaultConfigPanel, 1, 1, 1, 1, GridBagConstraints.CENTER, 4, 1, 0);
        } else {
            WidgetUtils.addToGridBag(DCLabel.dark("None set"), defaultConfigPanel, 0, 0, GridBagConstraints.CENTER);
        }

        return defaultConfigPanel;
    }

    private DCPanel getDirectConnectionsConfigurationsPanel() {
        final DCPanel directConnectionsPanel = new DCPanel();
        directConnectionsPanel.setLayout(new GridBagLayout());
        
        final DCLabel subTitleLabel = DCLabel.dark("Connections:");
        subTitleLabel.setFont(WidgetUtils.FONT_HEADER2);
        
        WidgetUtils.addToGridBag(subTitleLabel, directConnectionsPanel, 1, 0, 1.0, 1.0);
        
        final String[] serverNames = _serverInformationCatalog.getServerNames();
        for (int i = 0; i < serverNames.length; i++) {
            if (_serverInformationCatalog.getServer(serverNames[i]) instanceof DirectConnectionHadoopClusterInformation) {
                // create panel with this server; 
                DirectConnectionHadoopClusterInformation server = (DirectConnectionHadoopClusterInformation) _serverInformationCatalog.getServer(serverNames[i]); 
                final HadoopDirectConnectionPanel panel = new HadoopDirectConnectionPanel(_windowContext, server.getName(), server.getNameNodeUri(), server.getDescription(), server,_serverInformationCatalog);
                _directConnectionsPanels.add(panel);  
            }
        }
        
        int row = 0; 
        for (row = 0; row <_directConnectionsPanels.size(); row++){
            // +1  to the grid positions because a label was added before
            WidgetUtils.addToGridBag(_directConnectionsPanels.get(row), directConnectionsPanel, 1, row + 1, 1.0, 1.0);
        }
        
        final JButton addButton = WidgetFactory.createDefaultButton("Add"); 
        addButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                final HadoopConnectionToNamenodeDialog directConnectionDialog = new HadoopConnectionToNamenodeDialog(_windowContext, null,  _serverInformationCatalog);
                directConnectionDialog.setVisible(true);
            }
        });
        
        WidgetUtils.addToGridBag(addButton, directConnectionsPanel, 1, ++row, 1.0, 1.0); 
        return directConnectionsPanel;
    }

    private DCPanel getDirectoriesConfigurationsPanel() {

        final DCPanel directoryConfigurationPanel = new DCPanel(); 
        directoryConfigurationPanel.setLayout(new GridBagLayout());
        
        final DCLabel label = DCLabel.dark("Existing Directories:");
        label.setFont(WidgetUtils.FONT_HEADER2);
       
        final JPanel centerPanel = new DCPanel();
        centerPanel.setLayout(new VerticalLayout(true));
        createDirectoriesConfigurationListPanel(centerPanel);
        
        for (int i=0; i<_directoriesConfigurationsPanels.size(); i++){
            centerPanel.add(_directoriesConfigurationsPanels.get(i)); 
        }
        
        final JButton addConfiguration = WidgetFactory.createDefaultButton("Add");
        addConfiguration.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                final HadoopDirectoryConfigurationPanel newDirConfigPanel = new HadoopDirectoryConfigurationPanel(null,_serverInformationCatalog, centerPanel);
                _directoriesConfigurationsPanels.add(newDirConfigPanel); 
                centerPanel.add(newDirConfigPanel); 
            }
        });
        centerPanel.addContainerListener(new ContainerListener() {
            @Override
            public void componentRemoved(ContainerEvent e) {
                directoryConfigurationPanel.revalidate();
                directoryConfigurationPanel.repaint();
            }
            
            @Override
            public void componentAdded(ContainerEvent e) {
                directoryConfigurationPanel.revalidate();
                directoryConfigurationPanel.repaint();
            }
        });
        
        WidgetUtils.addToGridBag(label, directoryConfigurationPanel, 0, 0, 1, 1, GridBagConstraints.WEST, 4, 1.0, 0.0);
        WidgetUtils.addToGridBag(centerPanel, directoryConfigurationPanel, 0, 1, 1, 1, GridBagConstraints.WEST, 4, 1.0, 0.0);
        WidgetUtils.addToGridBag(addConfiguration, directoryConfigurationPanel, 0, 2, 0, 0, GridBagConstraints.SOUTH, 4, 1.0, 0.0);
        
        return directoryConfigurationPanel;
    }

    
    /**
     * Create a list with directory configurations panels
     * @param parentPanel
     */
    private void createDirectoriesConfigurationListPanel(JPanel parentPanel) {
     
        final String[] serverNames = _serverInformationCatalog.getServerNames();
        for (int i = 0; i < serverNames.length; i++) {
            if (_serverInformationCatalog.getServer(serverNames[i]).getClass().equals(
                    DirectoryBasedHadoopClusterInformation.class)) {
                // create panel with this server;
                final DirectoryBasedHadoopClusterInformation server = (DirectoryBasedHadoopClusterInformation) _serverInformationCatalog
                        .getServer(serverNames[i]);
                _directoriesConfigurationsPanels.add(new HadoopDirectoryConfigurationPanel(server,_serverInformationCatalog, parentPanel)); 
            }
        }
        
    }

    @Override
    public String getWindowTitle() {
        return "Hadoop configurations";
    }

    @Override
    public Image getWindowIcon() {
        return imageManager.getImageIcon(IconUtils.FILE_HDFS).getImage();
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
                HadoopConfigurationsOptionsDialog.this.dispose();
                // _userPreferences.save();
                //TODO: see where can we save the server information catalog. 
            }
        });

        final DCPanel buttonPanel = DCPanel.flow(Alignment.CENTER, closeButton);
        final DCBannerPanel banner = new DCBannerPanel("Set Hadoop Configurations");
        final DCPanel outerPanel = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        outerPanel.setLayout(new BorderLayout());
        outerPanel.setBackground(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        outerPanel.add(banner, BorderLayout.NORTH);
        
        final DCPanel contentPanel = new DCPanel();
        contentPanel.setLayout(new VerticalLayout());
        contentPanel.add(_defaultConfigurationPanel);
        _directoriesConfigurationsPanel.setLayout(new BorderLayout());
        _directoriesConfigurationsPanel.add(getDirectoriesConfigurationsPanel(), BorderLayout.CENTER); 
        contentPanel.add(_directoriesConfigurationsPanel);
        _directConnectionsConfigurationsPanel.setLayout(new BorderLayout());
        _directConnectionsConfigurationsPanel.add(getDirectConnectionsConfigurationsPanel(), BorderLayout.CENTER); 
        contentPanel.add(_directConnectionsConfigurationsPanel);
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        final JScrollPane scroll = WidgetUtils.scrolleable(contentPanel); 
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        outerPanel.add(scroll, BorderLayout.CENTER);
        outerPanel.add(buttonPanel, BorderLayout.SOUTH);
        outerPanel.setPreferredSize(700, 700);
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
        if (serverInformation instanceof DirectConnectionHadoopClusterInformation) {
            _directConnectionsPanels.clear();
            _directConnectionsConfigurationsPanel.removeAll();
            _directConnectionsConfigurationsPanel.add(getDirectConnectionsConfigurationsPanel());
            _directConnectionsConfigurationsPanel.revalidate();
            _directConnectionsConfigurationsPanel.repaint();
        } else if (serverInformation.getClass().equals(DirectoryBasedHadoopClusterInformation.class)) {
            _directoriesConfigurationsPanels.clear();
            _directoriesConfigurationsPanel.removeAll();
            _directoriesConfigurationsPanel.add(getDirectoriesConfigurationsPanel());
            _directoriesConfigurationsPanel.revalidate();
            _directoriesConfigurationsPanel.repaint();
        }
    }

    public static void main(String[] args) throws Exception {
        LookAndFeelManager.get().init();
        final List<ServerInformation> servers = new ArrayList<>();
        servers.add(new EnvironmentBasedHadoopClusterInformation(HadoopResource.DEFAULT_CLUSTERREFERENCE,
                "hadoop conf dir"));

        servers.add(new DirectoryBasedHadoopClusterInformation("directory", "directopry set up",
                "C:\\Users\\claudiap\\git\\vagrant-vms\\bigdatavm\\yarn_conf_client",  "C:\\Users\\claudiap\\git\\vagrant-vms\\bigdatavm\\yarn_conf_client"));

        servers.add(new DirectConnectionHadoopClusterInformation("namenode", "directconnection", new URI(
                "hdfs://192.168.0.200:9000/")));
        servers.add(new DirectConnectionHadoopClusterInformation("namenode2", "directconnection", new URI(
                "hdfs://192.168.0.200:9000/")));
        final ServerInformationCatalog serverInformationCatalog = new ServerInformationCatalogImpl(servers);
        JFrame frame = new JFrame();
        frame.setVisible(false);
        frame.pack();
        frame.dispose();
        final Resource resource = new FileResource(new File("C:\\Users\\claudiap\\Desktop\\testconf.xml"));
        MutableServerInformationCatalog mutableServerInformationCatalog = new MutableServerInformationCatalog(serverInformationCatalog, new DatastoreXmlExternalizer(resource)); 

        final UserPreferencesImpl userPreferencesImpl = new UserPreferencesImpl(null);
        WindowContext windowContext = new DCWindowContext(null, null, null);

        final HadoopConfigurationsOptionsDialog hadoopConfigurationDialog = new HadoopConfigurationsOptionsDialog(windowContext, null,
                userPreferencesImpl, mutableServerInformationCatalog);
        hadoopConfigurationDialog.setVisible(true);
        hadoopConfigurationDialog.show();
        hadoopConfigurationDialog.pack();
    }

   

}
