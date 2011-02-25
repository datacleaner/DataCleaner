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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Encapsulated Swing glass pane, ensures correct access to it.
 * 
 * @author Kasper Sørensen
 */
public class DCGlassPane {

	private final JPanel _glassPane;

	public DCGlassPane(Component glassPane) {
		_glassPane = (JPanel) glassPane;
		_glassPane.setLayout(null);
	}

	public void show(final JComponent comp, int timeoutMillis) {
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
		_glassPane.remove(comp);
		if (_glassPane.getComponentCount() == 0) {
			_glassPane.setVisible(false);
		}
	}
}
