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
import javax.swing.SwingConstants;

import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.SimpleTableDef;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.SchemaFactory;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.tabs.CloseableTabbedPane;
import org.datacleaner.widgets.tabs.TabCloseEvent;
import org.datacleaner.widgets.tabs.TabCloseListener;

/**
 * A panel for letting the user build multiple {@link SimpleTableDef}s
 */
public class SimpleTableDefsPanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    private static final Icon TABLE_ICON = ImageManager.get().getImageIcon(IconUtils.MODEL_TABLE);

    private final CloseableTabbedPane _tabbedPane;
    private final SchemaFactory _schemaFactory;
    private final DCLabel _instructionsLabel;

    private DCPanel _tabbedPaneContainer;

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
        _instructionsLabel = DCLabel.bright("Click 'Add table' above to define the first table.");
        _instructionsLabel.setFont(WidgetUtils.FONT_HEADER1);
        _instructionsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        _instructionsLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        _tabbedPaneContainer = new DCPanel(CloseableTabbedPane.COLOR_BACKGROUND);
        _tabbedPaneContainer.setLayout(new BorderLayout());


        setLayout(new BorderLayout());
        
        if (tableDefs == null || tableDefs.length == 0) {
            setTabbedPaneVisible(false);
        } else {
            for (SimpleTableDef tableDef : tableDefs) {
                addTableDef(tableDef);
            }
        }

        add(WidgetUtils.decorateWithShadow(_tabbedPaneContainer), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.NORTH);
        setMinimumSize(new Dimension(400, 300));

        _tabbedPane.addTabCloseListener(new TabCloseListener() {
            @Override
            public void tabClosed(TabCloseEvent ev) {
                if (_tabbedPane.getTabCount() == 0) {
                    setTabbedPaneVisible(false);
                }
            }
        });
    }

    private void setTabbedPaneVisible(boolean showTabbedPane) {
        _tabbedPaneContainer.removeAll();
        if (showTabbedPane) {
            _tabbedPaneContainer.add(_tabbedPane, BorderLayout.CENTER);
        } else {
            _tabbedPaneContainer.add(_instructionsLabel, BorderLayout.CENTER);
        }
        _tabbedPaneContainer.updateUI();
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

        final DCPanel buttonPanel;

        if (_schemaFactory != null) {
            final JButton autoDetectButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REFRESH);
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
            buttonPanel = DCPanel.flow(Alignment.RIGHT, 10, 10, addButton, autoDetectButton);
        } else {
            buttonPanel = DCPanel.flow(Alignment.RIGHT, 10, 10, addButton);
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
        setTabbedPaneVisible(true);
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
