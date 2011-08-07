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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.eobjects.analyzer.beans.api.Renderer;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.renderer.RendererFactory;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCTaskPaneContainer;
import org.jdesktop.swingx.JXTaskPane;

/**
 * Window for showing a single analysis result in a separate window. Typically
 * used for "details", eg. interactionable parts of the result.
 * 
 * @author Kasper Sørensen
 */
public final class DetailsResultWindow extends AbstractWindow {

	private static final long serialVersionUID = 1L;

	private final static ImageManager imageManager = ImageManager.getInstance();
	private final RendererFactory _rendererFactory;
	private final List<AnalyzerResult> _results;
	private final String _title;
	private final DCTaskPaneContainer _taskPaneContainer;

	public DetailsResultWindow(String title, List<AnalyzerResult> results, WindowContext windowContext, RendererFactory rendererFactory) {
		super(windowContext);
		_title = title;
		_results = results;
		_taskPaneContainer = WidgetFactory.createTaskPaneContainer();
		_taskPaneContainer.setBackground(WidgetUtils.BG_COLOR_BRIGHT);
		_rendererFactory = rendererFactory;
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
		return _title;
	}

	@Override
	public Image getWindowIcon() {
		return imageManager.getImage("images/model/result.png");
	}

	@Override
	protected JComponent getWindowContent() {
		if (!_results.isEmpty()) {
			for (AnalyzerResult analyzerResult : _results) {
				Renderer<? super AnalyzerResult, ? extends JComponent> renderer = _rendererFactory.getRenderer(analyzerResult,
						SwingRenderingFormat.class);
				JComponent component;
				if (renderer == null) {
					component = new JTextArea(analyzerResult.toString());
				} else {
					component = renderer.render(analyzerResult);
				}

				addRenderedResult(component);
			}
		}

		DCPanel panel = new DCPanel(WidgetUtils.BG_COLOR_LESS_DARK, WidgetUtils.BG_COLOR_DARK);
		panel.setLayout(new BorderLayout());
		panel.add(WidgetUtils.scrolleable(_taskPaneContainer), BorderLayout.CENTER);

		Dimension preferredSize = panel.getPreferredSize();
		int height = preferredSize.height < 400 ? preferredSize.height + 100 : preferredSize.height;
		int width = preferredSize.width < 500 ? 500 : preferredSize.width;
		panel.setPreferredSize(width, height);

		return panel;
	}

	public void addRenderedResult(JComponent component) {
		ImageIcon icon = imageManager.getImageIcon("images/actions/drill-to-detail.png");
		JXTaskPane taskPane = WidgetFactory.createTaskPane("Detailed results", icon);

		final DCPanel taskPanePanel = new DCPanel(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		taskPanePanel.setBorder(new EmptyBorder(4, 4, 4, 4));
		taskPanePanel.setLayout(new BorderLayout());
		taskPanePanel.add(component);

		taskPane.add(taskPanePanel);

		_taskPaneContainer.add(taskPane);
	}

}
