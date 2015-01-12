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

import java.awt.BorderLayout;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JToolTip;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.tooltip.DCToolTip;

public final class HelpIcon extends JLabel {

	private static final long serialVersionUID = 1L;

	private static final ImageManager imageManager = ImageManager.get();
	private final String _helpMessage;
	private final Icon _tooltipIcon;

	public HelpIcon(String helpMessage, Icon tooltipIcon) {
		super(imageManager.getImageIcon("images/widgets/help.png", IconUtils.ICON_SIZE_SMALL));
		_tooltipIcon = tooltipIcon;
		_helpMessage = helpMessage;
		ToolTipManager.sharedInstance().registerComponent(this);
	}

	public HelpIcon(String helpMessage) {
		this(helpMessage, imageManager.getImageIcon("images/widgets/help.png"));
	}

	@Override
	public String getToolTipText() {
		return _helpMessage;
	}

	@Override
	public JToolTip createToolTip() {
		DCPanel panel = new DCPanel();
		panel.setOpaque(true);
		panel.setBackground(WidgetUtils.BG_COLOR_DARK);

		panel.setLayout(new BorderLayout());
		panel.add(new JLabel(_tooltipIcon), BorderLayout.WEST);

		DCLabel descriptionLabel = DCLabel.brightMultiLine(_helpMessage);
		panel.add(descriptionLabel, BorderLayout.CENTER);

		Border border = new CompoundBorder(WidgetUtils.BORDER_THIN, WidgetUtils.BORDER_EMPTY);
		panel.setBorder(border);

		panel.setPreferredSize(300, 130);

		return new DCToolTip(this, panel);
	}
}
