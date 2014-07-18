/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.panels.result;

import java.awt.BorderLayout;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingWorker;

import org.eobjects.analyzer.beans.api.Renderer;
import org.eobjects.analyzer.descriptors.ComponentDescriptor;
import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.renderer.RendererFactory;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.analyzer.util.LabelUtils;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCTaskPaneContainer;
import org.eobjects.datacleaner.widgets.LoadingIcon;
import org.jdesktop.swingx.JXTaskPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel that displays a collection of results in task panes.
 */
public class ResultListPanel extends DCPanel {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(ResultListPanel.class);

	private final RendererFactory _rendererFactory;
	private final DCTaskPaneContainer _taskPaneContainer;
	private final ProgressInformationPanel _progressInformationPanel;

	public ResultListPanel(RendererFactory rendererFactory, ProgressInformationPanel progressInformationPanel) {
		super(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		_rendererFactory = rendererFactory;
		_progressInformationPanel = progressInformationPanel;
		_taskPaneContainer = WidgetFactory.createTaskPaneContainer();
		setLayout(new BorderLayout());
		add(WidgetUtils.scrolleable(_taskPaneContainer), BorderLayout.CENTER);
	}

	public void addResult(final ComponentJob componentJob, final AnalyzerResult result) {
		final ComponentDescriptor<?> descriptor = componentJob.getDescriptor();
		final Icon icon = IconUtils.getDescriptorIcon(descriptor, IconUtils.ICON_SIZE_LARGE);

		final String resultLabel = LabelUtils.getLabel(componentJob);

		final JXTaskPane taskPane = WidgetFactory.createTaskPane(resultLabel, icon);

		final DCPanel taskPanePanel = new DCPanel(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		taskPanePanel.setLayout(new BorderLayout());
		taskPane.add(taskPanePanel);

		taskPanePanel.add(new LoadingIcon());
		_progressInformationPanel.addUserLog("Rendering result for " + resultLabel);

		WidgetUtils.invokeSwingAction(new Runnable() {
			@Override
			public void run() {
				String title = taskPane.getTitle();
				JXTaskPane[] taskPanes = _taskPaneContainer.getTaskPanes();
				boolean added = false;
				for (int i = 0; i < taskPanes.length; i++) {
					JXTaskPane existingTaskPane = taskPanes[i];
					if (existingTaskPane.getTitle().compareTo(title) > 0) {
						_taskPaneContainer.add(taskPane, i);
						added = true;
						break;
					}
				}
				if (!added) {
					_taskPaneContainer.add(taskPane);
				}
			}
		});

		// use a swing worker to run the rendering in the background
		new SwingWorker<JComponent, Void>() {

			@Override
			protected JComponent doInBackground() throws Exception {
				final Renderer<? super AnalyzerResult, ? extends JComponent> renderer = _rendererFactory.getRenderer(result,
						SwingRenderingFormat.class);
				if (renderer == null) {
					final String message = "No renderer found for result type " + result.getClass().getName();
					logger.error(message);
					throw new IllegalStateException(message);
				}
				logger.debug("renderer.render({})", result);
				final JComponent component = renderer.render(result);
				if (logger.isInfoEnabled()) {
					String resultAsString = result.toString();
					if (resultAsString.length() > 150) {
						resultAsString = resultAsString.substring(0, 147) + "...";
					}
					resultAsString = resultAsString.replaceAll("\n", " | ");
					logger.info("renderer.render({}) returned: {}", resultAsString, component);
				}
				return component;
			}

			protected void done() {
				taskPanePanel.removeAll();
				JComponent component;
				try {
					component = get();
					taskPanePanel.add(component);
					_progressInformationPanel.addUserLog("Result rendered for " + resultLabel);
				} catch (Exception e) {
					logger.error("Error occurred while rendering result", e);
					_progressInformationPanel.addUserLog("Error occurred while rendering result", e, false);
					taskPanePanel.add(new JLabel("An error occurred while rendering result, check the status tab"));
				}

				taskPanePanel.updateUI();
			};

		}.execute();
	}
}
