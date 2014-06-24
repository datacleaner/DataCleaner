/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.NumberDocument;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.widgets.table.DCTable;
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.MetaModelException;
import org.eobjects.metamodel.data.DataSet;
import org.eobjects.metamodel.data.DataSetTableModel;
import org.eobjects.metamodel.query.Query;
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

    public QueryWindow(WindowContext windowContext, Datastore datastore, String query) {
        super(windowContext);
        _datastore = datastore;
        _queryTextArea = new RSyntaxTextArea(5, 17);
        _queryTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        _queryTextArea.setText(query);

        _limitTextField = WidgetFactory.createTextField(null, 3);
        _limitTextField.setDocument(new NumberDocument(false, false));
        _limitTextField.setText("500");

        _table = new DCTable();
        _queryButton = new JButton("Execute query");
        _queryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                final String queryString = _queryTextArea.getText();
                logger.debug("Query being parsed: {}", queryString);

                final DatastoreConnection con = _datastore.openConnection();
                try {
                    final DataContext dataContext = con.getDataContext();
                    final Query q = dataContext.parseQuery(queryString);
                    logger.info("Parsed query: {}", q);
                    final String limitString = _limitTextField.getText();
                    if (!StringUtils.isNullOrEmpty(limitString)) {
                        int limit = Integer.parseInt(limitString);
                        q.setMaxRows(limit);
                    }
                    final DataSet dataSet = dataContext.executeQuery(q);
                    _centerPanel.setVisible(true);
                    _table.setModel(new DataSetTableModel(dataSet));
                } catch (MetaModelException e) {
                    WidgetUtils.showErrorMessage("Failed to execute query", e.getMessage(), e);
                } finally {
                    con.close();
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
        final DCPanel decoratedTextField = WidgetUtils.decorateWithShadow(scrolledTextArea, true, 2);

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
        DCPanel outerPanel = new DCPanel();
        outerPanel.setLayout(new BorderLayout());

        outerPanel.add(_upperPanel, BorderLayout.NORTH);
        outerPanel.add(_centerPanel, BorderLayout.CENTER);

        outerPanel.setPreferredSize(new Dimension(600, 400));

        return outerPanel;
    }

}
