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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Provider;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.ServerInformationCatalog;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.HadoopResource;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.jfree.ui.tabbedui.VerticalLayout;

public class SelectHadoopClusterDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    private final JList<String> _serverList;
    private final JButton _okButton;
    private final JButton _optionsButton;
    private String _selectedConfiguration;
    private final Map<String, String> _mappedServers;
    private final Provider<OptionsDialog> _optionsDialogProvider;

    
    public SelectHadoopClusterDialog(WindowContext windowContext, ServerInformationCatalog serverInformationCatalog, Provider<OptionsDialog> optionsDialogProvider) {
         super(windowContext, ImageManager.get().getImage(IconUtils.FILE_HDFS)); 
         
         _optionsDialogProvider = optionsDialogProvider; 
         //It needs to be modal. Otherwise there will be null for selected Configuration
         setModal(true);
        // It's important to keep the order of the elements.
        _mappedServers = new LinkedHashMap<String, String>();
        
        final String[] serverNames = getMappedServers(serverInformationCatalog, _mappedServers);
        _selectedConfiguration = null; 
        _serverList = new JList<String>(serverNames);
        _serverList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        _serverList.setLayoutOrientation(JList.VERTICAL);
        _serverList.setSelectedIndex(serverNames.length-1);
        _serverList.setBorder(WidgetUtils.BORDER_WIDE_WELL);

          
        _okButton = WidgetFactory.createPrimaryButton("OK",  IconUtils.ACTION_FORWARD);
        _okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final String selectedValue = _serverList.getSelectedValue();
                _selectedConfiguration = _mappedServers.get(selectedValue);
              dispose();
            }
        });
        _optionsButton = WidgetFactory.createDefaultButton("Options", IconUtils.MENU_OPTIONS);
        _optionsButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                SelectHadoopClusterDialog.this.close();
                final OptionsDialog optionsDialog = _optionsDialogProvider.get();
                optionsDialog.selectHadoopClustersTab();
                optionsDialog.open();
            }
        });
    }

    /**
     * We avoid having HadoopResource.DEFAULT_CLUSTERREFERENCE(
     * "org.datacleaner.hadoop.environment") as a server name. We write
     * "default" instead. 
     */
    private String[] getMappedServers(ServerInformationCatalog serverInformationCatalog, Map<String, String> mappedServers) {

        if (serverInformationCatalog.containsServer(HadoopResource.DEFAULT_CLUSTERREFERENCE)) {
            mappedServers.put("(default)", HadoopResource.DEFAULT_CLUSTERREFERENCE);
        }

        final String[] serverNames = serverInformationCatalog.getServerNames();
        for (int i = 0; i < serverNames.length; i++) {
            final String serverName = serverNames[i];
            if (!serverName.equals(HadoopResource.DEFAULT_CLUSTERREFERENCE)) {
                mappedServers.put(serverName, serverName);
            }
        }
        return mappedServers.keySet().toArray(new String[serverNames.length]);
    }

    public String getSelectedConfiguration() {
        return _selectedConfiguration;
    }

    @Override
    public String getWindowTitle() {
        return "Select Hadoop cluster";
    }

    @Override
    protected String getBannerTitle() {
        return getWindowTitle();
    }

    @Override
    protected int getDialogWidth() {
        return 500;
    }
    
    @Override
    protected JComponent getDialogContent() {
       
        final DCPanel contentPanel = new DCPanel();
        contentPanel.setLayout(new GridBagLayout());
        
        final DCPanel listPanel = new DCPanel(); 
        listPanel.setBackground(WidgetUtils.COLOR_WELL_BACKGROUND);
        listPanel.setLayout(new VerticalLayout());
        listPanel.add(_serverList);
        

        final DCLabel label = DCLabel.dark("Please select the Hadoop cluster to connect to:");
        label.setFont(WidgetUtils.FONT_HEADER2);
        WidgetUtils.addToGridBag(label, contentPanel, 0, 0, 1.0, 0.0);
        WidgetUtils.addToGridBag(listPanel, contentPanel, 0, 1, 1, 2, GridBagConstraints.NORTH, 10, 1.0, 1.0);
        WidgetUtils.addToGridBag(_optionsButton, contentPanel, 1, 1, GridBagConstraints.SOUTH);
        WidgetUtils.addToGridBag(_okButton, contentPanel, 1, 2, GridBagConstraints.NORTH);
        
        final JScrollPane scrolleable = WidgetUtils.scrolleable(contentPanel);
        scrolleable.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        final DCPanel outerPanel = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND); 
        outerPanel.setLayout(new BorderLayout());
        outerPanel.add(scrolleable, BorderLayout.CENTER); 
        outerPanel.setPreferredSize(getDialogWidth(), 300); 
        return outerPanel; 
    }
    
    @Override
    protected boolean isWindowResizable() {
        return true;
    }
}
