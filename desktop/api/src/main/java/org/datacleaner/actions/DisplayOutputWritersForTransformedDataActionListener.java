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
package org.datacleaner.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;

import org.datacleaner.api.Component;
import org.datacleaner.api.InputColumn;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.desktop.api.PrecedingComponentConsumer;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.lifecycle.LifeCycleHelper;

/**
 * Action that displays output writers for a transformer's data.
 */
public class DisplayOutputWritersForTransformedDataActionListener extends DisplayOutputWritersAction
        implements ActionListener {

    private final TransformerComponentBuilder<?> _transformerJobBuilder;

    public DisplayOutputWritersForTransformedDataActionListener(
            final TransformerComponentBuilder<?> transformerJobBuilder) {
        super(transformerJobBuilder.getAnalysisJobBuilder());
        _transformerJobBuilder = transformerJobBuilder;
    }

    @Override
    protected void configure(final AnalysisJobBuilder analysisJobBuilder, final ComponentBuilder componentBuilder) {
        final Component component = componentBuilder.getComponentInstance();
        if (component instanceof PrecedingComponentConsumer) {
            final LifeCycleHelper helper = new LifeCycleHelper(analysisJobBuilder.getConfiguration(), null, true);
            helper.assignProvidedProperties(componentBuilder.getDescriptor(), component);
            ((PrecedingComponentConsumer) component)
                    .configureForTransformedData(analysisJobBuilder, _transformerJobBuilder.getDescriptor());
        }

        final Set<ConfiguredPropertyDescriptor> inputProperties =
                componentBuilder.getDescriptor().getConfiguredPropertiesForInput(false);
        if (!inputProperties.isEmpty()) {
            final ConfiguredPropertyDescriptor property = inputProperties.iterator().next();

            final List<InputColumn<?>> inputColumnsToAdd = new ArrayList<>();
            inputColumnsToAdd.addAll(_transformerJobBuilder.getInputColumns());
            final List<MutableInputColumn<?>> outputColumns = _transformerJobBuilder.getOutputColumns();
            for (final MutableInputColumn<?> outputColumn : outputColumns) {
                if (!outputColumn.isHidden()) {
                    inputColumnsToAdd.add(outputColumn);
                }
            }
            componentBuilder.addInputColumns(inputColumnsToAdd, property);
        }
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final JComponent component = (JComponent) e.getSource();
        showPopup(component);
    }

}
