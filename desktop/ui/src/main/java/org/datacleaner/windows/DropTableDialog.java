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

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;

import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.drop.DropTable;
import org.apache.metamodel.schema.Table;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.UpdateableDatastore;
import org.datacleaner.connection.UpdateableDatastoreConnection;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.tree.SchemaTree;
import org.jdesktop.swingx.JXTextField;

public class DropTableDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    private final UpdateableDatastore _datastore;
    private final Table _table;
    private final SchemaTree _schemaTree;

    public DropTableDialog(WindowContext windowContext, UpdateableDatastore datastore, Table table,
            SchemaTree schemaTree) {
        super(windowContext, ImageManager.get().getImage("images/window/banner-tabledef.png"));

        _datastore = datastore;
        _table = table;
        _schemaTree = schemaTree;
    }

    @Override
    public String getWindowTitle() {
        return "Drop table '" + _table.getName() + "'";
    }

    @Override
    protected String getBannerTitle() {
        return "Drop table\n" + _table.getName();
    }

    @Override
    protected int getDialogWidth() {
        return 450;
    }

    @Override
    protected JComponent getDialogContent() {
        final DCLabel label = DCLabel
                .darkMultiLine("Warning! You are about to <b>drop</b> (delete) the table with name '<b>"
                        + _table.getName()
                        + "</b>'. This operation cannot be undone.<br/><br/>"
                        + "As a precaution to avoid unintended loss of data, please enter the name of the table below and click the 'Drop table' button to confirm the operation.");

        final JXTextField confirmTextField = WidgetFactory.createTextField("Enter the table's name to confirm");

        final JButton dropTableButton = WidgetFactory.createPrimaryButton("Drop table", IconUtils.ACTION_DROP_TABLE);
        dropTableButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!confirmTextField.getText().trim().equalsIgnoreCase(_table.getName().trim())) {
                    WidgetUtils.showErrorMessage("Enter the table's name to confirm",
                            "The names do not match. Please enter '" + _table.getName()
                                    + "' if you wish to drop the table.");
                    return;
                }
                doDropTable();
                _schemaTree.refreshDatastore();
                DropTableDialog.this.close();
            }
        });

        final JButton cancelButton = WidgetFactory.createDefaultButton("Cancel", IconUtils.ACTION_CANCEL);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DropTableDialog.this.close();
            }
        });

        final DCPanel panel = new DCPanel(WidgetUtils.COLOR_WELL_BACKGROUND);
        int row = 0;
        WidgetUtils.addToGridBag(label, panel, 0, row, 2, 1, GridBagConstraints.CENTER, 4, 1.0, 1.0);
        row++;
        WidgetUtils.addToGridBag(confirmTextField, panel, 0, row, 2, 1);
        row++;
        WidgetUtils.addToGridBag(dropTableButton, panel, 0, row, 0.5, 0.1);
        WidgetUtils.addToGridBag(cancelButton, panel, 1, row, 0.5, 0.1);
        
        panel.setPreferredSize(getDialogWidth(), 300);

        return panel;
    }

    protected boolean isWindowResizable() {
        return true;
    };

    protected void doDropTable() {
        final UpdateableDatastoreConnection con = _datastore.openConnection();
        try {
            final DropTable dropTable = new DropTable(_table);
            final UpdateableDataContext dataContext = con.getUpdateableDataContext();
            dataContext.executeUpdate(dropTable);
        } finally {
            con.close();
        }
    }

}
