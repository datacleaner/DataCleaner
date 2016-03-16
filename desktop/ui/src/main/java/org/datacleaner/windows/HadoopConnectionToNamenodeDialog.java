package org.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.ServerInformation;
import org.datacleaner.panels.DCBannerPanel;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.server.DirectConnectionHadoopClusterInformation;
import org.datacleaner.user.MutableServerInformationCatalog;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.jdesktop.swingx.JXTextField;

public class HadoopConnectionToNamenodeDialog extends AbstractDialog {
    
    private final DirectConnectionHadoopClusterInformation _directConnection;
    protected final JLabel _statusLabel;
    private final JXTextField _nameTextField;
    private final JXTextField _hostTextField; 
    private final JXTextField _portTextField; 
    private final JXTextField _descriptionTextField;
    private final JButton _saveButton;
    private final JButton _cancelButton;
    private final MutableServerInformationCatalog _mutableServerInformationCatalog; 
    private ServerInformation _savedServer = null; 
    
  
    public HadoopConnectionToNamenodeDialog(WindowContext windowContext, DirectConnectionHadoopClusterInformation directConnection, MutableServerInformationCatalog serverinformationCatalog ) {
        super(windowContext);
        
        _statusLabel = DCLabel.bright("Please specify connection name");
        _directConnection = directConnection; 
        _nameTextField = WidgetFactory.createTextField("Connection name"); 
        _hostTextField = WidgetFactory.createTextField("host"); 
        _portTextField = WidgetFactory.createTextField("port");
        _descriptionTextField = WidgetFactory.createTextField("description");
        _mutableServerInformationCatalog = serverinformationCatalog; 
        
        
        final String saveButtonText = directConnection == null ? "Register connection" : "Save connection";
        _saveButton = WidgetFactory.createPrimaryButton(saveButtonText, IconUtils.ACTION_SAVE_BRIGHT);
        _saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
               URI nameNodeUri;
                try {
                    nameNodeUri = new URI("hdfs", "//", _hostTextField.getText(), Integer.parseInt(_portTextField.getText()),"/", null, null);
                    DirectConnectionHadoopClusterInformation newServer = new DirectConnectionHadoopClusterInformation(_nameTextField.getText(), _descriptionTextField.getText(), nameNodeUri); 
                    if (_directConnection != null){
                        _mutableServerInformationCatalog.removeServer(_directConnection);
                    }
                    _savedServer = newServer; 
                    _mutableServerInformationCatalog.addServerInformation(newServer);
                    
                } catch (URISyntaxException e1) {
                    //TODO : log exception
                    e1.printStackTrace();
                }
            }
        });

        _cancelButton = WidgetFactory.createDefaultButton("Cancel", IconUtils.ACTION_CANCEL);
        _cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HadoopConnectionToNamenodeDialog.this.close();
            }
        });
        
        if (directConnection != null){
          _nameTextField.setText(directConnection.getName());
          _hostTextField.setText(directConnection.getNameNodeUri().getHost()); 
          _portTextField.setText("" + directConnection.getNameNodeUri().getPort());
          _descriptionTextField.setText(directConnection.getDescription());
        }
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

       formPanel.setLayout(new GridBagLayout());
       //TODO:add description  label to dialog
       /*final JLabel descriptionLabel = DCLabel.bright("Create direct connection to Hadoop Namenode:");
       descriptionLabel.setFont(WidgetUtils.FONT_HEADER2);
       int row = 0;
       WidgetUtils.addToGridBag(descriptionLabel, formPanel, 0, row, 2.0, 0.0); 
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
    
    public ServerInformation getSavedServer() {
        return _savedServer;
    }
   
}
