package org.datacleaner.windows;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import org.datacleaner.configuration.ServerInformation;
import org.datacleaner.configuration.ServerInformationCatalog;
import org.datacleaner.configuration.ServerInformationCatalogImpl;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.server.DirectConnectionHadoopClusterInformation;
import org.datacleaner.server.DirectoryBasedHadoopClusterInformation;
import org.datacleaner.server.EnvironmentBasedHadoopClusterInformation;
import org.datacleaner.util.HadoopResource;
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
    private final JButton _cancelButton;
    private final JButton _optionsButton;
    private String _hadoopConfiguration;
    private final LinkedList<String> _mappedServers; 

    public SelectHadoopConfigurationDialog(ServerInformationCatalog serverInformationCatalog) {
        _serverInformationCatalog = serverInformationCatalog;
        setLayout(new VerticalLayout());
        final DCPanel panel = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        panel.setLayout(new GridLayout());

        final DCLabel label = DCLabel.dark("Select Hadoop configuration: ");

        // It's important to keep the order of the elements. 
        _mappedServers = new LinkedList<String>();  
        final String[] serverNames = mapServers(serverInformationCatalog.getServerNames(), _mappedServers);
        _serverList = new JList<String>(serverNames);
        _serverList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        _serverList.setLayoutOrientation(JList.VERTICAL);
        _serverList.setVisibleRowCount(-1);
        _serverList.setSelectedIndex(0);
        final JScrollPane listScroller = new JScrollPane(_serverList);
        _serverList.setBorder(new EmptyBorder(WidgetUtils.DEFAULT_PADDING, WidgetUtils.DEFAULT_PADDING,
                WidgetUtils.DEFAULT_PADDING, WidgetUtils.DEFAULT_PADDING));

        listScroller.setEnabled(true);
        
        _okButton = new JButton("OK");
        _optionsButton = new JButton("Options");
        _cancelButton = new JButton("Cancel"); 

        WidgetUtils.addToGridBag(label, panel, 0, 0); // , 0,1);
        WidgetUtils.addToGridBag(_serverList, panel, 0, 1);
        WidgetUtils.addToGridBag(_optionsButton, panel, 1, 1, GridBagConstraints.PAGE_START);
        WidgetUtils.addToGridBag(_okButton, panel, 1, 2, GridBagConstraints.PAGE_END);

        setBorder(new EmptyBorder(WidgetUtils.DEFAULT_PADDING, WidgetUtils.DEFAULT_PADDING, WidgetUtils.DEFAULT_PADDING,
                WidgetUtils.DEFAULT_PADDING));
        add(panel);
    }

    /**
     *  We avoid having HadoopResource.DEFAULT_CLUSTERREFERENCE("org.datacleaner.hadoop.environment") as a server name. We write "default" instead
     */
    private String[] mapServers(String[] serverNames, LinkedList<String> mappedServers) {

        for (int i = 0; i < serverNames.length; i++) {
            final String serverName = serverNames[i];
            if (serverName.equals(HadoopResource.DEFAULT_CLUSTERREFERENCE)) {
                mappedServers.add("default");
            } else {
                mappedServers.add(serverName);
            }

        }
        return mappedServers.toArray(new String[serverNames.length]);
    }

    public String selectServer(Component parent, ServerInformationCatalog serverInformationCatalog) {

        final SelectHadoopConfigurationDialog selectHadoopConfigurationDialog = new SelectHadoopConfigurationDialog(
                serverInformationCatalog);
        final JDialog dialog = WidgetFactory.createModalDialog(selectHadoopConfigurationDialog, parent,
                "Select Hadoop Configuration", false);

        selectHadoopConfigurationDialog._okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final int selectedIndex = selectHadoopConfigurationDialog._serverList.getSelectedIndex();
                selectHadoopConfigurationDialog._hadoopConfiguration = serverInformationCatalog.getServerNames()[selectedIndex];
                dialog.dispose();
            }
        });
        
        dialog.setVisible(true);
        dialog.dispose();
        return selectHadoopConfigurationDialog._hadoopConfiguration;
    }

    public ServerInformationCatalog getServerInformationCatalog() {
        return _serverInformationCatalog;
    }
    
    public static void main(String[] args) throws Exception {
        LookAndFeelManager.get().init();
        final List<ServerInformation> servers = new ArrayList<>();
        servers.add(new EnvironmentBasedHadoopClusterInformation(HadoopResource.DEFAULT_CLUSTERREFERENCE, "hadoop conf dir"));
        servers.add(new DirectoryBasedHadoopClusterInformation("directory", "directopry set up",
                "C:\\Users\\claudiap\\git\\vagrant-vms\\bigdatavm\\yarn_conf_client"));
        servers.add(new DirectConnectionHadoopClusterInformation("namenode", "directconnection", new URI(
                "hdfs://192.168.0.200:9000/")));
        final ServerInformationCatalog serverInformationCatalog = new ServerInformationCatalogImpl(servers);
        JFrame frame = new JFrame();
        frame.setVisible(false);
        frame.pack();
        frame.dispose();

        final SelectHadoopConfigurationDialog selectHadoopConfigurationDialog = new SelectHadoopConfigurationDialog(serverInformationCatalog); 
        
        System.out.println(selectHadoopConfigurationDialog.selectServer(frame, serverInformationCatalog));

    }
}
