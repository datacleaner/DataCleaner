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
package org.datacleaner.widgets.result;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableModel;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.Provided;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.result.AnnotatedRowsResult;
import org.datacleaner.result.CategorizationResult;
import org.datacleaner.result.renderer.AbstractRenderer;
import org.datacleaner.result.renderer.RendererFactory;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.windows.DetailsResultWindow;

public abstract class AbstractCategorizationResultSwingRenderer<R extends CategorizationResult> extends
        AbstractRenderer<R, JComponent> {
    @Inject
    @Provided
    WindowContext windowContext;

    @Inject
    @Provided
    RendererFactory rendererFactory;

    private int addValue(final Object extraData, final int row, final DefaultTableModel model, final String desc,
            final int count, final AnnotatedRowsResult sampleResult) {
        model.setValueAt(desc, row, 0);
        if (sampleResult == null || count == 0) {
            model.setValueAt(count, row, 1);
        } else {
            final DCPanel panel = new DCPanel();
            panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

            final JLabel label = new JLabel(count + "");
            final JButton button = WidgetFactory.createSmallButton(IconUtils.ACTION_DRILL_TO_DETAIL);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    drillToGroup(desc, sampleResult);
                }

            });

            panel.add(label);
            panel.add(Box.createHorizontalStrut(4));
            panel.add(button);

            model.setValueAt(panel, row, 1);
        }

        addExtraValue(extraData, row, model, desc, count, sampleResult);
        return row + 1;
    }

    protected void addExtraValue(Object extraData, int row, DefaultTableModel model, String desc, int count,
            AnnotatedRowsResult sampleResult) {
        // Do nothing by default
    }

    private void drillToGroup(final String title, final AnnotatedRowsResult sampleResult) {
        List<AnalyzerResult> results = Arrays.<AnalyzerResult> asList(sampleResult);
        final DetailsResultWindow window = new DetailsResultWindow(title, results, windowContext, rendererFactory);
        window.open();
    }

    protected DefaultTableModel prepareModel(final CategorizationResult analyzerResult) {
        return prepareModel(analyzerResult, null);
    }

    protected DefaultTableModel prepareModel(final CategorizationResult analyzerResult, final Object extraData) {
        final DefaultTableModel model = new DefaultTableModel(new Object[] { "Match outcome", LabelUtils.COUNT_LABEL },
                analyzerResult.getCategoryNames().size());
        int row = 0;
        final Collection<String> categoryNames = analyzerResult.getCategoryNames();
        for (String categoryName : categoryNames) {
            final AnnotatedRowsResult sample = analyzerResult.getCategoryRowSample(categoryName);
            final Number count = analyzerResult.getCategoryCount(categoryName);
            row = addValue(extraData, row, model, categoryName, count.intValue(), sample);
        }
        return model;
    }

}
