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
package org.datacleaner.widgets;

import java.awt.Dimension;
import java.awt.GridBagConstraints;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JToolTip;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.datacleaner.descriptors.BeanDescriptor;
import org.datacleaner.util.StringUtils;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.tooltip.DCToolTip;

/**
 * MenuItem for a component descriptor.
 */
public class DescriptorMenuItem extends JMenuItem {

	private static final long serialVersionUID = 1L;

	private final BeanDescriptor<?> _descriptor;

	public DescriptorMenuItem(BeanDescriptor<?> descriptor) {
		super(descriptor.getDisplayName());
		_descriptor = descriptor;
		ToolTipManager.sharedInstance().registerComponent(this);
	}

	@Override
	public Icon getIcon() {
		return IconUtils.getDescriptorIcon(_descriptor);
	}

	@Override
	public String getToolTipText() {
		return _descriptor.toString();
	}

	@Override
	public JToolTip createToolTip() {
		JToolTip toolTip = new DCToolTip(this, createToolTipPanel());
		return toolTip;
	}

	protected JComponent createToolTipPanel() {
		DCPanel panel = new DCPanel();
		panel.setOpaque(true);
		panel.setBackground(WidgetUtils.BG_COLOR_DARK);

		JLabel iconLabel = new JLabel(IconUtils.getDescriptorIcon(_descriptor,
				IconUtils.ICON_SIZE_LARGE));
		iconLabel.setBorder(new EmptyBorder(0, 0, 0, 10));
		iconLabel.setOpaque(false);

		JLabel nameLabel = new JLabel(_descriptor.getDisplayName());
		nameLabel.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
		nameLabel.setOpaque(false);
		nameLabel.setFont(WidgetUtils.FONT_HEADER1);

		// if the bean has a description, add it in the CENTER of the tooltip
		String description = _descriptor.getDescription();
		if (StringUtils.isNullOrEmpty(description)) {

			WidgetUtils.addToGridBag(iconLabel, panel, 0, 0);
			WidgetUtils.addToGridBag(nameLabel, panel, 1, 0);

		} else {
			String[] lines = description.split("\n");

			WidgetUtils.addToGridBag(iconLabel, panel, 0, 0, 1,
					lines.length + 1, GridBagConstraints.WEST);
			WidgetUtils.addToGridBag(nameLabel, panel, 1, 0);

			int width = 0;
			int height = 0;

			for (int i = 0; i < lines.length; i++) {
				String line = lines[i];

				DCLabel label = DCLabel.brightMultiLine(line);
				label.setMaximumWidth(350);

				Dimension ps = label.getPreferredSize();
				height += ps.height + 8;
				width = Math.max(ps.width, width);

				WidgetUtils.addToGridBag(label, panel, 1, i + 1);
			}

			width += iconLabel.getPreferredSize().width + 30;
			height += nameLabel.getPreferredSize().height + 30;

			panel.setPreferredSize(new Dimension(width, height));
		}

		Border border = new CompoundBorder(WidgetUtils.BORDER_THIN,
				WidgetUtils.BORDER_EMPTY);
		panel.setBorder(border);
		return panel;
	}
}
