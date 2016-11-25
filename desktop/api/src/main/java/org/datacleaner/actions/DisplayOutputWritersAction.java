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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.datacleaner.components.categories.WriteSuperCategory;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.widgets.DescriptorMenuItem;

public class DisplayOutputWritersAction {

    private final AnalysisJobBuilder _analysisJobBuilder;

    public DisplayOutputWritersAction(final AnalysisJobBuilder analysisJobBuilder) {
        _analysisJobBuilder = analysisJobBuilder;
    }

    public final void showPopup(final JComponent component) {
        final JPopupMenu popup = new JPopupMenu();

        final List<JMenuItem> menuItems = createMenuItems();
        for (final JMenuItem menuItem : menuItems) {
            popup.add(menuItem);
        }

        popup.show(component, 0, component.getHeight());
    }

    public List<JMenuItem> createMenuItems() {
        final List<JMenuItem> result = new ArrayList<>();
        for (final ComponentDescriptor<?> descriptor : getDescriptors()) {
            final JMenuItem outputWriterMenuItem = new DescriptorMenuItem(_analysisJobBuilder, null, descriptor, false);
            outputWriterMenuItem.addActionListener(e -> {
                final ComponentBuilder componentBuilder = _analysisJobBuilder.addComponent(descriptor);

                configure(_analysisJobBuilder, componentBuilder);
            });
            result.add(outputWriterMenuItem);
        }
        return result;
    }

    protected void configure(final AnalysisJobBuilder analysisJobBuilder, final ComponentBuilder componentBuilder) {
    }

    protected Collection<? extends ComponentDescriptor<?>> getDescriptors() {
        final DescriptorProvider descriptorProvider =
                _analysisJobBuilder.getConfiguration().getEnvironment().getDescriptorProvider();
        return descriptorProvider.getComponentDescriptorsOfSuperCategory(new WriteSuperCategory());
    }
}
