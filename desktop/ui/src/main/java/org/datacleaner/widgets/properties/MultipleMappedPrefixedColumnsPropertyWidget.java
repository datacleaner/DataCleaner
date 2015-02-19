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
package org.datacleaner.widgets.properties;

import java.awt.BorderLayout;

import javax.swing.JComponent;

import org.datacleaner.api.InputColumn;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.SourceColumnComboBox;

/**
 * A simple subclass of {@link MultipleMappedColumnsPropertyWidget} which just
 * adds a string label in front of all source column selection boxes.
 */
public class MultipleMappedPrefixedColumnsPropertyWidget extends MultipleMappedColumnsPropertyWidget {

    private final String _prefix;

    public MultipleMappedPrefixedColumnsPropertyWidget(ComponentBuilder componentBuilder,
            ConfiguredPropertyDescriptor inputColumnsProperty, ConfiguredPropertyDescriptor mappedColumnsProperty,
            String prefix) {
        super(componentBuilder, inputColumnsProperty, mappedColumnsProperty);
        _prefix = prefix;
    }

    @Override
    protected JComponent decorateSourceColumnComboBox(InputColumn<?> inputColumn,
            SourceColumnComboBox sourceColumnComboBox) {
        final DCLabel label = DCLabel.dark(_prefix);
        label.setFont(WidgetUtils.FONT_MONOSPACE);

        final DCPanel panel = new DCPanel();
        panel.setLayout(new BorderLayout());
        panel.add(label, BorderLayout.WEST);
        panel.add(sourceColumnComboBox, BorderLayout.CENTER);
        return panel;
    }
}
