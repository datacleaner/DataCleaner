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
package org.datacleaner.sample.ui;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.job.builder.TransformerJobBuilder;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.panels.TransformerJobBuilderPanel;
import org.datacleaner.panels.TransformerJobBuilderPresenter;
import org.datacleaner.widgets.properties.PropertyWidgetFactory;

public class HelloWorldTransformerPresenter extends TransformerJobBuilderPanel implements TransformerJobBuilderPresenter {

	private static final long serialVersionUID = 1L;

	public HelloWorldTransformerPresenter(TransformerJobBuilder<?> transformerJobBuilder, WindowContext windowContext,
			PropertyWidgetFactory propertyWidgetFactory, AnalyzerBeansConfiguration configuration) {
		super(transformerJobBuilder, windowContext, propertyWidgetFactory, configuration);
	}

	@Override
	protected JComponent decorate(final DCPanel panel) {
		JComponent result = super.decorate(panel);

		JPanel outerPanel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("Hello DataCleaner users! This label was drawn by our extension!");
		label.setOpaque(false);
		label.setBorder(new EmptyBorder(20, 20, 20, 20));

		outerPanel.add(label, BorderLayout.NORTH);
		outerPanel.add(result, BorderLayout.CENTER);
		return outerPanel;
	}
}
