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
package org.datacleaner.widgets.database;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import org.datacleaner.connection.JdbcDatastore;
import org.datacleaner.database.DatabaseDriverDescriptor;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.windows.JdbcDatastoreDialog;
import org.jdesktop.swingx.JXTextField;

/**
 * Default implementation of {@link DatabaseConnectionPresenter}, which simply
 * presents each field as a text box.
 */
public class DefaultDatabaseConnectionPresenter extends AbstractDatabaseConnectionPresenter {

    private final JXTextField _connectionStringTextField;
    private final JButton _connectionStringTemplateButton;

    private volatile String[] _connectionUrls;

    public DefaultDatabaseConnectionPresenter() {
        _connectionStringTextField =
                WidgetFactory.createTextField("Connection string / URL", JdbcDatastoreDialog.TEXT_FIELD_WIDTH);

        _connectionStringTextField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "nextTemplateItem");
        _connectionStringTextField.getActionMap().put("nextTemplateItem", getNextTemplateItemAction());
        _connectionStringTemplateButton = WidgetFactory.createSmallButton(IconUtils.ACTION_HELP);
        _connectionStringTemplateButton.addActionListener(e -> {
            if (_connectionUrls != null) {
                final JPopupMenu menu = new JPopupMenu();
                for (final String connectionUrl : _connectionUrls) {
                    final JMenuItem menuItem = new JMenuItem(connectionUrl);
                    menuItem.addActionListener(e1 -> {
                        _connectionStringTextField.setText(connectionUrl);
                        getNextTemplateItemAction().actionPerformed(null);
                    });
                    menu.add(menuItem);
                }
                menu.show(_connectionStringTemplateButton, 0, 0);
            }
        });
    }

    @Override
    public boolean initialize(final JdbcDatastore datastore) {
        super.initialize(datastore);
        _connectionStringTextField.setText(datastore.getJdbcUrl());
        return true;
    }

    @Override
    protected int layoutGridBagAboveCredentials(final DCPanel panel) {
        final int row = 0;
        WidgetUtils.addToGridBag(DCLabel.dark("Connection string:"), panel, 0, row);
        WidgetUtils.addToGridBag(_connectionStringTextField, panel, 1, row, 1.0, 0.0);
        WidgetUtils.addToGridBag(_connectionStringTemplateButton, panel, 2, row, 0.0d, 0.0d);

        return row;
    }

    @Override
    public String getJdbcUrl() {
        return _connectionStringTextField.getText();
    }

    /**
     * @return an action listener that will set the correct focus, either inside
     *         a template connection url or the next text field.
     */
    private Action getNextTemplateItemAction() {
        return new AbstractAction() {

            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                final String text = _connectionStringTextField.getText();
                int selectionEnd = _connectionStringTextField.getSelectionEnd();
                int selectionStart = text.indexOf('<', selectionEnd);
                if (selectionStart != -1) {
                    selectionEnd = text.indexOf('>', selectionStart);
                }

                if (selectionStart != -1 && selectionEnd != -1) {
                    _connectionStringTextField.setSelectionStart(selectionStart);
                    _connectionStringTextField.setSelectionEnd(selectionEnd + 1);
                    _connectionStringTextField.requestFocus();
                } else {
                    selectionStart = text.indexOf('<');
                    if (selectionStart != -1) {
                        selectionEnd = text.indexOf('>', selectionStart);
                        if (selectionEnd != -1) {
                            _connectionStringTextField.setSelectionStart(selectionStart);
                            _connectionStringTextField.setSelectionEnd(selectionEnd + 1);
                            _connectionStringTextField.requestFocus();
                        } else {
                            getUsernameTextField().requestFocus();
                        }
                    } else {
                        getUsernameTextField().requestFocus();
                    }
                }

                _connectionStringTextField.getHorizontalVisibility().setValue(0);
            }
        };
    }

    @Override
    public void setSelectedDatabaseDriver(final DatabaseDriverDescriptor driver) {
        if (driver == null) {
            setConnectionUrlTemplates(null);
        } else {
            final String[] connectionUrls = driver.getConnectionUrlTemplates();
            setConnectionUrlTemplates(connectionUrls);
        }
    }

    private void setConnectionUrlTemplates(final String[] connectionUrls) {
        _connectionUrls = connectionUrls;
        boolean selectable = false;

        if (connectionUrls != null && connectionUrls.length > 0) {
            if (connectionUrls.length > 1) {
                selectable = true;
            }

            _connectionStringTextField.setFocusTraversalKeysEnabled(false);
            final String url = connectionUrls[0];
            _connectionStringTextField.setText(url);

            getNextTemplateItemAction().actionPerformed(null);
        }

        _connectionStringTemplateButton.setVisible(selectable);
    }
}
