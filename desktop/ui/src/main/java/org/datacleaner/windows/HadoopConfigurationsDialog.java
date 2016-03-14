package org.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.datacleaner.bootstrap.DCWindowContext;
import org.datacleaner.bootstrap.ExitActionListener;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.ServerInformation;
import org.datacleaner.configuration.ServerInformationCatalog;
import org.datacleaner.configuration.ServerInformationCatalogImpl;
import org.datacleaner.panels.DCBannerPanel;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.server.DirectConnectionHadoopClusterInformation;
import org.datacleaner.server.DirectoryBasedHadoopClusterInformation;
import org.datacleaner.server.EnvironmentBasedHadoopClusterInformation;
import org.datacleaner.server.HadoopClusterInformation;
import org.datacleaner.user.UserPreferences;
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

public class HadoopConfigurationsDialog  extends AbstractWindow {


    private static final long serialVersionUID = 1L;
    
    private final ImageManager imageManager = ImageManager.get();
    private final UserPreferences _userPreferences;
    private final DataCleanerConfiguration _configuration;
    private final ServerInformationCatalog _serverInformationCatalog; 
    private final DCPanel _defaultConfigurationPanel; 
    

   
   // private final JXTextField _columnTextField;

    @Inject
    public HadoopConfigurationsDialog(WindowContext windowContext, DataCleanerConfiguration configuration,
            UserPreferences userPreferences, final ServerInformationCatalog serverInformationCatalog) {
        super(windowContext);
        
        _userPreferences = userPreferences;
        _configuration = configuration;
        _serverInformationCatalog = serverInformationCatalog; 
        _defaultConfigurationPanel = getDefaultConfigurationPanel(); 
       
    }

    private DCPanel getDefaultConfigurationPanel() {
        final DCPanel defaultConfigPanel = new DCPanel().setTitledBorder("Default configuration HADOOP_CONF_DIR/YARN_CONF_DIR");
        defaultConfigPanel.setLayout(new GridLayout(2,2));
        final DCLabel directoryConfigurationLabel1 = DCLabel.dark("Directory 1:");
        final JXTextField directoryConfigurationTextField1 = WidgetFactory.createTextField();
        directoryConfigurationTextField1.setSize(400, 30);
        final DCLabel directoryConfigurationLabel2 = DCLabel.dark("Directory 2:");
        
        final JXTextField directoryConfigurationTextField2 = WidgetFactory.createTextField(); 
        final EnvironmentBasedHadoopClusterInformation defaultServer = (EnvironmentBasedHadoopClusterInformation) _serverInformationCatalog.getServer(HadoopResource.DEFAULT_CLUSTERREFERENCE);
        final String[] directories = defaultServer.getDirectories(); 
        if (directories.length > 0){
            directoryConfigurationTextField1.setText(directories[0]); 
           
            if (directories.length == 2){
                directoryConfigurationTextField2.setText(directories[1]); 
                
            }
        }
        
        directoryConfigurationTextField1.setEnabled(false); 
        directoryConfigurationTextField2.setEnabled(false);
        
        WidgetUtils.addToGridBag(directoryConfigurationLabel1,defaultConfigPanel, 0,0); 
        WidgetUtils.addToGridBag(directoryConfigurationTextField1,defaultConfigPanel, 1,0); 
        WidgetUtils.addToGridBag(directoryConfigurationLabel2,defaultConfigPanel, 0,1); 
        WidgetUtils.addToGridBag(directoryConfigurationTextField2,defaultConfigPanel, 1,1); 
        
        return defaultConfigPanel;
    }

    @Override
    public String getWindowTitle() {
        return "Hadoop configurations";
    }

    @Override
    public Image getWindowIcon() {
        return null;
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
                _userPreferences.save();
                HadoopConfigurationsDialog.this.dispose();
            }
        });

        final DCPanel buttonPanel = DCPanel.flow(Alignment.CENTER, closeButton);

        final DCBannerPanel banner = new DCBannerPanel("Set Hadoop Configurations");
   
        final DCPanel panel = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        panel.setLayout(new BorderLayout());
        panel.add(banner, BorderLayout.NORTH);
       
      
        
        panel.add(_defaultConfigurationPanel, BorderLayout.WEST); 
        panel.add(buttonPanel, BorderLayout.SOUTH);
        panel.setPreferredSize(700, 500);
        return panel;
    }


    public static void main(String[] args) throws Exception {
        LookAndFeelManager.get().init();
        final List<ServerInformation> servers = new ArrayList<>();
        servers.add(new EnvironmentBasedHadoopClusterInformation(HadoopResource.DEFAULT_CLUSTERREFERENCE,
                "hadoop conf dir"));
        servers.add(new DirectoryBasedHadoopClusterInformation("directory", "directopry set up",
                "C:\\Users\\claudiap\\git\\vagrant-vms\\bigdatavm\\yarn_conf_client"));
        servers.add(new DirectConnectionHadoopClusterInformation("namenode", "directconnection", new URI(
                "hdfs://192.168.0.200:9000/")));
        final ServerInformationCatalog serverInformationCatalog = new ServerInformationCatalogImpl(servers);
        JFrame frame = new JFrame();
        frame.setVisible(false);
        frame.pack();
        frame.dispose();

        WindowContext windowContext = new DCWindowContext(null,null, null); 
     
        final HadoopConfigurationsDialog hadoopConfigurationDialog = new HadoopConfigurationsDialog(windowContext, null, null, serverInformationCatalog);
        hadoopConfigurationDialog.setVisible(true);
        hadoopConfigurationDialog.show();
        hadoopConfigurationDialog.pack();
    }
    
    }

