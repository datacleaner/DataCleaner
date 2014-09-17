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
package org.eobjects.datacleaner.widgets;

import java.awt.BorderLayout;
import java.awt.Component;

import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

/**
 * Minor improvement to the regular JXTaskPaneContainer, wrapping all task panes
 * in a bordered panel.
 */
public class DCTaskPaneContainer extends JXTaskPaneContainer {

	private static final long serialVersionUID = 1L;

	public DCTaskPaneContainer() {
		super();
		setOpaque(false);
		setBackgroundPainter(null);
	}

	public void add(JXTaskPane group) {
		DCPanel panel = createTaskPanePanel(group);
		super.add(panel);
	}

	public void add(JXTaskPane group, int index) {
		DCPanel panel = createTaskPanePanel(group);
		super.add(panel, index);
	}

	private DCPanel createTaskPanePanel(JXTaskPane group) {
		DCPanel panel = new DCPanel();
		panel.setBorder(WidgetUtils.BORDER_SHADOW);
		panel.setLayout(new BorderLayout());
		panel.add(group, BorderLayout.CENTER);
		return panel;
	}

	@Override
	public Component add(Component comp) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Component add(Component comp, int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(Component comp, Object constraints) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(Component comp, Object constraints, int index) {
		throw new UnsupportedOperationException();
	}

	public JXTaskPane[] getTaskPanes() {
		Component[] components = getComponents();
		JXTaskPane[] result = new JXTaskPane[components.length];
		for (int i = 0; i < result.length; i++) {
			DCPanel panel = (DCPanel) components[i];
			result[i] = (JXTaskPane) panel.getComponent(0);
		}
		return result;
	}
	
	public void remove(JXTaskPane group) {
		Component[] components = getComponents();
		for (Component component : components) {
			if (component instanceof DCPanel) {
				Component innerComponent = ((DCPanel) component).getComponent(0);
				if (innerComponent.equals(group)) {
					super.remove(component);
					return;
				}
			}
		}
	}
}