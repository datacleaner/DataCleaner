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
import java.awt.Cursor;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.inject.Inject;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;

import org.datacleaner.connection.Datastore;
import org.datacleaner.guice.InjectorBuilder;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.LoadingIcon;
import org.datacleaner.widgets.tree.SchemaTree;
import org.jdesktop.swingx.JXTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.inject.Injector;

/**
 * Panel that wraps the {@link SchemaTree} as well as actions around it for
 * searching/filtering etc.
 */
public class SchemaTreePanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(SchemaTreePanel.class);

    private static final String DEFAULT_SEARCH_FIELD_TEXT = "Search component library...";

    private final InjectorBuilder _injectorBuilder;
    private final JXTextField _searchTextField;
    private final JComponent _resetSearchButton;
    private JComponent _updatePanel;
    private SchemaTree _schemaTree;

    @Inject
    protected SchemaTreePanel(InjectorBuilder injectorBuilder) {
        super(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        _injectorBuilder = injectorBuilder;
        _searchTextField = createSearchTextField();
        _resetSearchButton = createResetSearchButton();

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(4, 4, 4, 4));
        setDatastore(null, false);

        setFocusable(true);
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
                    _schemaTree = get();
                    final JScrollPane schemaTreeScroll = WidgetUtils.scrolleable(_schemaTree);
                    schemaTreeScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                    _schemaTree.addComponentListener(new ComponentAdapter() {
                        @Override
                        public void componentResized(ComponentEvent e) {
                            updateParentPanel();
                        }
                    });
                    _schemaTree.setFocusable(true);
                    _schemaTree.addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyPressed(KeyEvent e) {
                            if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
                                _searchTextField.setText("");
                                _searchTextField.requestFocusInWindow();
                            } else if (!e.isActionKey() && !_searchTextField.isFocusOwner()) {
                                final char keyChar = e.getKeyChar();
                                if (Character.isLetter(keyChar)) {
                                    _searchTextField.setText("");
                                    final Document document = _searchTextField.getDocument();
                                    try {
                                        document.insertString(document.getLength(), "" + keyChar,
                                                SimpleAttributeSet.EMPTY);
                                    } catch (BadLocationException ex) {
                                        logger.debug("Document.insertString({}) failed", keyChar, ex);
                                    }
                                    _searchTextField.requestFocusInWindow();
                                }
                            }
                        }
                    });
                    removeAll();
                    add(schemaTreeScroll, BorderLayout.CENTER);

                    final DCPanel searchComponent = new DCPanel(WidgetUtils.BG_COLOR_BRIGHTEST);
                    searchComponent.setLayout(new BorderLayout());
                    searchComponent.add(_searchTextField, BorderLayout.CENTER);
                    searchComponent.add(_resetSearchButton, BorderLayout.EAST);

                    add(searchComponent, BorderLayout.SOUTH);
                    _schemaTree.expandStandardPaths();
                    if (expandTree) {
                        _schemaTree.expandSelectedData();
                    }
                    updateParentPanel();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };

        }.execute();
    }

    private JComponent createResetSearchButton() {
        final JLabel resetSearchFieldIcon = new JLabel("X");
        resetSearchFieldIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        resetSearchFieldIcon.setBorder(WidgetUtils.BORDER_EMPTY);
        resetSearchFieldIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                final String currentText = _searchTextField.getText();
                if (Strings.isNullOrEmpty(currentText)) {
                    // do nothing
                    return;
                }
                _searchTextField.setText("");
                _schemaTree.filter("");
            }
        });
        return resetSearchFieldIcon;
    }

    protected JXTextField createSearchTextField() {
        final JXTextField searchTextField = new JXTextField(DEFAULT_SEARCH_FIELD_TEXT);
        searchTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                final int length = searchTextField.getText().length();
                searchTextField.select(length, length);
            }
        });
        searchTextField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                _schemaTree.filter(searchTextField.getText());
            }
        });
        searchTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
                    searchTextField.setText("");
                }
            }
        });
        searchTextField.setBorder(WidgetUtils.BORDER_EMPTY);
        return searchTextField;
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
