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
package org.eobjects.datacleaner.widgets.result;

import java.awt.BorderLayout;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;

import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.result.ValueDistributionGroupResult;
import org.eobjects.analyzer.result.ValueDistributionResult;
import org.eobjects.analyzer.result.renderer.AbstractRenderer;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCCollapsiblePanel;
import org.jdesktop.swingx.VerticalLayout;

@RendererBean(SwingRenderingFormat.class)
public class ValueDistributionResultSwingRenderer extends AbstractRenderer<ValueDistributionResult, JComponent> {

	@Override
	public JComponent render(ValueDistributionResult result) {
		if (result.isGroupingEnabled()) {
			return renderGroupedResult(result);
		} else {
			ValueDistributionResultSwingRendererGroupDelegate delegate = new ValueDistributionResultSwingRendererGroupDelegate(
					result.getColumnName());
			return delegate.render(result.getSingleValueDistributionResult());
		}
	}

	public JComponent renderGroupedResult(ValueDistributionResult result) {
		final DCPanel panel = new DCPanel();
		panel.setLayout(new VerticalLayout(0));
		Set<ValueDistributionGroupResult> results = result.getGroupedValueDistributionResults();
		for (ValueDistributionGroupResult res : results) {
			if (panel.getComponentCount() != 0) {
				panel.add(Box.createVerticalStrut(10));
			}
			ValueDistributionResultSwingRendererGroupDelegate delegate = new ValueDistributionResultSwingRendererGroupDelegate(
					res.getGroupName());
			final JComponent renderedResult = delegate.render(res);
			final DCPanel decoratedPanel = createDecoration(renderedResult);
			final String label = "Value distribution for group: " + res.getGroupName();
			final DCCollapsiblePanel collapsiblePanel = new DCCollapsiblePanel(label, label, false, decoratedPanel);
			panel.add(collapsiblePanel.toPanel());
		}
		return panel;
	}

	private DCPanel createDecoration(JComponent renderedResult) {
		renderedResult.setBorder(WidgetUtils.BORDER_SHADOW);
		final DCPanel wrappingPanel = new DCPanel();
		wrappingPanel.setLayout(new BorderLayout());
		wrappingPanel.add(renderedResult, BorderLayout.CENTER);
		wrappingPanel.setBorder(new EmptyBorder(4, 20, 4, 4));
		return wrappingPanel;
	}
}
