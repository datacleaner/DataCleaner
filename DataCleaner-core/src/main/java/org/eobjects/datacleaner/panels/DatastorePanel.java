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
package org.eobjects.datacleaner.panels;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eobjects.analyzer.connection.AccessDatastore;
import org.eobjects.analyzer.connection.CompositeDatastore;
import org.eobjects.analyzer.connection.CsvDatastore;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DbaseDatastore;
import org.eobjects.analyzer.connection.ExcelDatastore;
import org.eobjects.analyzer.connection.FileDatastore;
import org.eobjects.analyzer.connection.FixedWidthDatastore;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.connection.OdbDatastore;
import org.eobjects.analyzer.connection.XmlDatastore;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.user.MutableDatastoreCatalog;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.util.WindowManager;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.windows.AccessDatastoreDialog;
import org.eobjects.datacleaner.windows.CompositeDatastoreDialog;
import org.eobjects.datacleaner.windows.CsvDatastoreDialog;
import org.eobjects.datacleaner.windows.DbaseDatastoreDialog;
import org.eobjects.datacleaner.windows.ExcelDatastoreDialog;
import org.eobjects.datacleaner.windows.FixedWidthDatastoreDialog;
import org.eobjects.datacleaner.windows.JdbcDatastoreDialog;
import org.eobjects.datacleaner.windows.OdbDatastoreDialog;
import org.eobjects.datacleaner.windows.XmlDatastoreDialog;

/**
 * A panel that presents a datastore and shows edit/remove buttons. This panel
 * is placed as a child inside the {@link DatastoreListPanel}.
 * 
 * @author Kasper Sørensen
 */
public class DatastorePanel extends DCPanel {

	private static final long serialVersionUID = 1L;

	private final Datastore _datastore;
	private final MutableDatastoreCatalog _datastoreCatalog;
	private final DatastoreListPanel _datastoreListPanel;
	private final JCheckBox _checkBox;
	private final WindowManager _windowManager;

	public DatastorePanel(Datastore datastore, MutableDatastoreCatalog datastoreCatalog,
			DatastoreListPanel datastoreListPanel, WindowManager windowManager) {
		super(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_LESS_BRIGHT);
		_datastore = datastore;
		_datastoreCatalog = datastoreCatalog;
		_datastoreListPanel = datastoreListPanel;
		_windowManager = windowManager;

		setOpaque(false);

		final Icon icon = IconUtils.getDatastoreIcon(datastore);
		final String description = getDescription(datastore);

		_checkBox = new JCheckBox();
		_checkBox.setOpaque(false);
		_checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_datastoreListPanel.setSelectedDatastorePanel(DatastorePanel.this);
			}
		});
		_checkBox.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				setOpaque(isSelected());
				updateUI();
			}
		});
		String datastoreName = datastore.getName();
		final DCLabel datastoreNameLabel = DCLabel.dark("<html><b>" + datastoreName + "</b><br/>" + description + "</html>");
		datastoreNameLabel.setIconTextGap(10);
		datastoreNameLabel.setIcon(icon);
		MouseAdapter invokeCheckBoxMouseListener = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				_checkBox.doClick();
				_datastoreListPanel.requestSearchFieldFocus();
				if (e.getClickCount() > 1) {
					// begin job on double click
					_datastoreListPanel.clickAnalyzeButton();
				}
			}
		};

		addMouseListener(invokeCheckBoxMouseListener);
		datastoreNameLabel.addMouseListener(invokeCheckBoxMouseListener);

		final JButton editButton = createEditButton(datastore);
		final JButton removeButton = createRemoveButton(datastore);

		setBorder(WidgetUtils.BORDER_LIST_ITEM);

		WidgetUtils.addToGridBag(DCPanel.flow(_checkBox, datastoreNameLabel), this, 0, 0, GridBagConstraints.WEST, 1.0, 1.0);
		WidgetUtils.addToGridBag(editButton, this, 1, 0, GridBagConstraints.EAST);
		WidgetUtils.addToGridBag(removeButton, this, 2, 0, GridBagConstraints.EAST);
	}

	public Datastore getDatastore() {
		return _datastore;
	}

	private JButton createRemoveButton(final Datastore datastore) {
		final String name = datastore.getName();
		final JButton removeButton = WidgetFactory.createSmallButton("images/actions/remove.png");
		removeButton.setToolTipText("Remove datastore");
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int result = JOptionPane.showConfirmDialog(DatastorePanel.this,
						"Are you sure you wish to remove the datastore '" + name + "'?", "Confirm remove",
						JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.YES_OPTION) {
					_datastoreCatalog.removeDatastore(datastore);
				}
			}
		});
		if (!_datastoreCatalog.isDatastoreMutable(name)) {
			removeButton.setEnabled(false);
		}
		return removeButton;
	}

	private JButton createEditButton(final Datastore datastore) {
		final JButton editButton = WidgetFactory.createSmallButton("images/actions/edit.png");
		editButton.setToolTipText("Edit datastore");

		if (datastore instanceof JdbcDatastore) {
			editButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JdbcDatastoreDialog dialog = new JdbcDatastoreDialog((JdbcDatastore) datastore, _datastoreCatalog,
							_windowManager);
					dialog.setVisible(true);
				}
			});
		} else if (datastore instanceof CsvDatastore) {
			editButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					CsvDatastoreDialog dialog = new CsvDatastoreDialog((CsvDatastore) datastore, _datastoreCatalog,
							_windowManager);
					dialog.setVisible(true);
				}
			});
		} else if (datastore instanceof AccessDatastore) {
			editButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					AccessDatastoreDialog dialog = new AccessDatastoreDialog((AccessDatastore) datastore, _datastoreCatalog,
							_windowManager);
					dialog.setVisible(true);
				}
			});
		} else if (datastore instanceof ExcelDatastore) {
			editButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ExcelDatastoreDialog dialog = new ExcelDatastoreDialog((ExcelDatastore) datastore, _datastoreCatalog,
							_windowManager);
					dialog.setVisible(true);
				}
			});
		} else if (datastore instanceof XmlDatastore) {
			editButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					XmlDatastoreDialog dialog = new XmlDatastoreDialog((XmlDatastore) datastore, _datastoreCatalog,
							_windowManager);
					dialog.setVisible(true);
				}
			});
		} else if (datastore instanceof OdbDatastore) {
			editButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					OdbDatastoreDialog dialog = new OdbDatastoreDialog((OdbDatastore) datastore, _datastoreCatalog,
							_windowManager);
					dialog.setVisible(true);
				}
			});
		} else if (datastore instanceof FixedWidthDatastore) {
			editButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					FixedWidthDatastoreDialog dialog = new FixedWidthDatastoreDialog((FixedWidthDatastore) datastore,
							_datastoreCatalog, _windowManager);
					dialog.setVisible(true);
				}
			});
		} else if (datastore instanceof DbaseDatastore) {
			editButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					DbaseDatastoreDialog dialog = new DbaseDatastoreDialog((DbaseDatastore) datastore, _datastoreCatalog,
							_windowManager);
					dialog.setVisible(true);
				}
			});
		} else if (datastore instanceof CompositeDatastore) {
			editButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					CompositeDatastoreDialog dialog = new CompositeDatastoreDialog((CompositeDatastore) datastore,
							_datastoreCatalog, _windowManager);
					dialog.setVisible(true);
				}
			});
		} else {
			editButton.setEnabled(false);
		}

		if (!_datastoreCatalog.isDatastoreMutable(datastore.getName())) {
			editButton.setEnabled(false);
		}

		return editButton;
	}

	private static String getDescription(Datastore datastore) {
		if (datastore.getDescription() != null) {
			return datastore.getDescription();
		}
		if (datastore instanceof FileDatastore) {
			return ((FileDatastore) datastore).getFilename();
		} else if (datastore instanceof JdbcDatastore) {
			JdbcDatastore jdbcDatastore = (JdbcDatastore) datastore;
			String jdbcUrl = jdbcDatastore.getJdbcUrl();
			String datasourceJndiUrl = jdbcDatastore.getDatasourceJndiUrl();
			if (StringUtils.isNullOrEmpty(datasourceJndiUrl)) {
				return jdbcUrl;
			}
			return datasourceJndiUrl;
		} else if (datastore instanceof CompositeDatastore) {
			List<Datastore> datastores = ((CompositeDatastore) datastore).getDatastores();
			StringBuilder sb = new StringBuilder();
			for (Datastore ds : datastores) {
				if (sb.length() != 0) {
					sb.append(", ");
				}
				sb.append(ds.getName());
			}
			return sb.toString();
		}
		return "";
	}

	public boolean isSelected() {
		return _checkBox.isSelected();
	}

	public void setSelected(boolean selected) {
		_checkBox.setSelected(selected);
	}
}
