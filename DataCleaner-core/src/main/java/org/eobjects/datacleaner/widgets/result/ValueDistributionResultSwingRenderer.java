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
import java.util.Collection;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;

import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.beans.valuedist.ValueCount;
import org.eobjects.analyzer.result.ValueDistributionGroupResult;
import org.eobjects.analyzer.result.ValueDistributionResult;
import org.eobjects.analyzer.result.renderer.AbstractRenderer;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.LabelUtils;
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
			return delegate.renderGroupResult(result.getSingleValueDistributionResult());
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
			final JComponent renderedResult = delegate.renderGroupResult(res);
			final DCPanel decoratedPanel = createDecoration(renderedResult);
			final String label = "Value distribution for group '" + LabelUtils.getLabel(res.getGroupName()) + "'";

			final ValueCount distinctValue = getDistinctValueCount(res);
			final DCCollapsiblePanel collapsiblePanel;
			if (distinctValue == null) {
				collapsiblePanel = new DCCollapsiblePanel(label, label, false, decoratedPanel);
			} else {
				final String collapsedLabel = label + ": " + LabelUtils.getLabel(distinctValue.getValue()) + "="
						+ distinctValue.getCount() + "";
				collapsiblePanel = new DCCollapsiblePanel(collapsedLabel, label, true, decoratedPanel);
			}
			panel.add(collapsiblePanel.toPanel());
		}
		return panel;
	}

	/**
	 * Determines if a group result has just a single distinct value count. If
	 * so, this value count is returned, or else null is returned.
	 * 
	 * @param res
	 * @return
	 */
	private ValueCount getDistinctValueCount(ValueDistributionGroupResult res) {
		int distinctValueCounter = 0;
		ValueCount valueCount = null;
		if (res.getNullCount() > 0) {
			distinctValueCounter++;
			valueCount = new ValueCount(LabelUtils.NULL_LABEL, res.getNullCount());
		}
		int uniqueCount = res.getUniqueCount();
		if (uniqueCount > 0) {
			if (uniqueCount > 1) {
				// more than one distinct value
				return null;
			}
			distinctValueCounter++;
			final Collection<String> uniqueValues = res.getUniqueValues();
			String label = LabelUtils.UNIQUE_LABEL;
			if (!uniqueValues.isEmpty()) {
				label = uniqueValues.iterator().next();
			}
			valueCount = new ValueCount(label, 1);
		}
		if (distinctValueCounter > 1) {
			// more than one distinct value
			return null;
		}
		if (res.getTopValues().getActualSize() > 0) {
			distinctValueCounter += res.getTopValues().getActualSize();
			valueCount = res.getTopValues().getValueCounts().get(0);
		}
		if (res.getBottomValues().getActualSize() > 0) {
			distinctValueCounter += res.getBottomValues().getActualSize();
			valueCount = res.getBottomValues().getValueCounts().get(0);
		}
		if (distinctValueCounter > 1) {
			// more than one distinct value
			return null;
		}
		return valueCount;
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
