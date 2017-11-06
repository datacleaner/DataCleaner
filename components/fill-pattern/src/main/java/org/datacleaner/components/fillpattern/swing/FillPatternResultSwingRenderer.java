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
package org.datacleaner.components.fillpattern.swing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFrame;

import org.apache.metamodel.util.Ref;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.Provided;
import org.datacleaner.api.RendererBean;
import org.datacleaner.bootstrap.DCWindowContext;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.components.fillpattern.FillPatternGroup;
import org.datacleaner.components.fillpattern.FillPatternResult;
import org.datacleaner.components.fillpattern.FillPatternsBuilder;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.result.renderer.AbstractRenderer;
import org.datacleaner.result.renderer.RendererFactory;
import org.datacleaner.result.renderer.SwingRenderingFormat;
import org.datacleaner.storage.InMemoryRowAnnotationFactory2;
import org.datacleaner.storage.RowAnnotationFactory;
import org.datacleaner.user.UserPreferencesImpl;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.LookAndFeelManager;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.widgets.ComboButton;
import org.datacleaner.widgets.DCCollapsiblePanel;
import org.jdesktop.swingx.VerticalLayout;

@RendererBean(SwingRenderingFormat.class)
public class FillPatternResultSwingRenderer extends AbstractRenderer<FillPatternResult, JComponent> {

    @Inject
    @Provided
    WindowContext windowContext;

    @Inject
    @Provided
    RendererFactory rendererFactory;

    @Override
    public JComponent render(FillPatternResult fillPatternResult) {
        final JComponent tableViewPanel = createTableViewPanel(fillPatternResult);
        final JComponent fieldListPanel = createFieldListPanel(fillPatternResult);

        final ComboButton comboButton = new ComboButton();
        final AbstractButton tableViewButton = comboButton.addButton("Table view", IconUtils.MODEL_TABLE, true);
        tableViewButton.addActionListener(e -> {
            tableViewPanel.setVisible(true);
            fieldListPanel.setVisible(false);
        });
        final AbstractButton fieldListButton = comboButton.addButton("Field list", IconUtils.MODEL_ROW, true);
        fieldListButton.addActionListener(e -> {
            tableViewPanel.setVisible(false);
            fieldListPanel.setVisible(true);
        });

        final DCPanel comboButtonPanel = DCPanel.flow(Alignment.RIGHT, comboButton);

        final DCPanel panel = new DCPanel();
        panel.setLayout(new VerticalLayout(0));

        panel.add(comboButtonPanel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(tableViewPanel);
        panel.add(fieldListPanel);

        tableViewButton.doClick();

        return panel;
    }

    private JComponent createFieldListPanel(FillPatternResult fillPatternResult) {
        if (fillPatternResult.isGrouped()) {
            final DCPanel panel = new DCPanel();
            panel.setLayout(new VerticalLayout(0));

            final List<FillPatternGroup> groups = fillPatternResult.getFillPatternGroups();
            for (FillPatternGroup group : groups) {
                if (panel.getComponentCount() != 0) {
                    panel.add(Box.createVerticalStrut(10));
                }
                final int recordCount = group.getTotalObservationCount();
                final int patternCount = group.getPatternCount();

                final String text = group.getGroupName() + " (" + (patternCount == 1 ? "1 pattern"
                        : patternCount + " patterns") + ", " + (recordCount == 1 ? "1 record"
                                : recordCount + " records") + ")";
                final Ref<? extends JComponent> componentRef = () -> new FillPatternGroupListPanel(windowContext,
                        rendererFactory, fillPatternResult, group);
                final DCCollapsiblePanel collapsiblePanel = new DCCollapsiblePanel(text, text, patternCount < 2,
                        componentRef);
                panel.add(collapsiblePanel.toPanel());
            }
            return panel;
        } else {
            return new FillPatternGroupListPanel(windowContext, rendererFactory, fillPatternResult, fillPatternResult
                    .getFillPatternGroups().get(0));
        }
    }

    private JComponent createTableViewPanel(FillPatternResult fillPatternResult) {
        if (fillPatternResult.isGrouped()) {
            final DCPanel panel = new DCPanel();
            panel.setLayout(new VerticalLayout(0));

            final List<FillPatternGroup> groups = fillPatternResult.getFillPatternGroups();
            for (FillPatternGroup group : groups) {
                if (panel.getComponentCount() != 0) {
                    panel.add(Box.createVerticalStrut(10));
                }
                final int recordCount = group.getTotalObservationCount();
                final int patternCount = group.getPatternCount();

                final String text = group.getGroupName() + " (" + (patternCount == 1 ? "1 pattern"
                        : patternCount + " patterns") + ", " + (recordCount == 1 ? "1 record"
                                : recordCount + " records") + ")";
                final Ref<? extends JComponent> componentRef = () -> new FillPatternGroupTabelPanel(windowContext,
                        rendererFactory, fillPatternResult, group);
                final DCCollapsiblePanel collapsiblePanel = new DCCollapsiblePanel(text, text, patternCount < 2,
                        componentRef);
                panel.add(collapsiblePanel.toPanel());
            }
            return panel;
        } else {
            return new FillPatternGroupTabelPanel(windowContext, rendererFactory, fillPatternResult, fillPatternResult
                    .getFillPatternGroups().get(0));
        }

    }

    public static void main(String[] args) {
        LookAndFeelManager.get().init();

        final DataCleanerConfigurationImpl configuration = new DataCleanerConfigurationImpl();
        final FillPatternResultSwingRenderer renderer = new FillPatternResultSwingRenderer();
        renderer.rendererFactory = new RendererFactory(configuration);
        renderer.windowContext = new DCWindowContext(configuration, new UserPreferencesImpl(null), null);

        final InputColumn<?> col1 = new MockInputColumn<>("foo");
        final InputColumn<?> col2 = new MockInputColumn<>("bar");
        final InputColumn<?> col3 = new MockInputColumn<>("baz");

        final RowAnnotationFactory rowAnnotationFactory = new InMemoryRowAnnotationFactory2();
        final List<InputColumn<?>> inspectedColumns = new ArrayList<>();
        inspectedColumns.add(col1);
        inspectedColumns.add(col2);
        inspectedColumns.add(col3);

        final List<FillPatternGroup> fillPatterns = new ArrayList<>();

        final FillPatternsBuilder fillPatternsBuilder = new FillPatternsBuilder(rowAnnotationFactory);
        fillPatternsBuilder.addObservation(new MockInputRow().put(col1, "hello"), Arrays.asList("<filled>",
                LabelUtils.NULL_LABEL, LabelUtils.NULL_LABEL));
        fillPatternsBuilder.addObservation(new MockInputRow().put(col1, "").put(col2, "world"), Arrays.asList(
                LabelUtils.BLANK_LABEL, "<filled>", LabelUtils.NULL_LABEL));
        fillPatternsBuilder.addObservation(new MockInputRow().put(col1, "hello").put(col2, "world"), Arrays.asList(
                "<filled>", "<filled>", LabelUtils.NULL_LABEL));

        fillPatterns.add(fillPatternsBuilder.build("group1"));

        final FillPatternResult fillPatternResult = new FillPatternResult(rowAnnotationFactory, inspectedColumns,
                fillPatterns);
        final JComponent renderedResult = renderer.render(fillPatternResult);

        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 600);
        frame.getContentPane().add(renderedResult);
        frame.setVisible(true);
    }
}
