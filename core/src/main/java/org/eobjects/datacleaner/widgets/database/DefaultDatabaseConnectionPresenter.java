/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.widgets.database;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.datacleaner.database.DatabaseDriverDescriptor;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.windows.JdbcDatastoreDialog;
import org.jdesktop.swingx.JXTextField;

/**
 * Default implementation of {@link DatabaseConnectionPresenter}, which simply
 * presents each field as a text box.
 */
public class DefaultDatabaseConnectionPresenter extends AbstractDatabaseConnectionPresenter {

	private static final ImageManager imageManager = ImageManager.getInstance();

	private final JXTextField _connectionStringTextField;
	private final JButton _connectionStringTemplateButton;

	private volatile String[] _connectionUrls;

	public DefaultDatabaseConnectionPresenter() {
		_connectionStringTextField = WidgetFactory.createTextField("Connection string / URL",
				JdbcDatastoreDialog.TEXT_FIELD_WIDTH);

		_connectionStringTextField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "nextTemplateItem");
		_connectionStringTextField.getActionMap().put("nextTemplateItem", getNextTemplateItemAction());
		_connectionStringTemplateButton = new JButton(imageManager.getImageIcon("images/widgets/help.png",
				IconUtils.ICON_SIZE_SMALL));
		_connectionStringTemplateButton.setMargin(new Insets(0, 0, 0, 0));
		_connectionStringTemplateButton.setOpaque(false);
		_connectionStringTemplateButton.setBorder(null);
		_connectionStringTemplateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (_connectionUrls != null) {
					final JPopupMenu menu = new JPopupMenu();
					for (final String connectionUrl : _connectionUrls) {
						final JMenuItem menuItem = new JMenuItem(connectionUrl);
						menuItem.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								_connectionStringTextField.setText(connectionUrl);
								getNextTemplateItemAction().actionPerformed(null);
							}
						});
						menu.add(menuItem);
					}
					menu.show(_connectionStringTemplateButton, 0, 0);
				}
			}
		});
	}

	@Override
	public boolean initialize(JdbcDatastore datastore) {
		super.initialize(datastore);
		_connectionStringTextField.setText(datastore.getJdbcUrl());
		return true;
	}

	@Override
	protected int layoutGridBagAboveCredentials(DCPanel panel) {
		int row = 0;
		WidgetUtils.addToGridBag(DCLabel.dark("Connection string:"), panel, 0, row);
		WidgetUtils.addToGridBag(_connectionStringTextField, panel, 1, row);
		WidgetUtils.addToGridBag(_connectionStringTemplateButton, panel, 2, row, 1.0d, 0.0d);

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
			public void actionPerformed(ActionEvent e) {
				String text = _connectionStringTextField.getText();
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
	public void setSelectedDatabaseDriver(DatabaseDriverDescriptor driver) {
		if (driver == null) {
			setConnectionUrlTemplates(null);
		} else {
			String[] connectionUrls = driver.getConnectionUrlTemplates();
			setConnectionUrlTemplates(connectionUrls);
		}
	}

	private void setConnectionUrlTemplates(String[] connectionUrls) {
		_connectionUrls = connectionUrls;
		boolean selectable = false;

		if (connectionUrls != null && connectionUrls.length > 0) {
			if (connectionUrls.length > 1) {
				selectable = true;
			}

			_connectionStringTextField.setFocusTraversalKeysEnabled(false);
			String url = connectionUrls[0];
			_connectionStringTextField.setText(url);

			getNextTemplateItemAction().actionPerformed(null);
		}

		_connectionStringTemplateButton.setVisible(selectable);
	}
}
