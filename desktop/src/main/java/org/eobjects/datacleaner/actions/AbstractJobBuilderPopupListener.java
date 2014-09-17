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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.WidgetFactory;

/**
 * Abstract class containing the action method that will display a popup with
 * options as to change a job builder.
 * 
 * @author Kasper Sørensen
 */
public abstract class AbstractJobBuilderPopupListener {

	private final AnalysisJobBuilder _analysisJobBuilder;
	private final AbstractBeanJobBuilder<?, ?, ?> _jobBuilder;

	public AbstractJobBuilderPopupListener(AbstractBeanJobBuilder<?, ?, ?> jobBuilder, AnalysisJobBuilder analysisJobBuilder) {
		_jobBuilder = jobBuilder;
		_analysisJobBuilder = analysisJobBuilder;
	}

	public AbstractBeanJobBuilder<?, ?, ?> getJobBuilder() {
		return _jobBuilder;
	}

	public AnalysisJobBuilder getAnalysisJobBuilder() {
		return _analysisJobBuilder;
	}

	public void showPopup(Component parentComponent, int x, int y) {
		JMenuItem renameMenuItem = WidgetFactory.createMenuItem("Rename component", "images/actions/rename.png");
		renameMenuItem.addActionListener(new RenameComponentActionListener(_jobBuilder) {
			@Override
			protected void onNameChanged() {
				AbstractJobBuilderPopupListener.this.onNameChanged();
			}
		});

		JMenuItem removeMenuItem = WidgetFactory.createMenuItem("Remove component", IconUtils.ACTION_REMOVE);
		removeMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (_jobBuilder instanceof AnalyzerJobBuilder) {
					_analysisJobBuilder.removeAnalyzer((AnalyzerJobBuilder<?>) _jobBuilder);
				} else if (_jobBuilder instanceof TransformerJobBuilder) {
					_analysisJobBuilder.removeTransformer((TransformerJobBuilder<?>) _jobBuilder);
				} else if (_jobBuilder instanceof FilterJobBuilder) {
					_analysisJobBuilder.removeFilter((FilterJobBuilder<?, ?>) _jobBuilder);
				} else {
					throw new IllegalStateException("Unexpected component type: " + _jobBuilder);
				}
				onRemoved();
			}
		});

		JPopupMenu popup = new JPopupMenu();
		popup.add(renameMenuItem);
		popup.add(removeMenuItem);
		popup.show(parentComponent, x, y);
	}

	protected abstract void onNameChanged();

	protected abstract void onRemoved();
}
