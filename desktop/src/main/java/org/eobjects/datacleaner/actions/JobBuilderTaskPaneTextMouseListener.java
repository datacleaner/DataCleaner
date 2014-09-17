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
package org.eobjects.datacleaner.actions;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.util.LabelUtils;
import org.jdesktop.swingx.JXTaskPane;

/**
 * MouseListener that will present a popup menu on the task pane to enable the
 * user to rename a component (typically filter)
 * 
 * @author Kasper Sørensen
 */
public class JobBuilderTaskPaneTextMouseListener extends AbstractJobBuilderPopupListener implements MouseListener {

	private final JXTaskPane _taskPane;

	public JobBuilderTaskPaneTextMouseListener(AnalysisJobBuilder analysisJobBuilder,
			AbstractBeanJobBuilder<?, ?, ?> jobBuilder, JXTaskPane taskPane) {
		super(jobBuilder, analysisJobBuilder);
		_taskPane = taskPane;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int button = e.getButton();

		if (button == MouseEvent.BUTTON2 || button == MouseEvent.BUTTON3) {
			showPopup(_taskPane, e.getX(), e.getY());
		}
	}

	@Override
	protected void onNameChanged() {
		_taskPane.setTitle(LabelUtils.getLabel(getJobBuilder()));
	}

	@Override
	protected void onRemoved() {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}
