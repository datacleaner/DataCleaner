/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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
package org.datacleaner.widgets;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.WidgetUtils;

/**
 * A vertical stack of {@link DCTaskPane}s - a lightweight replacement for
 * SwingX's {@code DCTaskPaneContainer}, wrapping all task panes in a
 * bordered panel.
 */
public class DCTaskPaneContainer extends JPanel {

    private static final long serialVersionUID = 1L;

    public DCTaskPaneContainer() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    }

    public void add(final DCTaskPane taskPane) {
        final DCPanel panel = createTaskPanePanel(taskPane);
        super.add(panel);
    }

    public void add(final DCTaskPane taskPane, final int index) {
        final DCPanel panel = createTaskPanePanel(taskPane);
        super.add(panel, index);
    }

    private DCPanel createTaskPanePanel(final DCTaskPane taskPane) {
        final DCPanel panel = new DCPanel();
        panel.setBorder(WidgetUtils.BORDER_SHADOW);
        panel.setLayout(new BorderLayout());
        panel.add(taskPane, BorderLayout.CENTER);
        // make the panel fill the full width of the container
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return panel;
    }

    public DCTaskPane[] getTaskPanes() {
        final Component[] components = getComponents();
        final DCTaskPane[] result = new DCTaskPane[components.length];
        for (int i = 0; i < result.length; i++) {
            final DCPanel panel = (DCPanel) components[i];
            result[i] = (DCTaskPane) panel.getComponent(0);
        }
        return result;
    }

    public void remove(final DCTaskPane group) {
        final Component[] components = getComponents();
        for (final Component component : components) {
            if (component instanceof DCPanel) {
                final Component innerComponent = ((DCPanel) component).getComponent(0);
                if (innerComponent.equals(group)) {
                    super.remove(component);
                    return;
                }
            }
        }
    }
}
