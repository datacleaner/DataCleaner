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
import java.util.List;

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

/**
 * A panel for letting the user build multiple {@link SimpleTableDef}s
 */
public class SimpleTableDefsPanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    private static final Icon TABLE_ICON =
            ImageManager.get().getImageIcon(IconUtils.MODEL_TABLE, IconUtils.ICON_SIZE_TAB);

    private final CloseableTabbedPane _tabbedPane;
    private final SchemaFactory _schemaFactory;
    private final DCLabel _instructionsLabel;

    private DCPanel _tabbedPaneContainer;

    public SimpleTableDefsPanel() {
        this(null);
    }

    public SimpleTableDefsPanel(final SchemaFactory schemaFactory) {
        this(schemaFactory, null);
    }

    public SimpleTableDefsPanel(final SchemaFactory schemaFactory, final SimpleTableDef[] tableDefs) {
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
            for (final SimpleTableDef tableDef : tableDefs) {
                addTableDef(tableDef);
            }
        }

        add(WidgetUtils.decorateWithShadow(_tabbedPaneContainer), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.NORTH);
        setMinimumSize(new Dimension(400, 300));

        _tabbedPane.addTabCloseListener(ev -> {
            if (_tabbedPane.getTabCount() == 0) {
                setTabbedPaneVisible(false);
            }
        });
    }

    private void setTabbedPaneVisible(final boolean showTabbedPane) {
        _tabbedPaneContainer.removeAll();
        if (showTabbedPane) {
            _tabbedPaneContainer.add(_tabbedPane, BorderLayout.CENTER);
        } else {
            _tabbedPaneContainer.add(_instructionsLabel, BorderLayout.CENTER);
        }
        _tabbedPaneContainer.updateUI();
    }

    private DCPanel createButtonPanel() {
        final JButton addButton = WidgetFactory.createSmallButton(IconUtils.ACTION_ADD_DARK);
        addButton.setText("Add table");
        addButton.addActionListener(e -> {
            final String tableName = JOptionPane
                    .showInputDialog(SimpleTableDefsPanel.this, "What is the name of the table", "Add table",
                            JOptionPane.QUESTION_MESSAGE);
            if (!StringUtils.isNullOrEmpty(tableName)) {
                final SimpleTableDef tableDef = new SimpleTableDef(tableName, new String[0]);
                addTableDef(tableDef);
            }
        });

        final DCPanel buttonPanel;

        if (_schemaFactory != null) {
            final JButton autoDetectButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REFRESH);
            autoDetectButton.setText("Auto-detect tables");
            autoDetectButton.addActionListener(e -> {
                removeAllTableDefs();

                final Schema schema = _schemaFactory.createSchema();
                final List<Table> tables = schema.getTables();
                for (final Table table : tables) {
                    addTableDef(createTableDef(table));
                }
            });
            buttonPanel = DCPanel.flow(Alignment.RIGHT, 10, 10, addButton, autoDetectButton);
        } else {
            buttonPanel = DCPanel.flow(Alignment.RIGHT, 10, 10, addButton);
        }

        return buttonPanel;
    }

    private SimpleTableDef createTableDef(final Table table) {
        final int columnCount = table.getColumnCount();
        final String[] names = new String[columnCount];
        final ColumnType[] types = new ColumnType[columnCount];
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

    public void addTableDef(final SimpleTableDef tableDef) {
        _tabbedPane.addTab(tableDef.getName(), TABLE_ICON, new SimpleTableDefPanel(tableDef));
        setTabbedPaneVisible(true);
    }

    public SimpleTableDef[] getTableDefs() {
        final int tabCount = _tabbedPane.getTabCount();
        final SimpleTableDef[] result = new SimpleTableDef[tabCount];
        for (int i = 0; i < result.length; i++) {
            result[i] = getTableDef(i);
        }
        return result;
    }

    private SimpleTableDef getTableDef(final int index) {
        final Component component = _tabbedPane.getComponentAt(index);
        final SimpleTableDefPanel panel = (SimpleTableDefPanel) component;
        return panel.getTableDef();
    }
}
