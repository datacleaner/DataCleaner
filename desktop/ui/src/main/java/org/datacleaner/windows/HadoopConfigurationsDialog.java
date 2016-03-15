package org.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
import org.datacleaner.server.DirectConnectionHadoopClusterInformation;
import org.datacleaner.server.DirectoryBasedHadoopClusterInformation;
import org.datacleaner.server.EnvironmentBasedHadoopClusterInformation;
import org.datacleaner.user.MutableServerInformationCatalog;
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
import org.datacleaner.widgets.FileSelectionListener;
import org.datacleaner.widgets.FilenameTextField;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXTextField;
import org.jfree.ui.tabbedui.VerticalLayout;

public class HadoopConfigurationsDialog extends AbstractWindow {

    public class DirectConnectionToNamenodeDialog extends AbstractDialog{
       
        private final DirectConnectionHadoopClusterInformation _directConnection;
        private final JXTextField _nameTextField;
        private final JXTextField _hostTextField; 
        private final JXTextField _portTextField; 
        private final JXTextField _descriptionTextField;
        private final JButton _saveButton;
        private final JButton _cancelButton;
        
      
        public DirectConnectionToNamenodeDialog(WindowContext windowContext, DirectConnectionHadoopClusterInformation directConnection ) {
            super(windowContext);
            _directConnection = directConnection; 
            _nameTextField = WidgetFactory.createTextField("My connection"); 
            _hostTextField = WidgetFactory.createTextField("localhost"); 
            _portTextField = WidgetFactory.createTextField("9000");
            _descriptionTextField = WidgetFactory.createTextField("test environment");
            
            final String saveButtonText = _directConnection == null ? "Register connection" : "Save connection";
            _saveButton = WidgetFactory.createPrimaryButton(saveButtonText, IconUtils.ACTION_SAVE_BRIGHT);
            _saveButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // add/create connection
                    dispose();
                }
            });

            _cancelButton = WidgetFactory.createDefaultButton("Cancel", IconUtils.ACTION_CANCEL);
            _cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    DirectConnectionToNamenodeDialog.this.close();
                }
            });
        }

        private static final long serialVersionUID = 1L;

        @Override
        public String getWindowTitle() {
            return "Connect to Hadoop";
        }

        @Override
        protected String getBannerTitle() {
            return "Connect directly to namenode";
        }

        @Override
        protected int getDialogWidth() {
            return 400;
        }

        @Override
        protected JComponent getDialogContent() {
            
           final DCPanel formPanel = new DCPanel();

           //TODO: Add description label 
           
           /*final JLabel descriptionLabel = DCLabel.bright("Create direct connection to Hadoop Namenode:");
           descriptionLabel.setFont(WidgetUtils.FONT_HEADER2);
           int row = 0;
           WidgetUtils.addToGridBag(descriptionLabel, formPanel, 0, row);
           */
          
           int row = 0; 
           WidgetUtils.addToGridBag(DCLabel.bright("Connection name:"), formPanel, 0, row);
           WidgetUtils.addToGridBag(_nameTextField, formPanel, 1, row);

           row++;
           WidgetUtils.addToGridBag(DCLabel.bright("Host:"), formPanel, 0, row);
           WidgetUtils.addToGridBag(_hostTextField, formPanel, 1, row);

           row++;
           WidgetUtils.addToGridBag(DCLabel.bright("Port:"), formPanel, 0, row);
           WidgetUtils.addToGridBag(_portTextField, formPanel, 1, row);
           
           row++;
           WidgetUtils.addToGridBag(DCLabel.bright("Description:"), formPanel, 0, row);
           WidgetUtils.addToGridBag(_descriptionTextField, formPanel, 1, row);
           
           
           final DCBannerPanel banner = new DCBannerPanel("Hadoop Direct Configurations");
           final DCPanel outerPanel = new DCPanel();
           outerPanel.setLayout(new BorderLayout());
           outerPanel.add(banner , BorderLayout.NORTH);
           outerPanel.add(formPanel, BorderLayout.CENTER);
           outerPanel.add(getButtonPanel(), BorderLayout.SOUTH);

           outerPanel.setPreferredSize(getDialogWidth(), 400);
           return outerPanel; 
        }
        
        private DCPanel getButtonPanel() {
            final DCPanel buttonPanel = new DCPanel();
            buttonPanel.setBorder(WidgetUtils.BORDER_EMPTY);
            buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 4, 0));
            buttonPanel.add(_saveButton);
            buttonPanel.add(_cancelButton);
            return buttonPanel;
        }
        
    }
    
    public class DirectoryPanel extends DCPanel {

        private static final long serialVersionUID = 1L;

        private final JLabel _label;
        private final FilenameTextField _directoryTextField;
        private File _directory;
        private JButton _removeButton; 
        private JPanel _parent; 
        private final int _pathNumber; 

        public DirectoryPanel(int pathNumber, File directory, JPanel parent, DirectoryBasedHadoopClusterInformation server) {
            _pathNumber = pathNumber;  
            _parent = parent; 
            _directory = directory;
            _label = DCLabel.dark("Path " + pathNumber + ":"); 
            if (directory == null){
            _directoryTextField = new FilenameTextField(_userPreferences.getConfiguredFileDirectory(), true);
            }else{
                _directoryTextField = new FilenameTextField(directory, true);
            }
            _directoryTextField.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            _directoryTextField.addFileSelectionListener(new FileSelectionListener() {

                @Override
                public void onSelected(FilenameTextField filenameTextField, File file) {
                    _directory = file;
                   
                   //TODO:Move this in other place
                    final List<String> paths = Arrays.asList(server.getDirectories());
                    paths.add(_directory.getPath()); 
                    ServerInformation serverInformation = new DirectoryBasedHadoopClusterInformation(server.getName(), server.getDescription(), paths.toArray(new String[paths.size()]));
                    _serverInformationCatalog.addServerInformation(serverInformation);
                }
            });

            if (_directory != null) {
                _directoryTextField.setFile(_directory);
            }
            
            _removeButton = WidgetFactory.createDefaultButton("",IconUtils.ACTION_DELETE);
           
            }

        public DCPanel getPanel() {
            final DCPanel dcPanel = new DCPanel();
            dcPanel.setLayout(new HorizontalLayout(10));
            dcPanel.add(_label);
            dcPanel.add(_directoryTextField);
            dcPanel.add(_removeButton);
            dcPanel.setBorder(WidgetUtils.BORDER_TOP_PADDING);

            _removeButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    _directoryListPanels.remove(_pathNumber);
                    _parent.remove(dcPanel);
                }
            });
            return dcPanel;
        }
        public File getDirectory() {
            return _directory;
        }
    }

    private static final long serialVersionUID = 1L;

    private final ImageManager imageManager = ImageManager.get();
    private final UserPreferences _userPreferences;
    private final DataCleanerConfiguration _configuration;
    private final MutableServerInformationCatalog _serverInformationCatalog;
    private final DCPanel _defaultConfigurationPanel;
    private DCPanel _directoriesConfigurationsPanel;
    private final DCPanel _directConnectionsConfigurationsPanel;
    private final WindowContext _windowContext; 
    private final List<DirectoryPanel> _directoryListPanels; 

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
        _directoryListPanels = new ArrayList<>(); 
        _directoriesConfigurationsPanel = getDirectoriesConfigurationsPanel();
        _directConnectionsConfigurationsPanel = getDirectConnectionsConfigurationsPanel();

        
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
        directConnectionsPanel.setLayout(new GridLayout());
        final DCLabel subTitleLabel = DCLabel.dark("Connections:");
        subTitleLabel.setFont(WidgetUtils.FONT_HEADER2);
        directConnectionsPanel.add(subTitleLabel);
        
        final String[] serverNames = _serverInformationCatalog.getServerNames();
        for (int i = 0; i < serverNames.length; i++) {
            if (_serverInformationCatalog.getServer(serverNames[i]) instanceof DirectConnectionHadoopClusterInformation) {
                // create panel with this server; 
                DirectConnectionHadoopClusterInformation server = (DirectConnectionHadoopClusterInformation) _serverInformationCatalog.getServer(serverNames[i]); 
               // add to Panel 
                System.out.println("Namenode Connection : " + server.getNameNodeUri());
            }
        }
        
        final JButton addButton = WidgetFactory.createDefaultButton("Add"); 
        addButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                DirectConnectionToNamenodeDialog directConnectionDialog = new DirectConnectionToNamenodeDialog(_windowContext, null); 
                directConnectionDialog.setVisible(true);
                
            }
        });
        
        directConnectionsPanel.add(addButton); 
        return directConnectionsPanel;
    }

    private DirectoryBasedHadoopClusterInformation getDirectoryBasedHadoopClusterInformation() {

        final String[] serverNames = _serverInformationCatalog.getServerNames();
        for (int i = 0; i < serverNames.length; i++) {
            if (_serverInformationCatalog.getServer(serverNames[i]).getClass().equals(
                    DirectoryBasedHadoopClusterInformation.class)) {
                // create panel with this server;
                final DirectoryBasedHadoopClusterInformation server = (DirectoryBasedHadoopClusterInformation) _serverInformationCatalog
                        .getServer(serverNames[i]);

                return server;
            }
        }
        return null;
    }
    
    private List<DirectoryPanel> getDirectoriesListPanel(JPanel parent) {

        final DirectoryBasedHadoopClusterInformation server = getDirectoryBasedHadoopClusterInformation();
        final String[] directories = server.getDirectories();
        for (int j = 0; j < directories.length; j++) {
            final DirectoryPanel directoryPanel = new DirectoryPanel(j, new File(directories[j]), parent, server);
            _directoryListPanels.add(directoryPanel);
        }

        return _directoryListPanels;
    }
   
    private DCPanel getDirectoriesConfigurationsPanel() {

        final DCPanel directoryConfigurationPanel = new DCPanel().setTitledBorder(
                "Load configuration from directories:");
        directoryConfigurationPanel.setLayout(new GridBagLayout());

        final DCLabel label = DCLabel.dark("Existing Directories:");
        label.setFont(WidgetUtils.FONT_HEADER2);
       
        DCPanel centerPanel = new DCPanel();
        centerPanel.setLayout(new VerticalLayout());
        getDirectoriesListPanel(centerPanel);
        
        for (int i=0; i<_directoryListPanels.size(); i++){
            centerPanel.add(_directoryListPanels.get(i).getPanel()); 
        }
        
        final JButton addPath = WidgetFactory.createDefaultButton("Add");
        addPath.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                final DirectoryPanel newPathPanel = new DirectoryPanel(_directoryListPanels.size(), null, centerPanel);
                _directoryListPanels.add(newPathPanel); 
                centerPanel.add(newPathPanel.getPanel()); 
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
        
        WidgetUtils.addToGridBag(label, directoryConfigurationPanel, 0, 0, 1, 2, GridBagConstraints.NORTH, 4, 0, 0);
        WidgetUtils.addToGridBag(centerPanel, directoryConfigurationPanel, 1, 0, 1, 1, GridBagConstraints.WEST, 4, 0, 3);
        WidgetUtils.addToGridBag(addPath, directoryConfigurationPanel, 2, 1, 1, 1, GridBagConstraints.SOUTHEAST, 4, 0, 0);
        
        return directoryConfigurationPanel;
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
        contentPanel.add(_directConnectionsConfigurationsPanel);
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        panel.setPreferredSize(700, 700);
        return panel;
    }

    public static void main(String[] args) throws Exception {
        LookAndFeelManager.get().init();
        final List<ServerInformation> servers = new ArrayList<>();
        servers.add(new EnvironmentBasedHadoopClusterInformation(HadoopResource.DEFAULT_CLUSTERREFERENCE,
                "hadoop conf dir"));
        servers.add(new DirectoryBasedHadoopClusterInformation("directory", "directopry set up",
                "C:\\Users\\claudiap\\git\\vagrant-vms\\bigdatavm\\yarn_conf_client", "C:\\Users\\claudiap\\git\\vagrant-vms\\bigdatavm\\yarn_conf_client2"));
        servers.add(new DirectConnectionHadoopClusterInformation("namenode", "directconnection", new URI(
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
