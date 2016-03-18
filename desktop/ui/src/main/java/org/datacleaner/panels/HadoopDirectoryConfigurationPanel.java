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

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import org.datacleaner.server.DirectoryBasedHadoopClusterInformation;
import org.datacleaner.user.MutableServerInformationCatalog;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.FileSelectionListener;
import org.datacleaner.widgets.FilenameTextField;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXTextField;
import org.jfree.ui.tabbedui.VerticalLayout;

public class HadoopDirectoryConfigurationPanel extends DCPanel {

    public class DirectoryPathPanel extends DCPanel {

        private static final long serialVersionUID = 1L;

        private final JLabel _label;
        private final FilenameTextField _directoryTextField;
        private File _directory;
        private JButton _removeButton;
        private JPanel _parent;
        private final int _pathNumber;

        public DirectoryPathPanel(int pathNumber, File directory, JPanel parent) {
            _pathNumber = pathNumber;
            _parent = parent;
            _directory = directory;
            _label = DCLabel.dark("Path " + pathNumber + ":");
            if (directory == null) {
                _directoryTextField = new FilenameTextField(null, true);
            } else {
                _directoryTextField = new FilenameTextField(directory, true);
            }
            _directoryTextField.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            _directoryTextField.addFileSelectionListener(new FileSelectionListener() {

                @Override
                public void onSelected(FilenameTextField filenameTextField, File file) {
                    _directory = file;
                }
            });

            if (_directory != null) {
                _directoryTextField.setFile(_directory);
            }
            _removeButton = WidgetFactory.createDefaultButton("", IconUtils.ACTION_DELETE);

            this.setLayout(new HorizontalLayout(10));
            add(_label);
            add(_directoryTextField);
            add(_removeButton);
            setBorder(WidgetUtils.BORDER_TOP_PADDING);

            _removeButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    _pathPanels.remove(_pathNumber);
                    _parent.remove(DirectoryPathPanel.this);
                }
            });
        }

        public File getDirectory() {
            return _directory;
        }
    }

    
    private static final long serialVersionUID = 1L;
    private final JXTextField _nameTextField;
    private final JXTextField _descriptionTextField;
    private List<DirectoryPathPanel> _pathPanels;
    private DirectoryBasedHadoopClusterInformation _server;
    private final MutableServerInformationCatalog _serverInformationCatalog; 
    private final JPanel _parent;
    private final JButton _saveButton; 
    private final JButton _removeButton; 
   // private final DCPanel _outerPanel; 

    public HadoopDirectoryConfigurationPanel(final DirectoryBasedHadoopClusterInformation server, MutableServerInformationCatalog serverInformationCatalog,  JPanel parent) {
        _server = server;
        _serverInformationCatalog = serverInformationCatalog; 
        _nameTextField = WidgetFactory.createTextField("MyConnection");
        _descriptionTextField = WidgetFactory.createTextField();
        _parent = parent;

        if (_server != null) {
            final String serverName = _server.getName();
            if (serverName != null) {
                _nameTextField.setText(serverName);
                _nameTextField.setEnabled(false);
            }
            final String description = _server.getDescription();
            if (description != null) {
                _descriptionTextField.setText(description);
            }
        }

         
        DCPanel contentPanel = new DCPanel(); 
        contentPanel.setBorder(WidgetUtils.BORDER_LIST_ITEM);
        contentPanel.setBackground(WidgetUtils.ADDITIONAL_COLOR_RED_BRIGHT);
        contentPanel.setLayout(new BorderLayout());
        
        final DCPanel centerPanel = new DCPanel();
        centerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        centerPanel.setLayout(new GridBagLayout());
        final JPanel listPanel = new DCPanel();
        listPanel.setLayout(new VerticalLayout());
        _pathPanels = getDirectoriesListPanel(listPanel);
        for (int i = 0; i < _pathPanels.size(); i++) {
            listPanel.add(_pathPanels.get(i));
        }
        listPanel.addContainerListener(new ContainerListener() {

            @Override
            public void componentRemoved(ContainerEvent e) {
                centerPanel.revalidate();
                centerPanel.repaint();
            }

            @Override
            public void componentAdded(ContainerEvent e) {
                centerPanel.revalidate();
                centerPanel.repaint();
            }
        });
        
        final JButton addPath = WidgetFactory.createDefaultButton("Add path");
        addPath.setToolTipText("Add path to configuration");
        addPath.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final DirectoryPathPanel newPathPanel = new DirectoryPathPanel(_pathPanels.size(), null, listPanel);
                _pathPanels.add(newPathPanel);
                listPanel.add(newPathPanel);

            }
        });

     
        _saveButton = WidgetFactory.createPrimaryButton("Save", IconUtils.ACTION_SAVE_BRIGHT);
        _removeButton = WidgetFactory.createDefaultButton("Remove", IconUtils.ACTION_DELETE);
        _removeButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (_server != null) {
                    int result = JOptionPane.showConfirmDialog(HadoopDirectoryConfigurationPanel.this,
                            "Are you sure you wish to remove the connection '" + _server.getName() + "'?",
                            "Confirm remove", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        _serverInformationCatalog.removeServer(_server);
                    }
                }else{
                    _parent.remove(HadoopDirectoryConfigurationPanel.this);
                }
            }
        });
        
        
        _saveButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                final List<String> paths = new ArrayList<>(); 
                for (int i=0; i<_pathPanels.size(); i++){
                    final DirectoryPathPanel directoryPathPanel = _pathPanels.get(i); 
                    final File directory = directoryPathPanel.getDirectory(); 
                    if (directory != null && directory.isDirectory()) {
                        paths.add(directory.getPath());
                    }
                } 
                 //We do not save a connection if there are no paths 
                 if (paths.size() ==0 ){
                     WidgetUtils.showErrorMessage("The connection can not be created","There are no paths defined"); 
                     return; 
                 }
                 if (_server != null){
                     _serverInformationCatalog.removeServer(_server);
                 }

                 if (_server == null){
                     _server = new DirectoryBasedHadoopClusterInformation(_nameTextField.getText(), _descriptionTextField.getText(), paths.toArray(new String[paths.size()])); 
                 }
                 try{
                 _serverInformationCatalog.addServerInformation(_server);
                 }
                 catch(Exception exception){
                     WidgetUtils.showErrorMessage("Error while adding connection", exception);
                 }
            }
        }); 

        WidgetUtils.addToGridBag(DCLabel.dark("Name:"), centerPanel, 0, 0, 1, 1, GridBagConstraints.EAST, 4, 0.0, 0.0);
        WidgetUtils.addToGridBag(_nameTextField, centerPanel, 1, 0, 1, 1, GridBagConstraints.WEST, 4, 0.0, 0.0);
        WidgetUtils.addToGridBag(DCLabel.dark("Description:"), centerPanel, 0, 1, 1, 1, GridBagConstraints.EAST, 4, 0.0, 0.0);
        WidgetUtils.addToGridBag(_descriptionTextField, centerPanel, 1, 1, 1, 1, GridBagConstraints.WEST,  4, 1.0, 0.0);
        WidgetUtils.addToGridBag(DCLabel.dark("Paths:"), centerPanel, 0, 2, 1, 1, GridBagConstraints.EAST, 4, 1.0, 0.0);
        WidgetUtils.addToGridBag(listPanel, centerPanel, 1, 2, GridBagConstraints.CENTER);
        WidgetUtils.addToGridBag(addPath, centerPanel, 1, 3, GridBagConstraints.EAST);

  
       
        
        final DCPanel buttonsPanel = DCPanel.flow(Alignment.CENTER,_saveButton, _removeButton); 
        contentPanel.add(centerPanel, BorderLayout.CENTER); 
        contentPanel.add(buttonsPanel, BorderLayout.SOUTH); 
        
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10,0,0,0));
        add(contentPanel); 
    }
    private List<DirectoryPathPanel> getDirectoriesListPanel(JPanel parent) {
        _pathPanels = new ArrayList<>();
        if (_server != null) {
            final String[] directories = _server.getDirectories();
            if (directories != null) {
                for (int j = 0; j < directories.length; j++) {
                    final DirectoryPathPanel directoryPanel = new DirectoryPathPanel(j, new File(directories[j]),
                            parent);
                    _pathPanels.add(directoryPanel);
                }
            } else {
                _pathPanels.add(new DirectoryPathPanel(0, null, parent));
            }
        } else {
            _pathPanels.add(new DirectoryPathPanel(0, null, parent));
        }
      
        return _pathPanels;
    }
}
