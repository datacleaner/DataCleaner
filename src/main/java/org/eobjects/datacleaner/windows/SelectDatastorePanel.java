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
package org.eobjects.datacleaner.windows;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.AccessDatastore;
import org.eobjects.analyzer.connection.CompositeDatastore;
import org.eobjects.analyzer.connection.CsvDatastore;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DbaseDatastore;
import org.eobjects.analyzer.connection.ExcelDatastore;
import org.eobjects.analyzer.connection.FileDatastore;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.connection.OdbDatastore;
import org.eobjects.analyzer.connection.XmlDatastore;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.panels.DCGlassPane;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.DCConfiguration;
import org.eobjects.datacleaner.user.DatastoreChangeListener;
import org.eobjects.datacleaner.user.MutableDatastoreCatalog;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.widgets.DCPopupBubble;
import org.jdesktop.swingx.VerticalLayout;

/**
 * Panel to select which datastore to use. Shown in the "source" tab, if no
 * datastore has been selected to begin with.
 * 
 * @author Kasper Sørensen
 */
public class SelectDatastorePanel extends DCPanel implements DatastoreChangeListener {

	private static final long serialVersionUID = 1L;

	private static final ImageManager imageManager = ImageManager.getInstance();
	private final MutableDatastoreCatalog _datastoreCatalog;
	private final AnalysisJobBuilderWindow _analysisJobBuilderWindow;
	private final List<JCheckBox> _checkBoxes = new ArrayList<JCheckBox>();
	private final List<String> _datastoreNames = new ArrayList<String>();
	private final DCGlassPane _glassPane;

	private final DCPanel _existingDatastoresPanel;

	public SelectDatastorePanel(AnalyzerBeansConfiguration configuration, AnalysisJobBuilderWindow analysisJobBuilderWindow,
			DCGlassPane glassPane) {
		super();
		_datastoreCatalog = (MutableDatastoreCatalog) configuration.getDatastoreCatalog();
		_analysisJobBuilderWindow = analysisJobBuilderWindow;
		_glassPane = glassPane;

		_datastoreCatalog.addListener(this);

		setLayout(new VerticalLayout(4));

		final DCLabel headerLabel = DCLabel.dark("Select datastore for analysis");
		headerLabel.setFont(WidgetUtils.FONT_HEADER);
		add(headerLabel);

		final DCLabel createNewDatastoreLabel = DCLabel.dark("Create a new datastore:");
		createNewDatastoreLabel.setFont(WidgetUtils.FONT_HEADER);

		final DCPanel newDatastorePanel = new DCPanel();
		newDatastorePanel.setLayout(new VerticalLayout(4));
		newDatastorePanel.setBorder(new EmptyBorder(10, 10, 10, 0));
		newDatastorePanel.add(createNewDatastoreLabel);
		newDatastorePanel.add(createNewDatastorePanel());

		add(newDatastorePanel);

		_existingDatastoresPanel = new DCPanel();
		_existingDatastoresPanel.setLayout(new VerticalLayout(4));
		_existingDatastoresPanel.setBorder(new EmptyBorder(10, 10, 10, 0));
		add(_existingDatastoresPanel);
		updateDatastores();

		JButton button = new JButton("Analyze!", imageManager.getImageIcon("images/filetypes/analysis_job.png"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int i = 0;
				for (JCheckBox c : _checkBoxes) {
					if (c.isSelected()) {
						String dsName = _datastoreNames.get(i);
						Datastore datastore = _datastoreCatalog.getDatastore(dsName);
						_analysisJobBuilderWindow.setDatastore(datastore);
						return;
					}
					i++;
				}
			}
		});
		DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		buttonPanel.add(button);

		add(buttonPanel);
	}

	private void updateDatastores() {
		_existingDatastoresPanel.removeAll();
		_datastoreNames.clear();
		_checkBoxes.clear();
		final DCLabel existingDatastoresLabel = DCLabel.dark("Analyze an existing datastore:");
		existingDatastoresLabel.setFont(WidgetUtils.FONT_HEADER);

		_existingDatastoresPanel.add(existingDatastoresLabel);

		String[] datastoreNames = _datastoreCatalog.getDatastoreNames();
		for (int i = 0; i < datastoreNames.length; i++) {
			final Datastore datastore = _datastoreCatalog.getDatastore(datastoreNames[i]);

			_existingDatastoresPanel.add(createDatastorePanel(datastore));
		}

		if (!_checkBoxes.isEmpty()) {
			_checkBoxes.get(0).doClick();
		}
	}

	private Component createDatastorePanel(Datastore datastore) {
		final Icon icon = IconUtils.getDatastoreIcon(datastore);
		final JCheckBox checkBox = new JCheckBox();
		checkBox.setOpaque(false);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (JCheckBox c : _checkBoxes) {
					if (checkBox == c) {
						c.setSelected(true);
					} else {
						c.setSelected(false);
					}
				}
			}
		});
		_checkBoxes.add(checkBox);
		String datastoreName = datastore.getName();
		_datastoreNames.add(datastoreName);
		final DCLabel datastoreNameLabel = DCLabel.dark("<html><b>" + datastoreName + "</b><br/>"
				+ getDescription(datastore) + "</html>");
		datastoreNameLabel.setIconTextGap(10);
		datastoreNameLabel.setIcon(icon);
		datastoreNameLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				checkBox.doClick();
			}
		});

		final JButton editButton = createEditButton(datastore);
		final JButton removeButton = createRemoveButton(datastore);

		final DCPanel panel = new DCPanel();
		panel.setBorder(WidgetUtils.BORDER_LIST_ITEM);

		WidgetUtils.addToGridBag(DCPanel.flow(checkBox, datastoreNameLabel), panel, 0, 0, GridBagConstraints.WEST, 1.0, 1.0);
		WidgetUtils.addToGridBag(editButton, panel, 1, 0, GridBagConstraints.EAST);
		WidgetUtils.addToGridBag(removeButton, panel, 2, 0, GridBagConstraints.EAST);
		return panel;
	}

	private JButton createRemoveButton(final Datastore datastore) {
		final String name = datastore.getName();
		final JButton removeButton = WidgetFactory.createSmallButton("images/actions/remove.png");
		removeButton.setToolTipText("Remove datastore");
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int result = JOptionPane.showConfirmDialog(SelectDatastorePanel.this,
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
					JdbcDatastoreDialog dialog = new JdbcDatastoreDialog((JdbcDatastore) datastore, _datastoreCatalog);
					dialog.setVisible(true);
				}
			});
		} else if (datastore instanceof CsvDatastore) {
			editButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					CsvDatastoreDialog dialog = new CsvDatastoreDialog((CsvDatastore) datastore, _datastoreCatalog);
					dialog.setVisible(true);
				}
			});
		} else if (datastore instanceof AccessDatastore) {
			editButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					AccessDatastoreDialog dialog = new AccessDatastoreDialog((AccessDatastore) datastore, _datastoreCatalog);
					dialog.setVisible(true);
				}
			});
		} else if (datastore instanceof ExcelDatastore) {
			editButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ExcelDatastoreDialog dialog = new ExcelDatastoreDialog((ExcelDatastore) datastore, _datastoreCatalog);
					dialog.setVisible(true);
				}
			});
		} else if (datastore instanceof XmlDatastore) {
			editButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					XmlDatastoreDialog dialog = new XmlDatastoreDialog((XmlDatastore) datastore, _datastoreCatalog);
					dialog.setVisible(true);
				}
			});
		} else if (datastore instanceof OdbDatastore) {
			editButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					OdbDatastoreDialog dialog = new OdbDatastoreDialog((OdbDatastore) datastore, _datastoreCatalog);
					dialog.setVisible(true);
				}
			});
		} else if (datastore instanceof DbaseDatastore) {
			editButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					DbaseDatastoreDialog dialog = new DbaseDatastoreDialog((DbaseDatastore) datastore, _datastoreCatalog);
					dialog.setVisible(true);
				}
			});
		} else if (datastore instanceof CompositeDatastore) {
			editButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					CompositeDatastoreDialog dialog = new CompositeDatastoreDialog((CompositeDatastore) datastore,
							_datastoreCatalog);
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

	private String getDescription(Datastore datastore) {
		if (datastore instanceof FileDatastore) {
			return ((FileDatastore) datastore).getFilename();
		} else if (datastore instanceof JdbcDatastore) {
			JdbcDatastore jdbcDatastore = (JdbcDatastore) datastore;
			String jdbcUrl = jdbcDatastore.getJdbcUrl();
			if ("jdbc:hsqldb:res:orderdb;readonly=true".equals(jdbcUrl)) {
				return "DataCleaner example database";
			}
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

	private DCPanel createNewDatastorePanel() {
		DCPanel panel = new DCPanel();
		panel.setBorder(WidgetUtils.BORDER_LIST_ITEM);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
		panel.add(createNewDatastoreButton("CSV file", "Comma-separated values (CSV) file (or file with other separators)",
				"images/datastore-types/csv.png", CsvDatastoreDialog.class));
		panel.add(createNewDatastoreButton("Excel spreadsheet",
				"Microsoft Excel spreadsheet. Either .xls (97-2003) or .xlsx (2007+) format.",
				"images/datastore-types/excel.png", ExcelDatastoreDialog.class));
		panel.add(createNewDatastoreButton("Access database", "Microsoft Access database file (.mdb).",
				"images/datastore-types/access.png", AccessDatastoreDialog.class));
		panel.add(createNewDatastoreButton("DBase database", "DBase database file (.dbf)",
				"images/datastore-types/dbase.png", DbaseDatastoreDialog.class));
		panel.add(createNewDatastoreButton("XML file", "Extensible Markup Language file (.xml)",
				"images/datastore-types/xml.png", XmlDatastoreDialog.class));
		panel.add(createNewDatastoreButton("OpenOffice.org Base database", "OpenOffice.org Base database file (.odb)",
				"images/datastore-types/odb.png", OdbDatastoreDialog.class));
		panel.add(Box.createHorizontalStrut(20));
		panel.add(createNewDatastoreButton("MySQL connection", "Connect to a MySQL database",
				"images/datastore-types/databases/mysql.png", JdbcDatastoreDialog.class));
		panel.add(createNewDatastoreButton("PostgreSQL connection", "Connect to a PostgreSQL database",
				"images/datastore-types/databases/postgresql.png", JdbcDatastoreDialog.class));
		panel.add(createNewDatastoreButton("Oracle connection", "Connect to a Oracle database",
				"images/datastore-types/databases/oracle.png", JdbcDatastoreDialog.class));
		panel.add(createNewDatastoreButton("Microsoft SQL Server connection", "Connect to a Microsoft SQL Server database",
				"images/datastore-types/databases/microsoft.png", JdbcDatastoreDialog.class));

		final JButton moreDatastoreTypesButton = new JButton("more");
		moreDatastoreTypesButton.setMargin(new Insets(0, 0, 0, 0));
		panel.add(moreDatastoreTypesButton);

		return panel;
	}

	private JButton createNewDatastoreButton(final String title, final String description, final String imagePath,
			final Class<? extends AbstractDialog> dialogClass) {
		ImageIcon icon = imageManager.getImageIcon(imagePath);
		final JButton button = new JButton(icon);
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setBorder(null);
		button.setOpaque(false);

		final DCPopupBubble popupBubble = new DCPopupBubble(_glassPane, "<html><b>" + title + "</b><br/>" + description
				+ "</html>", 0, 0, imagePath);
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				Point locationOnScreen = button.getLocationOnScreen();
				popupBubble.setLocationOnScreen(locationOnScreen.x + 15, locationOnScreen.y + button.getHeight());
				popupBubble.show();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				popupBubble.hide();
			}
		});
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					Object[] constructorArgs = null;
					Constructor<? extends AbstractDialog> constructor = null;
					try {
						constructor = dialogClass.getConstructor(new Class[0]);
					} catch (NoSuchMethodException e) {
						constructor = dialogClass.getConstructor(new Class[] { MutableDatastoreCatalog.class });
						constructorArgs = new Object[] { DCConfiguration.get().getDatastoreCatalog() };
					}
					AbstractDialog dialog = constructor.newInstance(constructorArgs);
					dialog.setVisible(true);
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
			}
		});
		return button;
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		_datastoreCatalog.removeListener(this);
	}

	@Override
	public void onAdd(Datastore datastore) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateDatastores();
			}
		});
	}

	@Override
	public void onRemove(Datastore datastore) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateDatastores();
			}
		});
	}
}
