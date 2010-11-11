package org.eobjects.datacleaner.windows;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JTextArea;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.renderer.Renderer;
import org.eobjects.analyzer.result.renderer.RendererFactory;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.builder.WidgetFactory;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

public class ResultListPanel extends DCPanel {

	private static final long serialVersionUID = 1L;

	private final RendererFactory _rendererFactory;
	private final JXTaskPaneContainer _taskPaneContainer;

	public ResultListPanel(RendererFactory rendererFactory) {
		super(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		_rendererFactory = rendererFactory;
		setLayout(new BorderLayout());
		_taskPaneContainer = WidgetFactory.createTaskPaneContainer();
		add(_taskPaneContainer, BorderLayout.CENTER);
	}

	public void addResult(AnalyzerJob analyzerJob, AnalyzerResult result) {
		Renderer<? super AnalyzerResult, ? extends JComponent> renderer = _rendererFactory.getRenderer(result,
				SwingRenderingFormat.class);
		JComponent component;
		if (renderer == null) {
			component = new JTextArea(result.toString());
		} else {
			component = renderer.render(result);
		}
		JXTaskPane taskPane = new JXTaskPane();

		StringBuilder sb = new StringBuilder();
		sb.append(analyzerJob.getDescriptor().getDisplayName());

		InputColumn<?>[] input = analyzerJob.getInput();
		if (input.length > 0) {
			sb.append(" (");
			if (input.length < 4) {
				for (int i = 0; i < input.length; i++) {
					if (i != 0) {
						sb.append(',');
					}
					sb.append(input[i].getName());
				}
			} else {
				sb.append(input.length);
				sb.append(" columns");
			}
			sb.append(")");
		}

		taskPane.setTitle(sb.toString());
		taskPane.add(component);

		synchronized (this) {
			_taskPaneContainer.add(taskPane);
		}
	}

}
