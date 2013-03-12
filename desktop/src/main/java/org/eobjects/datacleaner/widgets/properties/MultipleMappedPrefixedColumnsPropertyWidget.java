/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.widgets.properties;

import java.awt.BorderLayout;

import javax.swing.JComponent;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.widgets.SourceColumnComboBox;

/**
 * A simple subclass of {@link MultipleMappedColumnsPropertyWidget} which just
 * adds a string label in front of all source column selection boxes.
 */
public class MultipleMappedPrefixedColumnsPropertyWidget extends MultipleMappedColumnsPropertyWidget {

	private final String _prefix;

	public MultipleMappedPrefixedColumnsPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			ConfiguredPropertyDescriptor inputColumnsProperty, ConfiguredPropertyDescriptor mappedColumnsProperty,
			String prefix) {
		super(beanJobBuilder, inputColumnsProperty, mappedColumnsProperty);
		_prefix = prefix;
	}

	@Override
	protected JComponent decorateSourceColumnComboBox(InputColumn<?> inputColumn,
			SourceColumnComboBox sourceColumnComboBox) {
		DCPanel panel = new DCPanel();
		panel.setLayout(new BorderLayout());
		panel.add(DCLabel.dark(_prefix), BorderLayout.WEST);
		panel.add(sourceColumnComboBox, BorderLayout.CENTER);
		return panel;
	}
}
