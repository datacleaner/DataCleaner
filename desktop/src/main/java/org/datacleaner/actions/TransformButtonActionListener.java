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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.metamodel.util.CollectionUtils;
import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.descriptors.BeanDescriptor;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.descriptors.FilterBeanDescriptor;
import org.datacleaner.descriptors.TransformerBeanDescriptor;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.user.UsageLogger;
import org.datacleaner.widgets.DescriptorMenuBuilder;
import org.datacleaner.widgets.DescriptorMenuItem;
import org.datacleaner.widgets.visualization.JobGraphMetadata;

public class TransformButtonActionListener implements ActionListener {

    private final AnalyzerBeansConfiguration _configuration;
    private final AnalysisJobBuilder _analysisJobBuilder;
    private final UsageLogger _usageLogger;

    @Inject
    public TransformButtonActionListener(final AnalyzerBeansConfiguration configuration,
            final AnalysisJobBuilder analysisJobBuilder, final UsageLogger usageLogger) {
        _configuration = configuration;
        _analysisJobBuilder = analysisJobBuilder;
        _usageLogger = usageLogger;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final List<BeanDescriptor<?>> descriptors = getDescriptors();

        final JPopupMenu popup = new JPopupMenu();
        final DescriptorMenuBuilder descriptorMenuBuilder = new DescriptorMenuBuilder(descriptors) {
            @Override
            protected JMenuItem createMenuItem(final BeanDescriptor<?> descriptor) {
                return TransformButtonActionListener.this.createMenuItem(descriptor, null);
            }
        };
        descriptorMenuBuilder.addItemsToPopupMenu(popup);

        showPopup(e, popup);
    }

    public JMenuItem createMenuItem(final BeanDescriptor<?> descriptor, final Point2D p) {
        final DescriptorMenuItem menuItem = new DescriptorMenuItem(descriptor);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final Map<String, String> metadata = JobGraphMetadata.createMetadataProperties(p);

                if (descriptor instanceof TransformerBeanDescriptor) {
                    final TransformerBeanDescriptor<?> transformerDescriptor = (TransformerBeanDescriptor<?>) descriptor;
                    _analysisJobBuilder.addTransformer(transformerDescriptor, null, null, metadata);
                } else if (descriptor instanceof FilterBeanDescriptor) {
                    final FilterBeanDescriptor<?, ?> filterDescriptor = (FilterBeanDescriptor<?, ?>) descriptor;
                    _analysisJobBuilder.addFilter(filterDescriptor, null, null, metadata);
                }
                _usageLogger.logComponentUsage(descriptor);
            }
        });
        return menuItem;
    }

    public List<BeanDescriptor<?>> getDescriptors() {
        final DescriptorProvider descriptorProvider = _configuration.getDescriptorProvider();
        final Collection<FilterBeanDescriptor<?, ?>> filterBeanDescriptors = descriptorProvider
                .getFilterBeanDescriptors();
        final Collection<TransformerBeanDescriptor<?>> transformerBeanDescritpors = descriptorProvider
                .getTransformerBeanDescriptors();
        final List<BeanDescriptor<?>> descriptors = CollectionUtils.<BeanDescriptor<?>> concat(false,
                filterBeanDescriptors, transformerBeanDescritpors);
        return descriptors;
    }

    protected void showPopup(ActionEvent e, JPopupMenu popup) {
        Component source = (Component) e.getSource();
        popup.show(source, 0, source.getHeight());
    }
}
