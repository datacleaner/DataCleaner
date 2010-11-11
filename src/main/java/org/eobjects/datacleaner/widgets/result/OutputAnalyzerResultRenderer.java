package org.eobjects.datacleaner.widgets.result;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.result.renderer.Renderer;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.datacleaner.output.beans.OutputAnalyzerResult;

@RendererBean(SwingRenderingFormat.class)
public class OutputAnalyzerResultRenderer implements Renderer<OutputAnalyzerResult, JComponent> {

	@Override
	public JComponent render(OutputAnalyzerResult result) {
		return new JLabel("Output written");
	}

}
