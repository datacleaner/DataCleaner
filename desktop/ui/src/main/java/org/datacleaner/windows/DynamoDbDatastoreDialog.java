/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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
import javax.swing.JPasswordField;
import javax.swing.event.DocumentEvent;

import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.util.SimpleTableDef;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.DynamoDbDatastore;
import org.datacleaner.connection.UpdateableDatastoreConnection;
import org.datacleaner.guice.Nullable;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImmutableEntry;
import org.datacleaner.util.SchemaFactory;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.widgets.DCComboBox;
import org.datacleaner.widgets.TableDefinitionOptionSelectionPanel;
import org.jdesktop.swingx.JXTextField;

import com.amazonaws.regions.Regions;

public class DynamoDbDatastoreDialog extends AbstractDatastoreDialog<DynamoDbDatastore> implements SchemaFactory {

    private static final long serialVersionUID = 1L;

    private final DCComboBox<Regions> _regionField;
    private final JXTextField _accessKeyIdField;
    private final JPasswordField _secretAccessKeyField;
    private final TableDefinitionOptionSelectionPanel _tableDefinitionWidget;

    @Inject
    public DynamoDbDatastoreDialog(final WindowContext windowContext, final MutableDatastoreCatalog catalog,
            @Nullable final DynamoDbDatastore originalDatastore, final UserPreferences userPreferences) {
        super(originalDatastore, catalog, windowContext, userPreferences);

        _regionField = new DCComboBox<>(Regions.values());
        _regionField.setSelectedItem(Regions.DEFAULT_REGION);
        _accessKeyIdField = WidgetFactory.createTextField();
        _secretAccessKeyField = WidgetFactory.createPasswordField();

        _accessKeyIdField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(final DocumentEvent event) {
                validateAndUpdate();
            }
        });
        _secretAccessKeyField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(final DocumentEvent event) {
                validateAndUpdate();
            }
        });

        if (originalDatastore == null) {
            _tableDefinitionWidget = new TableDefinitionOptionSelectionPanel(windowContext, this, null);
        } else {
            _datastoreNameTextField.setText(originalDatastore.getName());
            _datastoreNameTextField.setEnabled(false);
            _accessKeyIdField.setText(originalDatastore.getAccessKeyId());
            _secretAccessKeyField.setText(originalDatastore.getSecretAccessKey());
            final String originalRegion = originalDatastore.getRegion();
            try {
                final Regions region = Regions.fromName(originalRegion);
                _regionField.setSelectedItem(region);
            } catch (IllegalArgumentException e) {
                // unable to resolve region, ignore.
            }
            _tableDefinitionWidget =
                    new TableDefinitionOptionSelectionPanel(windowContext, this, originalDatastore.getTableDefs());
        }
    }

    @Override
    public String getWindowTitle() {
        return "AWS DynamoDB database";
    }

    @Override
    protected String getBannerTitle() {
        return "AWS DynamoDB database";
    }

    @Override
    protected boolean validateForm() {
        final String datastoreName = _datastoreNameTextField.getText();
        if (StringUtils.isNullOrEmpty(datastoreName)) {
            setStatusError("Please enter a datastore name");
            return false;
        }

        setStatusValid();
        return true;
    }

    protected DynamoDbDatastore createDatastore() {
        final String name = _datastoreNameTextField.getText();
        final String region = _regionField.getSelectedItem().getName();
        final String accessKeyId = _accessKeyIdField.getText();
        final String secretAccessKey = new String(_secretAccessKeyField.getPassword());
        final SimpleTableDef[] tableDefs = _tableDefinitionWidget.getTableDefs();
        return new DynamoDbDatastore(name, region, accessKeyId, secretAccessKey, tableDefs);
    }

    @Override
    public Schema createSchema() {
        final DynamoDbDatastore datastore = createDatastore();
        try (UpdateableDatastoreConnection con = datastore.openConnection()) {
            return con.getDataContext().getDefaultSchema();
        }
    }

    @Override
    protected String getDatastoreIconPath() {
        return IconUtils.DYNAMODB_IMAGEPATH;
    }

    @Override
    protected List<Entry<String, JComponent>> getFormElements() {
        final List<Entry<String, JComponent>> result = super.getFormElements();
        result.add(new ImmutableEntry<>("Region", _regionField));
        result.add(new ImmutableEntry<>("Access key ID", _accessKeyIdField));
        result.add(new ImmutableEntry<>("Secret access key", _secretAccessKeyField));
        result.add(new ImmutableEntry<>("Schema model", _tableDefinitionWidget));
        return result;
    }
}
