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

import org.apache.metamodel.schema.Table;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.AccessDatastore;
import org.datacleaner.connection.CassandraDatastore;
import org.datacleaner.connection.CompositeDatastore;
import org.datacleaner.connection.CouchDbDatastore;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.connection.DbaseDatastore;
import org.datacleaner.connection.ElasticSearchDatastore;
import org.datacleaner.connection.ExcelDatastore;
import org.datacleaner.connection.FileDatastore;
import org.datacleaner.connection.FixedWidthDatastore;
import org.datacleaner.connection.HBaseDatastore;
import org.datacleaner.connection.JdbcDatastore;
import org.datacleaner.connection.MongoDbDatastore;
import org.datacleaner.connection.OdbDatastore;
import org.datacleaner.connection.PojoDatastore;
import org.datacleaner.connection.SalesforceDatastore;
import org.datacleaner.connection.SasDatastore;
import org.datacleaner.connection.SugarCrmDatastore;
import org.datacleaner.connection.XmlDatastore;
import org.datacleaner.guice.DCModule;
import org.datacleaner.guice.InjectorBuilder;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.windows.AccessDatastoreDialog;
import org.datacleaner.windows.CassandraDatastoreDialog;
import org.datacleaner.windows.CompositeDatastoreDialog;
import org.datacleaner.windows.CouchDbDatastoreDialog;
import org.datacleaner.windows.CsvDatastoreDialog;
import org.datacleaner.windows.DbaseDatastoreDialog;
import org.datacleaner.windows.ElasticSearchDatastoreDialog;
import org.datacleaner.windows.ExcelDatastoreDialog;
import org.datacleaner.windows.FixedWidthDatastoreDialog;
import org.datacleaner.windows.HBaseDatastoreDialog;
import org.datacleaner.windows.JdbcDatastoreDialog;
import org.datacleaner.windows.MongoDbDatastoreDialog;
import org.datacleaner.windows.OdbDatastoreDialog;
import org.datacleaner.windows.QueryWindow;
import org.datacleaner.windows.SalesforceDatastoreDialog;
import org.datacleaner.windows.SasDatastoreDialog;
import org.datacleaner.windows.SugarCrmDatastoreDialog;
import org.datacleaner.windows.XmlDatastoreDialog;

import com.google.inject.Injector;

/**
 * A panel that presents a datastore and shows edit/remove buttons. This panel
 * is placed as a child inside the {@link DatastoreManagementPanel}.
 */
public class DatastorePanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    public static final int LABEL_MAX_WIDTH = 450;

    private final Datastore _datastore;
    private final MutableDatastoreCatalog _datastoreCatalog;
    private final DatastoreManagementPanel _datastoreListPanel;
    private final JCheckBox _checkBox;
    private final WindowContext _windowContext;
    private final UserPreferences _userPreferences;
    private final DCModule _dcModule;

    public DatastorePanel(Datastore datastore, MutableDatastoreCatalog datastoreCatalog,
            DatastoreManagementPanel datastoreListPanel, WindowContext windowContext, UserPreferences userPreferences,
            DCModule dcModule) {
        super(WidgetUtils.BG_COLOR_BRIGHT);
        _datastore = datastore;
        _datastoreCatalog = datastoreCatalog;
        _datastoreListPanel = datastoreListPanel;
        _windowContext = windowContext;
        _userPreferences = userPreferences;
        _dcModule = dcModule;

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

        final String datastoreName = datastore.getName();
        final DCLabel datastoreNameLabel = DCLabel.dark("<html><b>" + datastoreName + "</b><br/>" + description
                + "</html>");
        datastoreNameLabel.setIconTextGap(10);
        datastoreNameLabel.setIcon(icon);
        datastoreNameLabel.setMaximumWidth(LABEL_MAX_WIDTH);
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
        final JButton queryButton = createQueryButton(datastore);
        final JButton removeButton = createRemoveButton(datastore);

        setBorder(WidgetUtils.BORDER_LIST_ITEM_SUBTLE);

        WidgetUtils.addToGridBag(_checkBox, this, 0, 0, GridBagConstraints.WEST);
        WidgetUtils.addToGridBag(DCPanel.flow(datastoreNameLabel), this, 1, 0, GridBagConstraints.WEST, 1.0, 1.0);
        WidgetUtils.addToGridBag(editButton, this, 2, 0, GridBagConstraints.EAST);
        WidgetUtils.addToGridBag(queryButton, this, 3, 0, GridBagConstraints.EAST);
        WidgetUtils.addToGridBag(removeButton, this, 4, 0, GridBagConstraints.EAST);
    }

    public Datastore getDatastore() {
        return _datastore;
    }

    private JButton createRemoveButton(final Datastore datastore) {
        final String name = datastore.getName();
        final JButton removeButton = WidgetFactory.createDefaultButton("Remove", IconUtils.ACTION_REMOVE);
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
        return removeButton;
    }

    private JButton createQueryButton(final Datastore datastore) {
        final JButton queryButton = WidgetFactory.createDefaultButton("Query", IconUtils.MODEL_QUERY);
        queryButton.setToolTipText("Query datastore");
        queryButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final String queryString;
                final DatastoreConnection connection = datastore.openConnection();
                try {
                    Table[] tables = connection.getSchemaNavigator().getDefaultSchema().getTables();
                    if (tables.length > 0) {
                        queryString = "SELECT *\nFROM " + tables[0].getQualifiedLabel();
                    } else {
                        queryString = "SELECT *\nFROM ?";
                    }
                } finally {
                    connection.close();
                }
                QueryWindow queryWindow = new QueryWindow(_windowContext, datastore, queryString);
                queryWindow.open();
            }
        });

        return queryButton;
    }

    private JButton createEditButton(final Datastore datastore) {
        final JButton editButton = WidgetFactory.createDefaultButton("Edit", IconUtils.ACTION_EDIT);
        editButton.setToolTipText("Edit datastore");

        if (datastore instanceof JdbcDatastore) {
            editButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Injector injectorWithDatastore = getInjectorBuilder().with(JdbcDatastore.class, datastore)
                            .createInjector();
                    JdbcDatastoreDialog dialog = injectorWithDatastore.getInstance(JdbcDatastoreDialog.class);
                    dialog.open();
                }
            });
        } else if (datastore instanceof CsvDatastore) {
            editButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Injector injector = getInjectorBuilder().with(CsvDatastore.class, datastore).createInjector();
                    CsvDatastoreDialog dialog = injector.getInstance(CsvDatastoreDialog.class);
                    dialog.open();
                }
            });
        } else if (datastore instanceof AccessDatastore) {
            editButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Injector injector = getInjectorBuilder().with(AccessDatastore.class, datastore).createInjector();
                    AccessDatastoreDialog dialog = injector.getInstance(AccessDatastoreDialog.class);
                    dialog.open();
                }
            });
        } else if (datastore instanceof ExcelDatastore) {
            editButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Injector injector = getInjectorBuilder().with(ExcelDatastore.class, datastore).createInjector();
                    ExcelDatastoreDialog dialog = injector.getInstance(ExcelDatastoreDialog.class);
                    dialog.open();
                }
            });
        } else if (datastore instanceof SasDatastore) {
            editButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Injector injector = getInjectorBuilder().with(SasDatastore.class, datastore).createInjector();
                    SasDatastoreDialog dialog = injector.getInstance(SasDatastoreDialog.class);
                    dialog.open();
                }
            });
        } else if (datastore instanceof XmlDatastore) {
            editButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Injector injector = getInjectorBuilder().with(XmlDatastore.class, datastore).createInjector();
                    XmlDatastoreDialog dialog = injector.getInstance(XmlDatastoreDialog.class);
                    dialog.open();
                }
            });
        } else if (datastore instanceof OdbDatastore) {
            editButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Injector injector = getInjectorBuilder().with(OdbDatastore.class, datastore).createInjector();
                    OdbDatastoreDialog dialog = injector.getInstance(OdbDatastoreDialog.class);
                    dialog.open();
                }
            });
        } else if (datastore instanceof FixedWidthDatastore) {
            editButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Injector injector = getInjectorBuilder().with(FixedWidthDatastore.class, datastore)
                            .createInjector();
                    FixedWidthDatastoreDialog dialog = injector.getInstance(FixedWidthDatastoreDialog.class);
                    dialog.open();
                }
            });
        } else if (datastore instanceof DbaseDatastore) {
            editButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Injector injector = getInjectorBuilder().with(DbaseDatastore.class, datastore).createInjector();
                    DbaseDatastoreDialog dialog = injector.getInstance(DbaseDatastoreDialog.class);
                    dialog.open();
                }
            });
        } else if (datastore instanceof HBaseDatastore) {
            editButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Injector injector = getInjectorBuilder().with(HBaseDatastore.class, datastore).createInjector();
                    HBaseDatastoreDialog dialog = injector.getInstance(HBaseDatastoreDialog.class);
                    dialog.open();
                }
            });
        } else if (datastore instanceof CassandraDatastore) {
            editButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Injector injector = getInjectorBuilder().with(CassandraDatastore.class, datastore).createInjector();
                    CassandraDatastoreDialog dialog = injector.getInstance(CassandraDatastoreDialog.class);
                    dialog.open();
                }
            });
        } else if (datastore instanceof ElasticSearchDatastore) {
            editButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Injector injector = getInjectorBuilder().with(ElasticSearchDatastore.class, datastore)
                            .createInjector();
                    ElasticSearchDatastoreDialog dialog = injector.getInstance(ElasticSearchDatastoreDialog.class);
                    dialog.open();
                }
            });
        } else if (datastore instanceof CouchDbDatastore) {
            editButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Injector injector = getInjectorBuilder().with(CouchDbDatastore.class, datastore).createInjector();
                    CouchDbDatastoreDialog dialog = injector.getInstance(CouchDbDatastoreDialog.class);
                    dialog.open();
                }
            });
        } else if (datastore instanceof MongoDbDatastore) {
            editButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Injector injector = getInjectorBuilder().with(MongoDbDatastore.class, datastore).createInjector();
                    MongoDbDatastoreDialog dialog = injector.getInstance(MongoDbDatastoreDialog.class);
                    dialog.open();
                }
            });
        } else if (datastore instanceof SalesforceDatastore) {
            editButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Injector injector = getInjectorBuilder().with(SalesforceDatastore.class, datastore)
                            .createInjector();
                    SalesforceDatastoreDialog dialog = injector.getInstance(SalesforceDatastoreDialog.class);
                    dialog.open();
                }
            });
        } else if (datastore instanceof SugarCrmDatastore) {
            editButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Injector injector = getInjectorBuilder().with(SugarCrmDatastore.class, datastore).createInjector();
                    SugarCrmDatastoreDialog dialog = injector.getInstance(SugarCrmDatastoreDialog.class);
                    dialog.open();
                }
            });
        } else if (datastore instanceof CompositeDatastore) {
            editButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    CompositeDatastoreDialog dialog = new CompositeDatastoreDialog((CompositeDatastore) datastore,
                            _datastoreCatalog, _windowContext, _userPreferences);
                    dialog.open();
                }
            });
        } else {
            editButton.setEnabled(false);
        }

        return editButton;
    }

    public static String getDescription(Datastore datastore) {
        if (datastore.getDescription() != null) {
            return datastore.getDescription();
        }
        if (datastore instanceof FileDatastore) {
            return ((FileDatastore) datastore).getFilename();
        } else if (datastore instanceof JdbcDatastore) {
            final JdbcDatastore jdbcDatastore = (JdbcDatastore) datastore;
            final String jdbcUrl = jdbcDatastore.getJdbcUrl();
            final String datasourceJndiUrl = jdbcDatastore.getDatasourceJndiUrl();
            if (StringUtils.isNullOrEmpty(datasourceJndiUrl)) {
                return jdbcUrl;
            }
            return datasourceJndiUrl;
        } else if (datastore instanceof ElasticSearchDatastore) {
            final ElasticSearchDatastore elasticSearchDatastore = (ElasticSearchDatastore) datastore;
            return elasticSearchDatastore.getClusterName() + " - " + elasticSearchDatastore.getIndexName();
        } else if (datastore instanceof CassandraDatastore) {
            final CassandraDatastore cassandraDatastore = (CassandraDatastore) datastore;
            return cassandraDatastore.getKeyspace();
        } else if (datastore instanceof MongoDbDatastore) {
            final MongoDbDatastore mongoDbDatastore = (MongoDbDatastore) datastore;
            return mongoDbDatastore.getHostname() + ":" + mongoDbDatastore.getPort() + " - "
                    + mongoDbDatastore.getDatabaseName();
        } else if (datastore instanceof CouchDbDatastore) {
            final CouchDbDatastore couchDbDatastore = (CouchDbDatastore) datastore;
            return (couchDbDatastore.isSslEnabled() ? "https://" : "http://") + couchDbDatastore.getHostname() + ":"
                    + couchDbDatastore.getPort();
        } else if (datastore instanceof PojoDatastore) {
            return "In-memory collection of records.";
        } else if (datastore instanceof CompositeDatastore) {
            final List<? extends Datastore> datastores = ((CompositeDatastore) datastore).getDatastores();
            final StringBuilder sb = new StringBuilder();
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

    private InjectorBuilder getInjectorBuilder() {
        return _dcModule.createInjectorBuilder();
    }

    public boolean isSelected() {
        return _checkBox.isSelected();
    }

    public void setSelected(boolean selected) {
        _checkBox.setSelected(selected);
    }
}
