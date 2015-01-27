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

import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;

import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.util.SimpleTableDef;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.connection.HBaseDatastore;
import org.datacleaner.guice.Nullable;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImmutableEntry;
import org.datacleaner.util.NumberDocument;
import org.datacleaner.util.SchemaFactory;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.widgets.TableDefinitionOptionSelectionPanel;
import org.jdesktop.swingx.JXTextField;

public class HBaseDatastoreDialog extends AbstractDatastoreDialog<HBaseDatastore> implements SchemaFactory {

    private static final long serialVersionUID = 1L;

    private final JXTextField _hostnameTextField;
    private final JXTextField _portTextField;
    private final TableDefinitionOptionSelectionPanel _tableDefinitionWidget;

    @Inject
    public HBaseDatastoreDialog(WindowContext windowContext, MutableDatastoreCatalog catalog,
            @Nullable HBaseDatastore originalDatastore, UserPreferences userPreferences) {
        super(originalDatastore, catalog, windowContext, userPreferences);

        _hostnameTextField = WidgetFactory.createTextField();
        _portTextField = WidgetFactory.createTextField();
        _portTextField.setDocument(new NumberDocument(false));
        
        _hostnameTextField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                validateAndUpdate();
            }
        });
        _portTextField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                validateAndUpdate();
            }
        });

        if (originalDatastore == null) {
            _hostnameTextField.setText("localhost");
            _portTextField.setText("2181");
            _tableDefinitionWidget = new TableDefinitionOptionSelectionPanel(windowContext, this, null);
        } else {
            _datastoreNameTextField.setText(originalDatastore.getName());
            _datastoreNameTextField.setEnabled(false);
            _hostnameTextField.setText(originalDatastore.getZookeeperHostname());
            _portTextField.setText(originalDatastore.getZookeeperPort() + "");
            final SimpleTableDef[] tableDefs = originalDatastore.getTableDefs();
            _tableDefinitionWidget = new TableDefinitionOptionSelectionPanel(windowContext, this, tableDefs);
        }
    }
    
    @Override
    protected boolean validateForm() {
        final String datastoreName = _datastoreNameTextField.getText();
        if (StringUtils.isNullOrEmpty(datastoreName)) {
            setStatusError("Please enter a datastore name");
            return false;
        }
        
        final String hostname = _hostnameTextField.getText();
        if (StringUtils.isNullOrEmpty(hostname)) {
            setStatusError("Please enter hostname");
            return false;
        }
        
        final String port = _portTextField.getText();
        if (StringUtils.isNullOrEmpty(port)) {
            setStatusError("Please enter port number");
            return false;
        } else {
            try {
                int portInt = Integer.parseInt(port);
                if (portInt <= 0) {
                    setStatusError("Please enter a valid (positive port number)");
                    return false;
                }
            } catch (NumberFormatException e) {
                setStatusError("Please enter a valid port number");
                return false;
            }
        }
        
        setStatusValid();
        return true;
    }

    @Override
    public String getWindowTitle() {
        return "HBase database";
    }

    @Override
    protected String getBannerTitle() {
        return "HBase database";
    }

    @Override
    protected HBaseDatastore createDatastore() {
        final String name = _datastoreNameTextField.getText();
        final String hostname = _hostnameTextField.getText();
        final Integer port = Integer.parseInt(_portTextField.getText());
        final SimpleTableDef[] tableDefinitions = _tableDefinitionWidget.getTableDefs();
        return new HBaseDatastore(name, hostname, port, tableDefinitions);
    }

    @Override
    public Schema createSchema() {
        final HBaseDatastore datastore = createDatastore();
        try (final DatastoreConnection con = datastore.openConnection()) {
            final Schema schema = con.getDataContext().getDefaultSchema();
            return schema;
        }
    }

    @Override
    protected String getDatastoreIconPath() {
        return IconUtils.HBASE_IMAGEPATH;
    }
    
    @Override
    protected List<Entry<String, JComponent>> getFormElements() {
        List<Entry<String, JComponent>> result = super.getFormElements();
        result.add(new ImmutableEntry<String, JComponent>("Hostname", _hostnameTextField));
        result.add(new ImmutableEntry<String, JComponent>("Port", _portTextField));
        result.add(new ImmutableEntry<String, JComponent>("Schema model", _tableDefinitionWidget));
        return result;
    }
}
