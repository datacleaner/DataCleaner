package org.eobjects.datacleaner.windows;

import java.awt.Image;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.renderer.Renderer;
import org.eobjects.analyzer.result.renderer.RendererFactory;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.util.WindowManager;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

public final class ResultWindow extends AbstractWindow {

	private static final long serialVersionUID = 1L;
	private List<AnalyzerResult> _results;

	public ResultWindow(List<AnalyzerResult> results) {
		super();
		_results = results;
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
	protected String getWindowTitle() {
		return "Analysis results";
	}

	@Override
	protected Image getWindowIcon() {
		return null;
	}

	@Override
	protected JComponent getWindowContent() {
		AnalyzerBeansConfiguration configuration = WindowManager.getInstance().getMainWindow().getConfiguration();
		RendererFactory renderFactory = new RendererFactory(configuration.getDescriptorProvider());

		JXTaskPaneContainer taskPaneContainer = new JXTaskPaneContainer();
		taskPaneContainer.setBackground(WidgetUtils.BG_COLOR_BRIGHT);

		for (AnalyzerResult analyzerResult : _results) {
			Renderer<? super AnalyzerResult, ? extends JComponent> renderer = renderFactory.getRenderer(analyzerResult,
					SwingRenderingFormat.class);
			JComponent component;
			if (renderer == null) {
				component = new JTextArea(analyzerResult.toString());
			} else {
				component = renderer.render(analyzerResult);
			}

			JXTaskPane taskPane = new JXTaskPane();
			taskPane.setTitle(analyzerResult.getClass().getSimpleName());
			taskPane.add(component);

			taskPaneContainer.add(taskPane);
		}

		return new JScrollPane(taskPaneContainer);
	}

}
