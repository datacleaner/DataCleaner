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
package org.eobjects.datacleaner.sample.ui;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.panels.TransformerJobBuilderPanel;
import org.eobjects.datacleaner.panels.TransformerJobBuilderPresenter;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;

public class HelloWorldTransformerPresenter extends TransformerJobBuilderPanel implements TransformerJobBuilderPresenter {

	private static final long serialVersionUID = 1L;

	public HelloWorldTransformerPresenter(TransformerJobBuilder<?> transformerJobBuilder, WindowContext windowContext, PropertyWidgetFactory propertyWidgetFactory) {
		super(transformerJobBuilder, windowContext, propertyWidgetFactory);
	}

	@Override
	public JComponent createJComponent() {
		// delegate to the parent implementation
		JComponent parentComponent = super.createJComponent();

		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("Hello DataCleaner users! This label was drawn by our extension!");
		label.setOpaque(false);
		label.setBorder(new EmptyBorder(20, 20, 20, 20));

		panel.add(label, BorderLayout.NORTH);
		panel.add(parentComponent, BorderLayout.CENTER);
		return panel;
	}
}
