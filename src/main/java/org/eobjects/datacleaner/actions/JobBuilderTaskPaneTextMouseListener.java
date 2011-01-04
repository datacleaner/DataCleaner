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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.util.LabelUtils;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.jdesktop.swingx.JXTaskPane;

/**
 * MouseListener that will present a popup menu on the task pane to enable the
 * user to rename a component (typically filter)
 * 
 * @author Kasper Sørensen
 */
public class JobBuilderTaskPaneTextMouseListener extends MouseAdapter {

	private final AbstractBeanJobBuilder<?, ?, ?> _jobBuilder;
	private final JXTaskPane _taskPane;

	public JobBuilderTaskPaneTextMouseListener(AbstractBeanJobBuilder<?, ?, ?> jobBuilder, JXTaskPane taskPane) {
		_jobBuilder = jobBuilder;
		_taskPane = taskPane;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int button = e.getButton();

		if (button == MouseEvent.BUTTON2 || button == MouseEvent.BUTTON3) {
			JMenuItem renameMenuItem = WidgetFactory.createMenuItem("Rename component", "images/actions/rename.png");
			renameMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final String originalValue = LabelUtils.getLabel(_jobBuilder);
					final String newValue = JOptionPane.showInputDialog("Name:", originalValue);
					if (!originalValue.equals(newValue)) {
						_jobBuilder.setName(newValue);
						_taskPane.setTitle(newValue);
					}
				}
			});

			JPopupMenu popup = new JPopupMenu();
			popup.add(renameMenuItem);
			popup.show(_taskPane, e.getX(), e.getY());
		}
	}
}
