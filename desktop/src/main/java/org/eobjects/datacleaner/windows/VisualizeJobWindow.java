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
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.border.MatteBorder;

import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.visualization.VisualizeJobGraph;

public class VisualizeJobWindow extends AbstractWindow {

    private static final long serialVersionUID = 1L;
    private final ImageManager imageManager = ImageManager.getInstance();
    private final AnalysisJobBuilder _analysisJobBuilder;
    private final JScrollPane _scroll;
    private volatile boolean _displayColumns;
    private volatile boolean _displayOutcomes;

    public VisualizeJobWindow(AnalysisJobBuilder analysisJobBuilder, WindowContext windowContext) {
        super(windowContext);
        _analysisJobBuilder = analysisJobBuilder;
        _displayColumns = isDefaultDisplayColumns(analysisJobBuilder);
        _displayOutcomes = true;

        _scroll = WidgetUtils.scrolleable(null);
        _scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        _scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        refreshGraph();
    }

    private boolean isDefaultDisplayColumns(AnalysisJobBuilder analysisJobBuilder) {
        int columnsTotal = analysisJobBuilder.getAvailableInputColumns(Object.class).size();

        return columnsTotal <= 10;
    }

    public void refreshGraph() {
        final JComponent visualization = VisualizeJobGraph.create(_analysisJobBuilder, _displayColumns,
                _displayOutcomes);
        _scroll.setViewportView(visualization);
    }

    @Override
    protected void initialize() {
        super.initialize();
    }

    @Override
    protected void onWindowVisible() {
        super.onWindowVisible();
        boolean horizontalShowing = _scroll.getHorizontalScrollBar().isShowing();
        boolean verticalShowing = _scroll.getVerticalScrollBar().isShowing();
        if (horizontalShowing || verticalShowing) {
            // maximize if needed
            setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
        }
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
    public String getWindowTitle() {
        return "Visualize job";
    }

    @Override
    public Image getWindowIcon() {
        return imageManager.getImage("images/actions/visualize.png");
    }

    @Override
    protected JComponent getWindowContent() {
        DCPanel panel = new DCPanel();
        panel.setLayout(new BorderLayout());
        panel.add(_scroll, BorderLayout.CENTER);
        panel.add(createButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JComponent createButtonPanel() {
        final JCheckBox displayColumnsCheckBox = new JCheckBox("Display columns?");
        displayColumnsCheckBox.setOpaque(false);
        displayColumnsCheckBox.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
        displayColumnsCheckBox.setSelected(_displayColumns);
        displayColumnsCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _displayColumns = displayColumnsCheckBox.isSelected();
                refreshGraph();
            }
        });

        final JCheckBox displayFilterOutcomesCheckBox = new JCheckBox("Display filter outcomes?");
        displayFilterOutcomesCheckBox.setOpaque(false);
        displayFilterOutcomesCheckBox.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
        displayFilterOutcomesCheckBox.setSelected(_displayOutcomes);
        displayFilterOutcomesCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _displayOutcomes = displayFilterOutcomesCheckBox.isSelected();
                refreshGraph();
            }
        });

        final DCPanel buttonPanel = new DCPanel(WidgetUtils.BG_COLOR_DARK, WidgetUtils.BG_COLOR_DARK);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 10));
        buttonPanel.setBorder(new MatteBorder(1, 0, 0, 0, WidgetUtils.BG_COLOR_MEDIUM));

        buttonPanel.add(new JLabel(imageManager.getImageIcon("images/model/column.png")));
        buttonPanel.add(displayColumnsCheckBox);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(new JLabel(imageManager.getImageIcon("images/component-types/filter-outcome.png")));
        buttonPanel.add(displayFilterOutcomesCheckBox);

        return buttonPanel;
    }
}