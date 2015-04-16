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
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.inject.Inject;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import org.datacleaner.connection.Datastore;
import org.datacleaner.guice.InjectorBuilder;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.LoadingIcon;
import org.datacleaner.widgets.tree.SchemaTree;

import com.google.inject.Injector;

public class SchemaTreePanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_SEARCH_FIELD_TEXT = "Search component library...";

    private final InjectorBuilder _injectorBuilder;
    private JComponent _updatePanel;
    private final JTextField _searchTextField;

    @Inject
    protected SchemaTreePanel(InjectorBuilder injectorBuilder) {
        super(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        _injectorBuilder = injectorBuilder;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(4, 4, 4, 4));
        setDatastore(null, false);
        _searchTextField = new JTextField(DEFAULT_SEARCH_FIELD_TEXT);
    }

    public void setDatastore(final Datastore datastore, final boolean expandTree) {
        removeAll();
        if (datastore == null) {
            add(new DCPanel().setPreferredSize(150, 150), BorderLayout.CENTER);
            return;
        }

        add(new LoadingIcon().setPreferredSize(150, 150), BorderLayout.CENTER);

        // load the schema tree in the background because it will retrieve
        // metadata about the datastore (might take several seconds)
        new SwingWorker<SchemaTree, Void>() {
            @Override
            protected SchemaTree doInBackground() throws Exception {
                Injector injector = _injectorBuilder.with(Datastore.class, datastore).createInjector();
                SchemaTree tree = injector.getInstance(SchemaTree.class);
                return tree;
            }

            protected void done() {
                try {
                    final SchemaTree schemaTree = get();
                    final JScrollPane schemaTreeScroll = WidgetUtils.scrolleable(schemaTree);
                    schemaTreeScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                    schemaTree.addComponentListener(new ComponentAdapter() {
                        @Override
                        public void componentResized(ComponentEvent e) {
                            updateParentPanel();
                        }

                    });
                    removeAll();
                    add(schemaTreeScroll, BorderLayout.CENTER);
                    _searchTextField.addKeyListener(new KeyListener() {

                        @Override
                        public void keyTyped(KeyEvent e) {
                        }

                        @Override
                        public void keyReleased(KeyEvent e) {
                            JTextField source = (JTextField) e.getSource();
                            schemaTree.filter(source.getText());
                        }

                        @Override
                        public void keyPressed(KeyEvent e) {
                        }
                    });
                    _searchTextField.addFocusListener(new FocusListener() {

                        @Override
                        public void focusLost(FocusEvent e) {
                            JTextField source = (JTextField) e.getSource();
                            if (source.getText().equals("")) {
                                source.setText(DEFAULT_SEARCH_FIELD_TEXT);
                            }

                        }

                        @Override
                        public void focusGained(FocusEvent e) {
                            JTextField source = (JTextField) e.getSource();
                            if (source.getText().equals(DEFAULT_SEARCH_FIELD_TEXT)) {
                                source.setText("");
                            }
                        }
                    });
                    DCPanel searchPanel = new DCPanel(Color.WHITE);
                    FlowLayout layout = (FlowLayout) searchPanel.getLayout();
                    layout.setVgap(0);
                    layout.setHgap(0);
                    JLabel resetSearchFieldIcon = new JLabel("X");
                    resetSearchFieldIcon.setToolTipText("Reset search field...");
                    resetSearchFieldIcon.setBorder(WidgetUtils.BORDER_EMPTY);
                    resetSearchFieldIcon.addMouseListener(new MouseListener() {

                        @Override
                        public void mouseReleased(MouseEvent e) {
                            _searchTextField.setText(DEFAULT_SEARCH_FIELD_TEXT);
                            schemaTree.filter("");
                        }

                        @Override
                        public void mousePressed(MouseEvent e) {
                        }

                        @Override
                        public void mouseExited(MouseEvent e) {
                        }

                        @Override
                        public void mouseEntered(MouseEvent e) {
                        }

                        @Override
                        public void mouseClicked(MouseEvent e) {
                        }
                    });
                    _searchTextField.setBorder(WidgetUtils.BORDER_EMPTY);
                    searchPanel.add(_searchTextField);
                    searchPanel.add(resetSearchFieldIcon);

                    add(searchPanel, BorderLayout.SOUTH);
                    updateParentPanel();
                    schemaTree.expandStandardPaths();
                    if (expandTree) {
                        schemaTree.expandSelectedData();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };

        }.execute();
    }

    private void updateParentPanel() {
        if (_updatePanel != null) {
            _updatePanel.updateUI();
        }
    }

    public void setUpdatePanel(JComponent updatePanel) {
        _updatePanel = updatePanel;
    }
}
