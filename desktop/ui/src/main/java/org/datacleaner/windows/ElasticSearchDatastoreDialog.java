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

import javax.inject.Inject;
import javax.swing.JComponent;

import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.util.SimpleTableDef;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.connection.ElasticSearchDatastore;
import org.datacleaner.guice.Nullable;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.NumberDocument;
import org.datacleaner.util.SchemaFactory;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.TableDefinitionOptionSelectionPanel;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.VerticalLayout;

public class ElasticSearchDatastoreDialog extends AbstractDatastoreDialog<ElasticSearchDatastore> implements SchemaFactory {

    private static final long serialVersionUID = 1L;

    private final JXTextField _hostnameTextField;
    private final JXTextField _portTextField;
    private final JXTextField _clusterNameTextField;
    private final JXTextField _indexNameTextField;
    private final JXTextField _datastoreNameTextField;
    private final TableDefinitionOptionSelectionPanel _tableDefinitionWidget;

    @Inject
    public ElasticSearchDatastoreDialog(WindowContext windowContext, MutableDatastoreCatalog catalog,
            @Nullable ElasticSearchDatastore originalDatastore, UserPreferences userPreferences) {
        super(originalDatastore, catalog, windowContext, userPreferences);

        _datastoreNameTextField = WidgetFactory.createTextField();
        _hostnameTextField = WidgetFactory.createTextField();
        _portTextField = WidgetFactory.createTextField();
        _portTextField.setDocument(new NumberDocument(false));
        _clusterNameTextField = WidgetFactory.createTextField();
        _indexNameTextField = WidgetFactory.createTextField();

        if (originalDatastore == null) {
            _hostnameTextField.setText("localhost");
            _portTextField.setText("9300");
            _tableDefinitionWidget = new TableDefinitionOptionSelectionPanel(windowContext, this, null);
        } else {
            _datastoreNameTextField.setText(originalDatastore.getName());
            _datastoreNameTextField.setEnabled(false);
            _hostnameTextField.setText(originalDatastore.getHostname());
            _portTextField.setText(originalDatastore.getPort() + "");
            _clusterNameTextField.setText(originalDatastore.getClusterName());
            _indexNameTextField.setText(originalDatastore.getIndexName());
            final SimpleTableDef[] tableDefs = originalDatastore.getTableDefs();
            _tableDefinitionWidget = new TableDefinitionOptionSelectionPanel(windowContext, this, tableDefs);
        }
    }

    @Override
    public String getWindowTitle() {
        return "ElasticSearch database";
    }

    @Override
    protected String getBannerTitle() {
        return "ElasticSearch database";
    }

    @Override
    protected boolean isWindowResizable() {
        return true;
    }

    @Override
    protected int getDialogWidth() {
        return 400;
    }

    @Override
    protected JComponent getDialogContent() {
        final DCPanel formPanel = new DCPanel();
        formPanel.setBorder(WidgetUtils.BORDER_EMPTY);

        int row = 0;
        WidgetUtils.addToGridBag(DCLabel.bright("Datastore name:"), formPanel, 0, row);
        WidgetUtils.addToGridBag(_datastoreNameTextField, formPanel, 1, row);
        row++;

        WidgetUtils.addToGridBag(DCLabel.bright("Hostname:"), formPanel, 0, row);
        WidgetUtils.addToGridBag(_hostnameTextField, formPanel, 1, row);
        row++;

        WidgetUtils.addToGridBag(DCLabel.bright("Port:"), formPanel, 0, row);
        WidgetUtils.addToGridBag(_portTextField, formPanel, 1, row);
        row++;
        
        WidgetUtils.addToGridBag(DCLabel.bright("Cluster name:"), formPanel, 0, row);
        WidgetUtils.addToGridBag(_clusterNameTextField, formPanel, 1, row);
        row++;
        
        WidgetUtils.addToGridBag(DCLabel.bright("Index name:"), formPanel, 0, row);
        WidgetUtils.addToGridBag(_indexNameTextField, formPanel, 1, row);
        row++;
        
        WidgetUtils.addToGridBag(DCLabel.bright("Schema model:"), formPanel, 0, row);
        WidgetUtils.addToGridBag(_tableDefinitionWidget, formPanel, 1, row);
        row++;

        final DCPanel buttonPanel = getButtonPanel();

        final DCPanel centerPanel = new DCPanel();
        centerPanel.setLayout(new VerticalLayout(4));
        centerPanel.add(formPanel);
        centerPanel.add(buttonPanel);

        return centerPanel;
    }

    protected ElasticSearchDatastore createDatastore() {
        final String name = _datastoreNameTextField.getText();
        final String hostname = _hostnameTextField.getText();
        final Integer port = Integer.parseInt(_portTextField.getText());
        final String clusterName = _clusterNameTextField.getText();
        final String indexName = _indexNameTextField.getText();
        return new ElasticSearchDatastore(name, hostname, port, clusterName, indexName);
    }

    @Override
    public Schema createSchema() {
        final ElasticSearchDatastore datastore = createDatastore();
        try (final DatastoreConnection con = datastore.openConnection()) {
            final Schema schema = con.getDataContext().getDefaultSchema();
            return schema;
        }
    }

    @Override
    protected String getDatastoreIconPath() {
        return IconUtils.ELASTICSEARCH_IMAGEPATH;
    }
}
