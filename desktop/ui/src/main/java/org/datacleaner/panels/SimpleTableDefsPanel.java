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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.datacleaner.util.StringUtils;
import org.datacleaner.util.SchemaFactory;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.widgets.tabs.CloseableTabbedPane;
import org.datacleaner.widgets.tabs.TabCloseEvent;
import org.datacleaner.widgets.tabs.TabCloseListener;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.SimpleTableDef;

/**
 * A panel for letting the user build multiple {@link SimpleTableDef}s
 */
public class SimpleTableDefsPanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    private static final Icon TABLE_ICON = ImageManager.get().getImageIcon("images/model/table.png");

    private final CloseableTabbedPane _tabbedPane;
    private final SchemaFactory _schemaFactory;

    public SimpleTableDefsPanel() {
        this(null);
    }

    public SimpleTableDefsPanel(SchemaFactory schemaFactory) {
        this(schemaFactory, null);
    }

    public SimpleTableDefsPanel(SchemaFactory schemaFactory, SimpleTableDef[] tableDefs) {
        super();
        _schemaFactory = schemaFactory;
        _tabbedPane = new CloseableTabbedPane();
        _tabbedPane.setVisible(false);

        if (tableDefs != null) {
            for (SimpleTableDef tableDef : tableDefs) {
                addTableDef(tableDef);
            }
        }

        setLayout(new BorderLayout());
        add(_tabbedPane, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.NORTH);
        setMinimumSize(new Dimension(400, 300));

        _tabbedPane.addTabCloseListener(new TabCloseListener() {
            @Override
            public void tabClosed(TabCloseEvent ev) {
                if (_tabbedPane.getTabCount() == 0) {
                    _tabbedPane.setVisible(false);
                }
            }
        });
    }

    private DCPanel createButtonPanel() {
        final JButton addButton = WidgetFactory.createSmallButton(IconUtils.ACTION_ADD);
        addButton.setText("Add table");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final String tableName = JOptionPane.showInputDialog(SimpleTableDefsPanel.this,
                        "What is the name of the table", "Add table", JOptionPane.QUESTION_MESSAGE);
                if (!StringUtils.isNullOrEmpty(tableName)) {
                    SimpleTableDef tableDef = new SimpleTableDef(tableName, new String[0]);
                    addTableDef(tableDef);
                }
            }
        });

        final DCPanel buttonPanel = DCPanel.flow(Alignment.RIGHT, 10, 10, addButton);

        if (_schemaFactory != null) {
            final JButton autoDetectButton = WidgetFactory.createSmallButton("images/actions/refresh.png");
            autoDetectButton.setText("Auto-detect tables");
            autoDetectButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    removeAllTableDefs();

                    Schema schema = _schemaFactory.createSchema();
                    Table[] tables = schema.getTables();
                    for (Table table : tables) {
                        addTableDef(createTableDef(table));
                    }
                }
            });
            buttonPanel.add(autoDetectButton);
        }

        return buttonPanel;
    }

    private SimpleTableDef createTableDef(Table table) {
        int columnCount = table.getColumnCount();
        String[] names = new String[columnCount];
        ColumnType[] types = new ColumnType[columnCount];
        for (int i = 0; i < columnCount; i++) {
            names[i] = table.getColumn(i).getName();
            types[i] = table.getColumn(i).getType();
        }
        return new SimpleTableDef(table.getName(), names, types);
    }

    public void removeAllTableDefs() {
        while (_tabbedPane.getTabCount() > 0) {
            _tabbedPane.removeTabAt(_tabbedPane.getTabCount() - 1);
        }
    }

    public void addTableDef(SimpleTableDef tableDef) {
        _tabbedPane.addTab(tableDef.getName(), TABLE_ICON, new SimpleTableDefPanel(tableDef));
        _tabbedPane.setVisible(true);
    }

    public SimpleTableDef[] getTableDefs() {
        int tabCount = _tabbedPane.getTabCount();
        SimpleTableDef[] result = new SimpleTableDef[tabCount];
        for (int i = 0; i < result.length; i++) {
            result[i] = getTableDef(i);
        }
        return result;
    }

    private SimpleTableDef getTableDef(int index) {
        Component component = _tabbedPane.getComponentAt(index);
        SimpleTableDefPanel panel = (SimpleTableDefPanel) component;
        return panel.getTableDef();
    }
}
