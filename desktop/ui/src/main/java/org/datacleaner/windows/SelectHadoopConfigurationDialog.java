package org.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.ServerInformation;
import org.datacleaner.configuration.ServerInformationCatalog;
import org.datacleaner.configuration.ServerInformationCatalogImpl;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.server.DirectConnectionHadoopClusterInformation;
import org.datacleaner.server.DirectoryBasedHadoopClusterInformation;
import org.datacleaner.server.EnvironmentBasedHadoopClusterInformation;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.LookAndFeelManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.jfree.ui.tabbedui.VerticalLayout;

public class SelectHadoopConfigurationDialog extends JComponent {

    private static final long serialVersionUID = 1L;


    private final ServerInformationCatalog _serverInformationCatalog;
    private final JList<String> _serverList; 
    private final JButton _okButton; 

    public SelectHadoopConfigurationDialog(ServerInformationCatalog serverInformationCatalog) {
        _serverInformationCatalog = serverInformationCatalog;

        setLayout(new VerticalLayout());
        final DCPanel panel = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        panel.setLayout(new GridLayout());
        
        DCLabel label = DCLabel.dark("Select Hadoop configuration:"); 
      
        final String[] serverNames = serverInformationCatalog.getServerNames();
        _serverList = new JList<String>(serverNames);
        _serverList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        _serverList.setLayoutOrientation(JList.VERTICAL);
        _serverList.setVisibleRowCount(-1);
        _serverList.setSelectedIndex(0);
        JScrollPane listScroller = new JScrollPane(_serverList);

        final DCPanel serverListPanel = new DCPanel(WidgetUtils.COLOR_WELL_BACKGROUND);
        serverListPanel.add(_serverList); 
        serverListPanel.add(listScroller); 
        serverListPanel.setBorder(new EmptyBorder(WidgetUtils.DEFAULT_PADDING, WidgetUtils.DEFAULT_PADDING, WidgetUtils.DEFAULT_PADDING,
                WidgetUtils.DEFAULT_PADDING));
        
        listScroller.setEnabled(true);
        _okButton = new JButton("OK"); 
        
        WidgetUtils.addToGridBag(label, panel, 0,0); //, 0,1); 
        WidgetUtils.addToGridBag(serverListPanel, panel, 0, 1);
        WidgetUtils.addToGridBag(_okButton, panel, 1, 1, GridBagConstraints.PAGE_END);
        
        setBorder(new EmptyBorder(WidgetUtils.DEFAULT_PADDING, WidgetUtils.DEFAULT_PADDING, WidgetUtils.DEFAULT_PADDING,       WidgetUtils.DEFAULT_PADDING));
        add(panel);
    }

    public static String selectServer(Component parent, ServerInformationCatalog serverInformationCatalog) {

        final SelectHadoopConfigurationDialog selectHadoopConfigurationDialog = new SelectHadoopConfigurationDialog(
                serverInformationCatalog);

        final JDialog dialog = WidgetFactory.createModalDialog(selectHadoopConfigurationDialog, parent,
                "Select Hadoop Configuration", false);
        dialog.setVisible(true);
        dialog.dispose();

        return null;
    }

    public static void main(String[] args) throws Exception {
        LookAndFeelManager.get().init();
        final List<ServerInformation> servers = new ArrayList<>(); 
        servers.add(new EnvironmentBasedHadoopClusterInformation("default", "hadoop conf dir"));
        servers.add(new DirectoryBasedHadoopClusterInformation("directory", "directopry set up", "C:\\Users\\claudiap\\git\\vagrant-vms\\bigdatavm\\yarn_conf_client")); 
        servers.add(new DirectConnectionHadoopClusterInformation("namenode", "directconnection", new URI("hdfs://192.168.0.200:9000/"))); 
        final ServerInformationCatalog serverInformationCatalog = new ServerInformationCatalogImpl(servers );
        JFrame frame = new JFrame();
        frame.setVisible(false);
        frame.pack();
        frame.dispose();
        
        SelectHadoopConfigurationDialog.selectServer(frame, serverInformationCatalog); 
        
    }
}
