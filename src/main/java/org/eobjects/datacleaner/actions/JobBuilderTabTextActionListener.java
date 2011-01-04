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
package org.eobjects.datacleaner.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.util.LabelUtils;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.widgets.tabs.CloseableTabbedPane;

/**
 * ActionListener that will present a popup menu on the tabbed pane to enable
 * the user to rename a component
 * 
 * @author Kasper Sørensen
 */
public class JobBuilderTabTextActionListener implements ActionListener {

	private final AbstractBeanJobBuilder<?, ?, ?> _jobBuilder;
	private final CloseableTabbedPane _tabbedPane;
	private final int _tabIndex;

	public JobBuilderTabTextActionListener(AbstractBeanJobBuilder<?, ?, ?> jobBuilder, int tabIndex,
			CloseableTabbedPane tabbedPane) {
		_jobBuilder = jobBuilder;
		_tabIndex = tabIndex;
		_tabbedPane = tabbedPane;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JMenuItem renameMenuItem = WidgetFactory.createMenuItem("Rename component", "images/actions/rename.png");
		renameMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final String originalValue = LabelUtils.getLabel(_jobBuilder);
				final String newValue = JOptionPane.showInputDialog("Name:", originalValue);
				if (!originalValue.equals(newValue)) {
					_jobBuilder.setName(newValue);
					_tabbedPane.setTitleAt(_tabIndex, newValue);
				}
			}
		});

		MouseEvent mouseEvent = (MouseEvent) e.getSource();

		JPopupMenu popup = new JPopupMenu();
		popup.add(renameMenuItem);
		popup.show(_tabbedPane, mouseEvent.getX(), mouseEvent.getY());
	}
}
