/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.panels;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.eobjects.datacleaner.util.WidgetUtils;

/**
 * Encapsulated Swing glass pane, ensures correct access to it.
 * 
 * @author Kasper Sørensen
 */
public class DCGlassPane {

	private final JFrame _frame;
	private final JPanel _glassPane;

	public DCGlassPane(JFrame frame) {
		_frame = frame;
		_glassPane = (JPanel) frame.getGlassPane();
		_glassPane.setLayout(null);
		_glassPane.setBackground(WidgetUtils.BG_COLOR_DARKEST);
	}

	public void showTooltip(final JComponent comp, int timeoutMillis) {
		add(comp);

		new Timer(timeoutMillis, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				remove(comp);
			}
		}).start();
	}

	public void add(JComponent comp) {
		_glassPane.add(comp);
		_glassPane.setVisible(true);
	}

	public void remove(JComponent comp) {
		Component[] components = _glassPane.getComponents();
		for (int i = 0; i < components.length; i++) {
			Component component = components[i];
			if (component.equals(comp)) {
				_glassPane.remove(i);
				break;
			}
		}
		if (_glassPane.getComponentCount() == 0) {
			_glassPane.setVisible(false);
		}
	}

	public void addCentered(JComponent comp) {
		Dimension compSize = comp.getSize();
		Dimension totalSize = _frame.getContentPane().getSize();
		int x = (totalSize.width - compSize.width) / 2;
		int y = (totalSize.height - compSize.height) / 2;
		comp.setLocation(x, y);
		add(comp);
	}

	public boolean isEmpty() {
		return _glassPane.getComponentCount() == 0;
	}

	public Point getLocationOnScreen() {
		Point contentPaneLocation = _frame.getContentPane().getLocationOnScreen();
		int x = contentPaneLocation.x;
		int y = contentPaneLocation.y;
		JMenuBar menuBar = _frame.getJMenuBar();
		if (menuBar != null) {
			y -= menuBar.getHeight();
		}
		return new Point(x, y);
	}
}
