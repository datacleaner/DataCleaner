package org.eobjects.datacleaner.widgets.result;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.result.ValidationResult;
import org.eobjects.analyzer.result.renderer.Renderer;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;

@RendererBean(SwingRenderingFormat.class)
public class ValidationResultSwingRenderer implements Renderer<ValidationResult, JPanel> {

	@Override
	public JPanel render(ValidationResult result) {
		JPanel panel = new AnnotatedRowsResultSwingRenderer().render(result);
		panel.add(new JLabel("Invalid rows:"), 0);
		return panel;
	}
}
