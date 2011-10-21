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
package org.eobjects.datacleaner.testtools.ui;

import javax.inject.Inject;
import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;

import org.eobjects.analyzer.beans.api.Renderer;
import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.beans.api.RendererPrecedence;
import org.eobjects.analyzer.result.AnnotatedRowsResult;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.datacleaner.guice.InjectorBuilder;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.testtools.TestToolAnalyzerResult;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCCollapsiblePanel;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.widgets.result.AnnotatedRowsResultSwingRenderer;
import org.eobjects.metamodel.util.ImmutableRef;
import org.jdesktop.swingx.VerticalLayout;

@RendererBean(SwingRenderingFormat.class)
public class TestToolAnalyzerSwingResultRenderer implements
		Renderer<TestToolAnalyzerResult, JComponent> {

	@Inject
	InjectorBuilder injectorBuilder;

	@Override
	public RendererPrecedence getPrecedence(TestToolAnalyzerResult result) {
		return RendererPrecedence.HIGH;
	}

	@Override
	public JComponent render(TestToolAnalyzerResult result) {
		DCPanel panel = new DCPanel();
		panel.setLayout(new VerticalLayout());

		DCLabel label = DCLabel.dark("Test was succesfull!");
		label.setFont(WidgetUtils.FONT_HEADER1);
		label.setIcon(ImageManager.getInstance().getImageIcon(
				"images/status/valid.png"));
		if (!result.isSuccesfull()) {
			label.setText("Test failed!");
			label.setIcon(ImageManager.getInstance().getImageIcon(
					"images/status/error.png"));
		}
		panel.add(label);

		AnnotatedRowsResult errorRowsResult = result.getErrorRowsResult();
		if (!result.isSuccesfull()) {
			AnnotatedRowsResultSwingRenderer annotatedRowResultRenderer = injectorBuilder
					.getInstance(AnnotatedRowsResultSwingRenderer.class);
			DCPanel errorRowsPanel = annotatedRowResultRenderer
					.render(errorRowsResult);

			String text = errorRowsResult.getRowCount() + " errornous records";
			DCCollapsiblePanel errorCollapsiblePanel = new DCCollapsiblePanel(
					text, text, false,
					ImmutableRef.<JComponent> of(errorRowsPanel));

			DCPanel innerPanel = errorCollapsiblePanel.toPanel();
			innerPanel.setBorder(new EmptyBorder(4, 10, 4, 10));
			panel.add(innerPanel);
		}

		return panel;
	}
}
