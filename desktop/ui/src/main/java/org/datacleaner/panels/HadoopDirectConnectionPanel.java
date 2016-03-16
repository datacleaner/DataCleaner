package org.datacleaner.panels;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.datacleaner.server.DirectConnectionHadoopClusterInformation;
import org.datacleaner.user.MutableServerInformationCatalog;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;

public class HadoopDirectConnectionPanel extends DCPanel {

    private static final long serialVersionUID = 1L;
    private final MutableServerInformationCatalog _serverInformationCatalog; 
    private final DirectConnectionHadoopClusterInformation _serverInformation; 
    
    public HadoopDirectConnectionPanel(String name, URI namenodeUri, String description,  DirectConnectionHadoopClusterInformation serverInformation, MutableServerInformationCatalog serverInformationCatalog){
        _serverInformationCatalog = serverInformationCatalog;
        _serverInformation = serverInformation; 
        
        
        final DCLabel label = DCLabel.dark("<html><b>" + name + "</b> - " + namenodeUri + "</br>" + description + "</html>"); 
        
        final JButton editButton = createEditButton();
        final JButton removeButton = createRemoveButton(serverInformation);
        
        setBorder(WidgetUtils.BORDER_LIST_ITEM_SUBTLE);
        
        WidgetUtils.addToGridBag(DCPanel.flow(label), this, 1, 0, GridBagConstraints.WEST, 1.0, 1.0);
        WidgetUtils.addToGridBag(editButton, this, 2, 0, GridBagConstraints.EAST);
        WidgetUtils.addToGridBag(removeButton, this, 3, 0, GridBagConstraints.EAST);
    }

    private JButton createEditButton() {
        final JButton editButton = WidgetFactory.createDefaultButton("Edit", IconUtils.ACTION_EDIT);
        editButton.setToolTipText("Edit connection");
        return editButton;
    }

    private JButton createRemoveButton(DirectConnectionHadoopClusterInformation serverInformation) {
        final JButton removeButton = WidgetFactory.createDefaultButton("Remove", IconUtils.ACTION_REMOVE);
        removeButton.setToolTipText("Remove connection");
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = JOptionPane.showConfirmDialog(HadoopDirectConnectionPanel.this,
                        "Are you sure you wish to remove the connection '" + serverInformation.getName() + "'?", "Confirm remove",
                        JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    _serverInformationCatalog.removeServer(_serverInformation);
                }
            }
        });
        return removeButton;
    }
}
