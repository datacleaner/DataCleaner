package org.datacleaner.windows;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

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
    private final JButton _optionsButton;
    private String _hadoopConfiguration;

    public SelectHadoopConfigurationDialog(ServerInformationCatalog serverInformationCatalog) {
        _serverInformationCatalog = serverInformationCatalog;
        setLayout(new VerticalLayout());
        final DCPanel panel = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        panel.setLayout(new GridLayout());

        final DCLabel label = DCLabel.dark("Select Hadoop configuration: ");

        final String[] serverNames = serverInformationCatalog.getServerNames();
        _serverList = new JList<String>(serverNames);
        _serverList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        _serverList.setLayoutOrientation(JList.VERTICAL);
        _serverList.setVisibleRowCount(-1);
        _serverList.setSelectedIndex(0);
        final JScrollPane listScroller = new JScrollPane(_serverList);

        _optionsButton = new JButton("Options");

        _serverList.setBorder(new EmptyBorder(WidgetUtils.DEFAULT_PADDING, WidgetUtils.DEFAULT_PADDING,
                WidgetUtils.DEFAULT_PADDING, WidgetUtils.DEFAULT_PADDING));

        listScroller.setEnabled(true);
        _okButton = new JButton("OK");

        WidgetUtils.addToGridBag(label, panel, 0, 0); // , 0,1);
        WidgetUtils.addToGridBag(_serverList, panel, 0, 1);
        WidgetUtils.addToGridBag(_optionsButton, panel, 1, 1, GridBagConstraints.PAGE_START);
        WidgetUtils.addToGridBag(_okButton, panel, 1, 1, GridBagConstraints.PAGE_END);

        setBorder(new EmptyBorder(WidgetUtils.DEFAULT_PADDING, WidgetUtils.DEFAULT_PADDING, WidgetUtils.DEFAULT_PADDING,
                WidgetUtils.DEFAULT_PADDING));
        add(panel);
    }

    public static String selectServer(Component parent, ServerInformationCatalog serverInformationCatalog) {

        final SelectHadoopConfigurationDialog selectHadoopConfigurationDialog = new SelectHadoopConfigurationDialog(
                serverInformationCatalog);

        final JDialog dialog = WidgetFactory.createModalDialog(selectHadoopConfigurationDialog, parent,
                "Select Hadoop Configuration", false);

        selectHadoopConfigurationDialog._okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                selectHadoopConfigurationDialog._hadoopConfiguration = selectHadoopConfigurationDialog._serverList
                        .getSelectedValue();
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
        servers.add(new EnvironmentBasedHadoopClusterInformation("default", "hadoop conf dir"));
        servers.add(new DirectoryBasedHadoopClusterInformation("directory", "directopry set up",
                "C:\\Users\\claudiap\\git\\vagrant-vms\\bigdatavm\\yarn_conf_client"));
        servers.add(new DirectConnectionHadoopClusterInformation("namenode", "directconnection", new URI(
                "hdfs://192.168.0.200:9000/")));
        final ServerInformationCatalog serverInformationCatalog = new ServerInformationCatalogImpl(servers);
        JFrame frame = new JFrame();
        frame.setVisible(false);
        frame.pack();
        frame.dispose();

        System.out.println(SelectHadoopConfigurationDialog.selectServer(frame, serverInformationCatalog));

    }
}
