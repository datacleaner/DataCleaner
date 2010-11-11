package org.eobjects.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTextArea;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.renderer.Renderer;
import org.eobjects.analyzer.result.renderer.RendererFactory;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.util.WindowManager;
import org.eobjects.datacleaner.widgets.builder.WidgetFactory;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

public final class DetailsResultWindow extends AbstractWindow {

	private static final long serialVersionUID = 1L;
	private final List<AnalyzerResult> _results;
	private final String _title;

	public DetailsResultWindow(String title, List<AnalyzerResult> results) {
		super();
		_title = title;
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
		return _title;
	}

	@Override
	protected Image getWindowIcon() {
		return ImageManager.getInstance().getImage("images/model/result.png");
	}

	@Override
	protected JComponent getWindowContent() {
		AnalyzerBeansConfiguration configuration = WindowManager.getInstance().getMainWindow().getConfiguration();
		RendererFactory renderFactory = new RendererFactory(configuration.getDescriptorProvider());

		JXTaskPaneContainer taskPaneContainer = WidgetFactory.createTaskPaneContainer();
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

		DCPanel panel = new DCPanel(WidgetUtils.BG_COLOR_MEDIUM, WidgetUtils.BG_COLOR_LESS_DARK);
		panel.setLayout(new BorderLayout());
		panel.add(WidgetUtils.scrolleable(taskPaneContainer), BorderLayout.CENTER);

		Dimension preferredSize = panel.getPreferredSize();
		panel.setPreferredSize(preferredSize.width, preferredSize.height + 100);

		return panel;
	}

}
