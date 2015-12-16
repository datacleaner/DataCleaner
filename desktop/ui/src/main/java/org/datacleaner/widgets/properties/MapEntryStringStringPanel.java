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
package org.datacleaner.widgets.properties;

import java.util.Map.Entry;

import javax.swing.BoxLayout;
import javax.swing.event.DocumentListener;

import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.ImmutableEntry;
import org.datacleaner.util.WidgetFactory;
import org.jdesktop.swingx.JXTextField;

import com.google.common.base.Strings;

public class MapEntryStringStringPanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    private final JXTextField _keyField;
    private final JXTextField _valueField;

    public MapEntryStringStringPanel(String key, String value) {
        super();
        _keyField = WidgetFactory.createTextField("Key");
        _valueField = WidgetFactory.createTextField("Value");
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        setEntryKey(key);
        setEntryValue(value);

        add(_keyField);
        add(_valueField);
    }

    public boolean isSet() {
        // at least the key needs to be set - we do allow empty values
        return !Strings.isNullOrEmpty(getEntryKey());
    }

    public String getEntryKey() {
        return _keyField.getText();
    }

    public String getEntryValue() {
        return _valueField.getText();
    }

    public void setEntryKey(String key) {
        if (key == null) {
            key = "";
        }
        _keyField.setText(key);
    }

    public void setEntryValue(String value) {
        if (value == null) {
            value = "";
        }
        _valueField.setText(value);
    }

    public void addDocumentListener(DocumentListener listener) {
        _keyField.getDocument().addDocumentListener(listener);
        _valueField.getDocument().addDocumentListener(listener);
    }

    public void setEntry(Entry<String, String> entry) {
        setEntryKey(entry.getKey());
        setEntryValue(entry.getValue());
    }

    public Entry<String, String> getEntry() {
        return new ImmutableEntry<>(getEntryKey(), getEntryValue());
    }
}
