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
package org.datacleaner.panels;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.server.DirectConnectionHadoopClusterInformation;
import org.datacleaner.user.MutableServerInformationCatalog;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.windows.HadoopConnectionToNamenodeDialog;

public class HadoopDirectConnectionPanel extends DCPanel {

    private static final long serialVersionUID = 1L;
    private final MutableServerInformationCatalog _serverInformationCatalog; 
    private final DirectConnectionHadoopClusterInformation _serverInformation; 
    private final WindowContext _windowContext; 
    
    public HadoopDirectConnectionPanel(WindowContext windowContext, String name, URI namenodeUri, String description,  DirectConnectionHadoopClusterInformation serverInformation, MutableServerInformationCatalog serverInformationCatalog){
        _serverInformationCatalog = serverInformationCatalog;
        _serverInformation = serverInformation;
        _windowContext = windowContext; 
        
        
        final DCLabel label = DCLabel.dark("<html><b>" + name + "</b> - " + namenodeUri + "<br/>" + description + "</html>"); 
        
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
        editButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
               final HadoopConnectionToNamenodeDialog hadoopConnectionToNamenodeDialog = new HadoopConnectionToNamenodeDialog(_windowContext,_serverInformation, _serverInformationCatalog);
               hadoopConnectionToNamenodeDialog.setVisible(true);
            }
        });
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
