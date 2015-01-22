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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;

import org.apache.metamodel.util.Action;
import org.apache.metamodel.util.SimpleTableDef;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.CouchDbDatastore;
import org.datacleaner.connection.MongoDbDatastore;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.panels.SimpleTableDefsPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.SchemaFactory;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Alignment;

/**
 * A dialog presenting a {@link SimpleTableDefsPanel} and a save button. Used
 * for defining tables for {@link MongoDbDatastore}s and
 * {@link CouchDbDatastore}s.
 */
public class TableDefinitionDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    private final SimpleTableDefsPanel _tableDefsPanel;
    private final Action<SimpleTableDef[]> _saveAction;

    public TableDefinitionDialog(WindowContext windowContext, SchemaFactory schemaFactory, SimpleTableDef[] tableDefs,
            Action<SimpleTableDef[]> saveAction) {
        super(windowContext, ImageManager.get().getImage("images/window/banner-tabledef.png"));
        
        setBackgroundColor(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        
        _tableDefsPanel = new SimpleTableDefsPanel(schemaFactory, tableDefs);
        _saveAction = saveAction;
    }

    @Override
    public String getWindowTitle() {
        return "Define tables";
    }

    @Override
    protected String getBannerTitle() {
        return "Define tables";
    }

    @Override
    protected int getDialogWidth() {
        return 500;
    }

    @Override
    protected boolean isWindowResizable() {
        return true;
    }

    @Override
    protected JComponent getDialogContent() {
        DCPanel panel = new DCPanel();
        panel.setLayout(new BorderLayout());
        panel.add(_tableDefsPanel, BorderLayout.CENTER);
        panel.add(createButtonPanel(), BorderLayout.SOUTH);
        panel.setPreferredSize(getDialogWidth(), 400);
        return panel;
    }

    private DCPanel createButtonPanel() {
        final JButton saveButton = WidgetFactory.createPrimaryButton("Save table definitions", IconUtils.ACTION_SAVE);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                final SimpleTableDef[] tableDefs = _tableDefsPanel.getTableDefs();
                try {
                    _saveAction.run(tableDefs);
                } catch (Exception e) {
                    WidgetUtils.showErrorMessage("Could not save table definitions", e);
                }
                TableDefinitionDialog.this.dispose();
            }
        });

        final JButton cancelButton = WidgetFactory.createDefaultButton("Cancel", IconUtils.ACTION_CANCEL);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TableDefinitionDialog.this.dispose();
            }
        });

        final DCPanel buttonPanel = DCPanel.flow(Alignment.CENTER, saveButton, cancelButton);
        buttonPanel.setBorder(WidgetUtils.BORDER_EMPTY);
        return buttonPanel;
    }
}
