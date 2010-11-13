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
package org.eobjects.datacleaner.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.analyzer.util.CollectionUtils;
import org.eobjects.datacleaner.util.DisplayNameComparator;
import org.eobjects.datacleaner.widgets.tooltip.DescriptorMenuItem;

public class AddQuickTransformationActionListener implements ActionListener {

	private final JButton _button;
	private final AnalyzerBeansConfiguration _configuration;
	private final AnalysisJobBuilder _analysisJobBuilder;
	private final InputColumn<?> _inputColumn;

	public AddQuickTransformationActionListener(JButton button, AnalyzerBeansConfiguration configuration,
			AnalysisJobBuilder analysisJobBuilder, InputColumn<?> inputColumn) {
		_button = button;
		_configuration = configuration;
		_analysisJobBuilder = analysisJobBuilder;
		_inputColumn = inputColumn;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JPopupMenu popup = new JPopupMenu("Quick transformation");
		Collection<TransformerBeanDescriptor<?>> descriptors = _configuration.getDescriptorProvider()
				.getTransformerBeanDescriptors();
		descriptors = CollectionUtils.sorted(descriptors, new DisplayNameComparator());

		for (TransformerBeanDescriptor<?> descriptor : descriptors) {
			Set<ConfiguredPropertyDescriptor> inputs = descriptor.getConfiguredPropertiesForInput();
			if (inputs.size() == 1) {
				ConfiguredPropertyDescriptor input = inputs.iterator().next();
				if (!input.isArray()) {
					DataTypeFamily currentDataTypeFamily = _inputColumn.getDataTypeFamily();
					DataTypeFamily inputDataTypeFamily = input.getInputColumnDataTypeFamily();
					if (inputDataTypeFamily == DataTypeFamily.UNDEFINED || currentDataTypeFamily == inputDataTypeFamily) {
						JMenuItem menuItem = createMenuItem(descriptor);
						popup.add(menuItem);
					}
				}
			}
		}
		popup.show(_button, 0, _button.getHeight());
	}

	private JMenuItem createMenuItem(final TransformerBeanDescriptor<?> descriptor) {
		JMenuItem menuItem = new DescriptorMenuItem(descriptor);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TransformerJobBuilder<?> tjb = _analysisJobBuilder.addTransformer(descriptor);
				tjb.addInputColumn(_inputColumn);
			}
		});
		return menuItem;
	}
}
