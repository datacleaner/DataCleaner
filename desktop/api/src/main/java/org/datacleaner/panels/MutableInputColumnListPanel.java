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
import java.awt.GridBagLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.Closeable;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComponent;

import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.OutputColumnVisibilityButton;
import org.jdesktop.swingx.JXTextField;

/**
 * Panel containing text box and other presentation of mutable input columns.
 */
public class MutableInputColumnListPanel extends DCPanel implements MutableInputColumn.Listener, Closeable {

    private static final long serialVersionUID = 1L;

    private final MutableInputColumn<?> _inputColumn;
    private final JXTextField _textField;
    private final AnalysisJobBuilder _analysisJobBuilder;
    private final OutputColumnVisibilityButton _visibilityButton;
    private final JComponent panelOwner;

    /**
     *
     * @param analysisJobBuilder
     * @param inputColumn
     * @param panelOwner - the component that logically owns this component. Will be repainted after
     *                   this component changes a content. Since this component is used as a cell
     *                   renderer in a table, it is not really part of the physical Swing component
     *                   hierarchy (the table doesn't have it as a Component child). So changing our
     *                   content doesn't automatically revalidate and repaint the table.
     *                   Must be called explicitly.
     */
    public MutableInputColumnListPanel(final AnalysisJobBuilder analysisJobBuilder,
            final MutableInputColumn<?> inputColumn, final JComponent panelOwner) {
        _analysisJobBuilder = analysisJobBuilder;
        _inputColumn = inputColumn;

        setLayout(new GridBagLayout());

        _textField = WidgetFactory.createTextField("Column name");
        _textField.setText(inputColumn.getName());

        _textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(final FocusEvent e) {
                if (!_inputColumn.getName().equals(_textField.getText())) {
                    _inputColumn.setName(_textField.getText());

                    final TransformerComponentBuilder<?> tjb =
                            _analysisJobBuilder.getOriginatingTransformer(_inputColumn);
                    if (tjb != null) {
                        tjb.onOutputChanged();
                    }
                }
            }
        });

        _inputColumn.addListener(this);

        _visibilityButton = new OutputColumnVisibilityButton(inputColumn);

        final JButton resetButton = WidgetFactory.createSmallButton(IconUtils.ACTION_RESET);
        resetButton.setToolTipText("Reset output column name");
        resetButton.addActionListener(e -> _textField.setText(_inputColumn.getInitialName()));

        WidgetUtils.addToGridBag(_visibilityButton, this, 0, 0, GridBagConstraints.WEST, 0.0, 0.0);
        WidgetUtils.addToGridBag(_textField, this, 1, 0, GridBagConstraints.WEST, 1.0, 1.0);
        WidgetUtils.addToGridBag(resetButton, this, 2, 0, GridBagConstraints.WEST, 0.0, 0.0);

        this.panelOwner = panelOwner;
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
    }

    @Override
    public void onNameChanged(final MutableInputColumn<?> column, final String oldName, final String newName) {
        _textField.setText(newName);
        if (panelOwner != null) {
            panelOwner.repaint();
        }
    }

    @Override
    public void onVisibilityChanged(final MutableInputColumn<?> column, final boolean hidden) {
        // do nothing (the visibility button is also a listener itself)
        _visibilityButton.setSelected(!hidden);
    }

    @Override
    public void close() throws IOException {
        _inputColumn.removeListener(this);
    }

}
