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
package org.datacleaner.panels.result;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.text.NumberFormat;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import org.apache.metamodel.schema.Table;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.DCProgressBar;

/**
 * A panel that shows progress information about the processing of a table
 */
public class TableProgressInformationPanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    private static final Font FONT = WidgetUtils.FONT_SMALL;

    private final Table _table;
    private final DCProgressBar _progressBar;
    private final DCLabel _progressCountLabel;
    private final DCLabel _progressStatusLabel;
    private final DCLabel _progressExpectationLabel;

    public TableProgressInformationPanel(Table table, int expectedRows) {
        _table = table;
        _progressBar = new DCProgressBar(0, expectedRows);
        _progressStatusLabel = DCLabel.bright("");
        _progressCountLabel = DCLabel.bright("0");
        _progressExpectationLabel = DCLabel.bright(" of approx. " + formatNumber(expectedRows) + " rows");

        _progressStatusLabel.setFont(FONT);
        _progressCountLabel.setFont(FONT);
        _progressExpectationLabel.setFont(FONT);

        GridBagLayout layout = new GridBagLayout();
        layout.columnWidths = new int[] { 150 };
        setLayout(layout);

        setBorder(new EmptyBorder(4, 4, 4, 4));

        final DCPanel rightSidePanel = new DCPanel();
        rightSidePanel.setLayout(new BorderLayout());
        rightSidePanel.add(Box.createVerticalStrut(8), BorderLayout.NORTH);
        rightSidePanel.add(_progressBar, BorderLayout.CENTER);
        rightSidePanel.add(createRecordCountPanel(), BorderLayout.SOUTH);

        WidgetUtils.addToGridBag(createTableLabel(), this, 0, 0);
        WidgetUtils.addToGridBag(rightSidePanel, this, 1, 0, 1.0, 0.0);
    }

    private Component createRecordCountPanel() {
        final DCPanel panel = new DCPanel();
        panel.setLayout(new FlowLayout(Alignment.RIGHT.getFlowLayoutAlignment()));

        panel.add(_progressStatusLabel);
        panel.add(_progressCountLabel);
        panel.add(_progressExpectationLabel);

        return panel;
    }

    private Component createTableLabel() {
        final Icon icon = ImageManager.get().getImageIcon(IconUtils.MODEL_TABLE);
        final JLabel iconLabel = new JLabel(icon);
        iconLabel.setHorizontalAlignment(Alignment.CENTER.getLabelAlignment());

        final DCLabel tableNameLabel = DCLabel.bright(_table.getName());
        tableNameLabel.setFont(FONT);
        tableNameLabel.setHorizontalAlignment(Alignment.CENTER.getLabelAlignment());

        final DCPanel panel = new DCPanel();
        panel.setLayout(new BorderLayout());
        panel.add(iconLabel, BorderLayout.CENTER);
        panel.add(tableNameLabel, BorderLayout.SOUTH);
        panel.setMaximumSize(new Dimension(150, 150));
        panel.setMinimumSize(new Dimension(150, 10));
        return panel;
    }

    public Table getTable() {
        return _table;
    }

    /**
     * Sets the progress of the processing of the table.
     * 
     * @param currentRow
     * @return
     */
    public boolean setProgress(int currentRow) {
        boolean result = _progressBar.setValueIfGreater(currentRow);

        if (result) {
            _progressCountLabel.setText(formatNumber(currentRow));
        }

        return result;
    }

    public void setProgressStopped(boolean failure) {
        _progressStatusLabel.setText("Stopped! - ");
        _progressBar.setEnabled(false);
        if (failure) {
            _progressBar.setProgressBarColor(WidgetUtils.ADDITIONAL_COLOR_RED_BRIGHT);
        }
    }

    public void setProgressFinished() {
        int maximum = _progressBar.getValue();
        setProgressFinished(maximum);
    }

    public void setProgressFinished(int finalNumberOfRows) {
        _progressStatusLabel.setText("Finished! - ");
        _progressBar.setMaximum(finalNumberOfRows);
        _progressExpectationLabel.setText(" of  " + formatNumber(finalNumberOfRows) + " rows");
    }

    public void setProgressCancelled() {
        _progressStatusLabel.setText("Cancelled! - ");
    }

    private String formatNumber(int number) {
        NumberFormat nf = NumberFormat.getInstance();
        return nf.format(number);
    }
}
