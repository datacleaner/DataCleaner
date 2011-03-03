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
package org.eobjects.datacleaner.windows;

import java.awt.Image;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.visualization.VisualizeJobGraph;

public class VisualizeJobWindow extends AbstractWindow {

	private static final long serialVersionUID = 1L;
	private final ImageManager imageManager = ImageManager.getInstance();
	private final AnalysisJobBuilder _analysisJobBuilder;
	private JScrollPane _scroll;

	public VisualizeJobWindow(AnalysisJobBuilder analysisJobBuilder) {
		_analysisJobBuilder = analysisJobBuilder;

		final JComponent visualization = VisualizeJobGraph.create(_analysisJobBuilder);
		_scroll = WidgetUtils.scrolleable(visualization);
		_scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		_scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	}

	@Override
	protected void initialize() {
		super.initialize();
	}

	@Override
	protected void onWindowVisible() {
		super.onWindowVisible();
		boolean horizontalShowing = _scroll.getHorizontalScrollBar().isShowing();
		boolean verticalShowing = _scroll.getVerticalScrollBar().isShowing();
		if (horizontalShowing || verticalShowing) {
			// maximize if needed
			setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
		}
	}

	@Override
	protected boolean isWindowResizable() {
		return true;
	}

	@Override
	protected boolean isCentered() {
		return true;
	}

	@Override
	public String getWindowTitle() {
		return "Visualize job";
	}

	@Override
	public Image getWindowIcon() {
		return imageManager.getImage("images/actions/visualize.png");
	}

	@Override
	protected JComponent getWindowContent() {
		return _scroll;
	}
}