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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
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
import org.datacleaner.panels.HadoopDirectoryConfigurationPanel;
import org.datacleaner.panels.HadoopDirectConnectionPanel;
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
import org.jdesktop.swingx.JXTextField;
import org.jfree.ui.tabbedui.VerticalLayout;

public class HadoopConfigurationsDialog extends AbstractWindow  implements ServerInformationChangeListener{

    private static final long serialVersionUID = 1L;

    private final ImageManager imageManager = ImageManager.get();
    private final UserPreferences _userPreferences;
    private final DataCleanerConfiguration _configuration;
    private final MutableServerInformationCatalog _serverInformationCatalog;
    private final DCPanel _defaultConfigurationPanel;
    private final JPanel _directoriesConfigurationsPanel;
    private DCPanel _directConnectionsConfigurationsPanel;
    private final WindowContext _windowContext; 
    private final List<HadoopDirectoryConfigurationPanel> _directoriesConfigurationsListPanels; 
    private final List<HadoopDirectConnectionPanel> _directConnectionsPanels; 

    // private final JXTextField _columnTextField;

    @Inject
    public HadoopConfigurationsDialog(WindowContext windowContext, DataCleanerConfiguration configuration,
            UserPreferences userPreferences, final MutableServerInformationCatalog serverInformationCatalog) {
        super(windowContext);

        _windowContext = windowContext; 
        _userPreferences = userPreferences;
        _configuration = configuration;
        _serverInformationCatalog = serverInformationCatalog;
        _defaultConfigurationPanel = getDefaultConfigurationPanel();
         _directoriesConfigurationsListPanels = new ArrayList<>(); 
        _directoriesConfigurationsPanel = getMainDirectoriesConfigurationsPanel();
        _directConnectionsPanels = new ArrayList<>(); 
        _directConnectionsConfigurationsPanel = new DCPanel(); 

        
    }
    
    @Override
    public void onAdd(ServerInformation serverInformation) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (serverInformation instanceof DirectConnectionHadoopClusterInformation) {
                    updateDirectConnectionsPanel(); 
                }
                
                if (serverInformation.getClass().equals(DirectoryBasedHadoopClusterInformation.class)){
                    System.out.println("Added directory server:" + serverInformation.getName());
                    updateDirectoryConnectionPanel(); 
                }
            }

        });
    }
   

    @Override
    public void onRemove(ServerInformation serverInformation) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (serverInformation instanceof DirectConnectionHadoopClusterInformation) {
                    updateDirectConnectionsPanel();
                }
            }
        });
        
    }

    private void updateDirectoryConnectionPanel() {
       
        
    }
    private void updateDirectConnectionsPanel() {
        _directConnectionsPanels.clear();
        _directConnectionsConfigurationsPanel.removeAll();
        _directConnectionsConfigurationsPanel.add(getDirectConnectionsConfigurationsPanel());
        _directConnectionsConfigurationsPanel.revalidate();
        _directConnectionsConfigurationsPanel.repaint();
    }
    
    private DCPanel getDefaultConfigurationPanel() {
        
        final DCPanel defaultConfigPanel = new DCPanel().setTitledBorder(
                "Default configuration HADOOP_CONF_DIR/YARN_CONF_DIR");
        defaultConfigPanel.setLayout(new GridBagLayout());
        final DCLabel directoryConfigurationLabel1 = DCLabel.dark("Directory 1:");
        final JXTextField directoryConfigurationTextField1 =  WidgetFactory.createTextField("<none>");  
        final DCLabel directoryConfigurationLabel2 = DCLabel.dark("Directory 2:");
        final JXTextField directoryConfigurationTextField2 = WidgetFactory.createTextField("<none>");
        
        final EnvironmentBasedHadoopClusterInformation defaultServer = (EnvironmentBasedHadoopClusterInformation) _serverInformationCatalog
                .getServer(HadoopResource.DEFAULT_CLUSTERREFERENCE);
        final String[] directories = defaultServer.getDirectories();
        if (directories.length > 0) {
            directoryConfigurationTextField1.setText(directories[0]);
            if (directories.length == 2) {
                directoryConfigurationTextField2.setText(directories[1]);
            }
        }

        directoryConfigurationTextField1.setEnabled(false);
        directoryConfigurationTextField2.setEnabled(false);
        directoryConfigurationTextField1.setToolTipText(null);
        directoryConfigurationTextField2.setToolTipText(null);

        WidgetUtils.addToGridBag(directoryConfigurationLabel1, defaultConfigPanel, 0, 0, GridBagConstraints.WEST);
        WidgetUtils.addToGridBag(directoryConfigurationTextField1, defaultConfigPanel, 1, 0, 1, 1, GridBagConstraints.EAST,4, 1, 0);
        WidgetUtils.addToGridBag(directoryConfigurationLabel2, defaultConfigPanel, 0, 1,GridBagConstraints.WEST);
        WidgetUtils.addToGridBag(directoryConfigurationTextField2, defaultConfigPanel, 1, 1, 1, 1, GridBagConstraints.EAST, 4, 1, 0);

        return defaultConfigPanel;
    }

    private DCPanel getDirectConnectionsConfigurationsPanel() {
        final DCPanel directConnectionsPanel = new DCPanel().setTitledBorder("Direct connections to namenode");
        directConnectionsPanel.setLayout(new GridBagLayout());
        final DCLabel subTitleLabel = DCLabel.dark("Connections:");
        subTitleLabel.setFont(WidgetUtils.FONT_HEADER2);
        
        WidgetUtils.addToGridBag(subTitleLabel, directConnectionsPanel, 1, 0, 1.0, 0.0);
        
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
            WidgetUtils.addToGridBag(_directConnectionsPanels.get(row), directConnectionsPanel, 1, row + 1, 1.0, 0.0);
        }
        
        final JButton addButton = WidgetFactory.createDefaultButton("Add"); 
        addButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                final HadoopConnectionToNamenodeDialog directConnectionDialog = new HadoopConnectionToNamenodeDialog(_windowContext, null,  _serverInformationCatalog);
                directConnectionDialog.setVisible(true);
            }
        });
        
        WidgetUtils.addToGridBag(addButton, directConnectionsPanel, 1, ++row, 1.0, 0.0); 
        return directConnectionsPanel;
    }

    private JPanel getMainDirectoriesConfigurationsPanel() {

        final DCPanel directoryConfigurationPanel = new DCPanel().setTitledBorder("Load configuration from directories:");
        final JScrollPane scrollPane = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        directoryConfigurationPanel.add(scrollPane); 
        directoryConfigurationPanel.setLayout(new GridBagLayout());
        
        final DCLabel label = DCLabel.dark("Existing Directories:");
        label.setFont(WidgetUtils.FONT_HEADER2);
       
        final JPanel centerPanel = new DCPanel();
         centerPanel.setLayout(new VerticalLayout());
        createDirectoriesConfigurationListPanel(centerPanel);
        
        for (int i=0; i<_directoriesConfigurationsListPanels.size(); i++){
            centerPanel.add(_directoriesConfigurationsListPanels.get(i)); 
        }
        
        final JButton addPath = WidgetFactory.createDefaultButton("Add");
        addPath.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                final HadoopDirectoryConfigurationPanel newDirConfigPanel = new HadoopDirectoryConfigurationPanel(null,_serverInformationCatalog, centerPanel);
                _directoriesConfigurationsListPanels.add(newDirConfigPanel); 
                centerPanel.add(newDirConfigPanel); 
            }
        });
        centerPanel.addContainerListener(new ContainerListener() {
            
            @Override
            public void componentRemoved(ContainerEvent e) {
                revalidate();
                repaint();
            }
            
            @Override
            public void componentAdded(ContainerEvent e) {
                revalidate();
                repaint();
            }
        });
        directoryConfigurationPanel.revalidate();
       
        WidgetUtils.addToGridBag(label, directoryConfigurationPanel, 0, 0, 1, 1, GridBagConstraints.WEST, 4, 1, 0);
        WidgetUtils.addToGridBag(centerPanel, directoryConfigurationPanel, 0, 1, 1, 1, GridBagConstraints.WEST, 4, 1, 0);
        WidgetUtils.addToGridBag(addPath, directoryConfigurationPanel, 0, 2, 0, 0, GridBagConstraints.SOUTH);
        
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
                _directoriesConfigurationsListPanels.add(new HadoopDirectoryConfigurationPanel(server,_serverInformationCatalog, parentPanel)); 
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
                HadoopConfigurationsDialog.this.dispose();
                // _userPreferences.save();
            }
        });

        final DCPanel buttonPanel = DCPanel.flow(Alignment.CENTER, closeButton);

        final DCBannerPanel banner = new DCBannerPanel("Set Hadoop Configurations");

        final DCPanel panel = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        panel.setLayout(new BorderLayout());
        panel.add(banner, BorderLayout.NORTH);

        final DCPanel contentPanel = new DCPanel();
        contentPanel.setLayout(new VerticalLayout());
        contentPanel.add(_defaultConfigurationPanel);
        contentPanel.add(_directoriesConfigurationsPanel);
        _directConnectionsConfigurationsPanel.add(getDirectConnectionsConfigurationsPanel()); 
        contentPanel.add(_directConnectionsConfigurationsPanel);
        //contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
       
        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        panel.setPreferredSize(900, 900);
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

        final HadoopConfigurationsDialog hadoopConfigurationDialog = new HadoopConfigurationsDialog(windowContext, null,
                userPreferencesImpl, mutableServerInformationCatalog);
        hadoopConfigurationDialog.setVisible(true);
        hadoopConfigurationDialog.show();
        hadoopConfigurationDialog.pack();
    }

   

}
