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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.Box;
import javax.swing.JButton;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.MutableColumn;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.NumberDocument;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCCheckBox;
import org.datacleaner.widgets.DCCheckBox.Listener;
import org.datacleaner.widgets.DCComboBox;
import org.datacleaner.windows.CreateTableDialog;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXTextField;

import com.google.common.base.Strings;

/**
 * Panel used by {@link CreateTableDialog} to show and edit the definition of a
 * column in a table that is being created.
 */
public class CreateTableColumnDefintionPanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    private static final Collection<ColumnType> AVAILABLE_COLUMN_TYPES = Arrays.asList(ColumnType.STRING,
            ColumnType.VARCHAR, ColumnType.CHAR, ColumnType.CLOB, ColumnType.NUMBER, ColumnType.BIGINT,
            ColumnType.INTEGER, ColumnType.SMALLINT, ColumnType.TINYINT, ColumnType.DOUBLE, ColumnType.FLOAT,
            ColumnType.BOOLEAN, ColumnType.BIT, ColumnType.TIMESTAMP, ColumnType.DATE, ColumnType.BINARY,
            ColumnType.BLOB);

    private final CreateTableDialog _parentDialog;
    private final JXTextField _nameTextField;
    private final DCComboBox<ColumnType> _columnTypeComboBox;
    private final JXTextField _sizeTextField;
    private final DCCheckBox<Boolean> _primaryKeyCheckBox;

    // may be null - which means in MetaModel that we do not know/decide if the
    // column is nullable or not.
    private Boolean _notNull;

    public CreateTableColumnDefintionPanel(CreateTableDialog parentDialog) {
        this(parentDialog, "", ColumnType.STRING, false);
    }

    public CreateTableColumnDefintionPanel(CreateTableDialog parentDialog, String name, ColumnType columnType,
            boolean primaryKey) {
        super(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        _parentDialog = parentDialog;
        _nameTextField = WidgetFactory.createTextField("Column name");
        _nameTextField.setText(name);
        _columnTypeComboBox = new DCComboBox<ColumnType>(AVAILABLE_COLUMN_TYPES);
        _columnTypeComboBox.setSelectedItem(columnType);
        _sizeTextField = WidgetFactory.createTextField("Size", 4);
        _sizeTextField.setDocument(new NumberDocument(false, false));
        _primaryKeyCheckBox = new DCCheckBox<Boolean>("Primary key?", primaryKey);
        final DCCheckBox<Boolean> notNullCheckBox = new DCCheckBox<Boolean>("Not null?", false);
        notNullCheckBox.addListener(new Listener<Boolean>() {
            @Override
            public void onItemSelected(Boolean item, boolean selected) {
                _notNull = selected;
            }
        });

        final JButton removeButton = WidgetFactory.createSmallButton("Remove", IconUtils.ACTION_REMOVE);
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                _parentDialog.removeColumnDefinitionPanel(CreateTableColumnDefintionPanel.this);
            }
        });

        final DCPanel buttonPanel = DCPanel.around(removeButton);
        buttonPanel.setBorder(WidgetUtils.BORDER_EMPTY);

        setLayout(new HorizontalLayout());
        setBorder(WidgetUtils.BORDER_LIST_ITEM);

        add(buttonPanel);
        add(_nameTextField);
        add(Box.createHorizontalStrut(10));
        add(_columnTypeComboBox);
        add(_sizeTextField);
        add(Box.createHorizontalStrut(10));
        add(_primaryKeyCheckBox);
        add(Box.createHorizontalStrut(10));
        add(notNullCheckBox);
    }

    public Column toColumn() {
        final String name = _nameTextField.getText();
        final ColumnType columnType = _columnTypeComboBox.getSelectedItem();
        final Integer size = getColumnSize();
        final boolean primaryKey = _primaryKeyCheckBox.isSelected();

        return new MutableColumn(name, columnType).setColumnSize(size).setPrimaryKey(primaryKey).setNullable(_notNull);
    }

    private Integer getColumnSize() {
        final String text = _sizeTextField.getText();
        if (Strings.isNullOrEmpty(text)) {
            return null;
        }
        return Integer.parseInt(text);
    }

    public boolean isColumnDefined() {
        return !Strings.isNullOrEmpty(_nameTextField.getText());
    }

    public void highlightIssues() {
        if (Strings.isNullOrEmpty(_nameTextField.getText())) {
            _nameTextField.setBorder(WidgetUtils.BORDER_EMPHASIZE_FIELD);
        } else {
            _nameTextField.setBorder(WidgetUtils.BORDER_INPUT);
        }
    }

}
