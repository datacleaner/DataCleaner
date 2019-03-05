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

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;

import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.KafkaDatastore;
import org.datacleaner.connection.KafkaDatastore.KeyValueType;
import org.datacleaner.guice.Nullable;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImmutableEntry;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.widgets.DCComboBox;
import org.datacleaner.widgets.EnumComboBoxListRenderer;
import org.jdesktop.swingx.JXTextField;

import com.google.common.base.Splitter;

/**
 * Dialog for setup of Apache Kafka stream. I used the CouchDbDatastoreDialog as the template (copy-paste source) for this one.
 * 
 * The "Topics" text field is comma delimited for simplicism.
 * 
 * @author davkrause
 */
public class KafkaDatastoreDialog extends AbstractDatastoreDialog<KafkaDatastore> {

    private static final long serialVersionUID = 1L;

    private final JXTextField _bootstrapServersTextField;
    private final JXTextField _topicsTextField; // comma delimited topics
    private final DCComboBox<KafkaDatastore.KeyValueType> _keyTypeCombo;
    private final DCComboBox<KafkaDatastore.KeyValueType> _valueTypeCombo;

    @Inject
    public KafkaDatastoreDialog(final WindowContext windowContext, final MutableDatastoreCatalog catalog,
            @Nullable final KafkaDatastore originalDatastore, final UserPreferences userPreferences) {
        super(originalDatastore, catalog, windowContext, userPreferences);

        _bootstrapServersTextField = WidgetFactory.createTextField();
        _topicsTextField = WidgetFactory.createTextField();
        _keyTypeCombo = createKeyValueTypeCombo();
        _valueTypeCombo = createKeyValueTypeCombo();

        _bootstrapServersTextField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(final DocumentEvent event) {
                validateAndUpdate();
            }
        });
        _topicsTextField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(final DocumentEvent event) {
                validateAndUpdate();
            }
        });

        if (originalDatastore == null) {
            _bootstrapServersTextField.setText("localhost:9092");
            _topicsTextField.setText("my_topic");
        } else {
            _datastoreNameTextField.setText(originalDatastore.getName());
            _datastoreNameTextField.setEnabled(false);
            _bootstrapServersTextField.setText(originalDatastore.getBootstrapServers());
            _topicsTextField.setText(originalDatastore.getTopics().stream().collect(Collectors.joining(",")));
            _keyTypeCombo.setSelectedItem(originalDatastore.getKeyType());
            _valueTypeCombo.setSelectedItem(originalDatastore.getValueType());
        }
    }

    private DCComboBox<KeyValueType> createKeyValueTypeCombo() {
        final DCComboBox<KeyValueType> c = new DCComboBox<>(KafkaDatastore.KeyValueType.values());
        c.setRenderer(new EnumComboBoxListRenderer());
        c.setSelectedItem(KafkaDatastore.KeyValueType.STRING);
        return c;
    }

    @Override
    public String getWindowTitle() {
        return "Kafka stream";
    }

    @Override
    protected String getBannerTitle() {
        return "Kafka stream";
    }

    @Override
    protected boolean validateForm() {
        final String datastoreName = _datastoreNameTextField.getText();
        if (StringUtils.isNullOrEmpty(datastoreName)) {
            setStatusError("Please enter a datastore name");
            return false;
        }

        final String hostname = _bootstrapServersTextField.getText();
        if (StringUtils.isNullOrEmpty(hostname)) {
            setStatusError("Please enter bootstrap servers");
            return false;
        }

        final String topics = _topicsTextField.getText();
        if (StringUtils.isNullOrEmpty(topics)) {
            setStatusError("Please enter topics");
            return false;
        }

        setStatusValid();
        return true;
    }

    protected KafkaDatastore createDatastore() {
        final String name = _datastoreNameTextField.getText();
        final String bootstrapServers = _bootstrapServersTextField.getText();
        final String topicsText = _topicsTextField.getText();
        final Collection<String> topics = Splitter.on(',').trimResults().omitEmptyStrings().splitToList(topicsText);
        final KeyValueType keyType = _keyTypeCombo.getSelectedItem();
        final KeyValueType valueType = _valueTypeCombo.getSelectedItem();
        return new KafkaDatastore(name, bootstrapServers, topics, keyType, valueType);
    }

    @Override
    protected String getDatastoreIconPath() {
        return IconUtils.KAFKA_IMAGEPATH;
    }

    @Override
    protected List<Entry<String, JComponent>> getFormElements() {
        final List<Entry<String, JComponent>> result = super.getFormElements();
        result.add(new ImmutableEntry<>("Bootstrap servers", _bootstrapServersTextField));
        result.add(new ImmutableEntry<>("Topic(s)", _topicsTextField));
        result.add(new ImmutableEntry<>("Key type", _keyTypeCombo));
        result.add(new ImmutableEntry<>("Value type", _valueTypeCombo));
        return result;
    }
}
