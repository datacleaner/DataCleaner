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

    public DisplayOutputWritersAction(AnalysisJobBuilder analysisJobBuilder) {
        _analysisJobBuilder = analysisJobBuilder;
    }

    public final void showPopup(JComponent component) {
        JPopupMenu popup = new JPopupMenu();

        List<JMenuItem> menuItems = createMenuItems();
        for (JMenuItem menuItem : menuItems) {
            popup.add(menuItem);
        }

        popup.show(component, 0, component.getHeight());
    }

    public List<JMenuItem> createMenuItems() {
        List<JMenuItem> result = new ArrayList<JMenuItem>();
        for (final ComponentDescriptor<?> descriptor : getDescriptors()) {
            JMenuItem outputWriterMenuItem = new DescriptorMenuItem(_analysisJobBuilder, null, descriptor);
            outputWriterMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ComponentBuilder componentBuilder = _analysisJobBuilder.addComponent(descriptor);

                    configure(_analysisJobBuilder, componentBuilder);
                }
            });
            result.add(outputWriterMenuItem);
        }
        return result;
    }

    protected void configure(AnalysisJobBuilder analysisJobBuilder, ComponentBuilder componentBuilder) {
    }

    protected Collection<? extends ComponentDescriptor<?>> getDescriptors() {
        final DescriptorProvider descriptorProvider = _analysisJobBuilder.getConfiguration().getDescriptorProvider();
        final Collection<? extends ComponentDescriptor<?>> descriptors = descriptorProvider
                .getComponentDescriptorsOfSuperCategory(new WriteSuperCategory());
        return descriptors;
    }
}
