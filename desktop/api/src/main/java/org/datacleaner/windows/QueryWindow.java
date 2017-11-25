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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.MetaModelException;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.DataSetTableModel;
import org.apache.metamodel.query.Query;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.NumberDocument;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.table.DCTable;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.jdesktop.swingx.JXTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Window that presents an ad-hoc query editor panel and a result of the query
 */
public class QueryWindow extends AbstractWindow {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(QueryWindow.class);

    private final Datastore _datastore;
    private final RSyntaxTextArea _queryTextArea;
    private final DCPanel _upperPanel;
    private final DCPanel _centerPanel;
    private final JButton _queryButton;
    private final DCTable _table;
    private final JXTextField _limitTextField;

    public QueryWindow(final WindowContext windowContext, final Datastore datastore, final String query) {
        super(windowContext);
        _datastore = datastore;
        _queryTextArea = new RSyntaxTextArea(5, 17);
        _queryTextArea.setFont(WidgetUtils.FONT_MONOSPACE);
        _queryTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        _queryTextArea.setText(query);

        _limitTextField = WidgetFactory.createTextField(null, 3);
        _limitTextField.setDocument(new NumberDocument(false, false));
        _limitTextField.setText("500");

        _table = new DCTable();
        _queryButton = WidgetFactory.createPrimaryButton("Execute query", IconUtils.MODEL_QUERY);
        _queryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                final String queryString = _queryTextArea.getText();
                logger.debug("Query being parsed: {}", queryString);

                try (DatastoreConnection con = _datastore.openConnection()) {
                    final DataContext dataContext = con.getDataContext();
                    final Query q = dataContext.parseQuery(queryString);
                    logger.info("Parsed query: {}", q);
                    final String limitString = _limitTextField.getText();
                    if (!StringUtils.isNullOrEmpty(limitString)) {
                        final int limit = Integer.parseInt(limitString);
                        q.setMaxRows(limit);
                    }
                    final DataSet dataSet = dataContext.executeQuery(q);
                    _centerPanel.setVisible(true);
                    _table.setModel(new DataSetTableModel(dataSet));
                } catch (final MetaModelException e) {
                    WidgetUtils.showErrorMessage("Failed to execute query", e.getMessage(), e);
                }
            }
        });

        _centerPanel = _table.toPanel();
        _centerPanel.setVisible(false);

        final DCPanel decoratedLimitTextField = WidgetUtils.decorateWithShadow(_limitTextField, false, 0);

        final DCPanel buttonPanel = new DCPanel();
        WidgetUtils.addToGridBag(DCLabel.dark("Max rows:"), buttonPanel, 1, 1, GridBagConstraints.CENTER);
        WidgetUtils.addToGridBag(decoratedLimitTextField, buttonPanel, 2, 1, GridBagConstraints.CENTER);
        WidgetUtils.addToGridBag(_queryButton, buttonPanel, 1, 2, 2, 1);

        final JScrollPane scrolledTextArea = new JScrollPane(_queryTextArea);
        final DCPanel decoratedTextField = WidgetUtils.decorateWithShadow(scrolledTextArea);

        _upperPanel = new DCPanel();
        _upperPanel.setLayout(new BorderLayout());
        _upperPanel.add(decoratedTextField, BorderLayout.CENTER);
        _upperPanel.add(buttonPanel, BorderLayout.EAST);
    }

    @Override
    public String getWindowTitle() {
        return "Query " + _datastore.getName();
    }

    @Override
    public Image getWindowIcon() {
        return ImageManager.get().getImage("images/model/query.png");
    }

    @Override
    protected boolean isWindowResizable() {
        return true;
    }

    @Override
    protected boolean isCentered() {
        return true;
    }

    @Override
    protected JComponent getWindowContent() {
        final DCPanel outerPanel = new DCPanel();
        outerPanel.setLayout(new BorderLayout());

        outerPanel.add(_upperPanel, BorderLayout.NORTH);
        outerPanel.add(_centerPanel, BorderLayout.CENTER);

        outerPanel.setPreferredSize(new Dimension(900, 400));

        return outerPanel;
    }
}
