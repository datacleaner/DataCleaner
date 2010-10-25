package org.eobjects.datacleaner.widgets.tooltip;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JTextArea;
import javax.swing.JToolTip;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.WidgetUtils;

public class DescriptorMenuItem extends JMenuItem {

	private static final long serialVersionUID = 1L;

	private final BeanDescriptor<?> _descriptor;

	public DescriptorMenuItem(BeanDescriptor<?> descriptor) {
		super(descriptor.getDisplayName(), IconUtils.getDescriptorIcon(descriptor, IconUtils.ICON_SIZE_SMALL));
		_descriptor = descriptor;
		ToolTipManager.sharedInstance().registerComponent(this);
	}

	@Override
	public String getToolTipText() {
		return _descriptor.toString();
	}

	@Override
	public JToolTip createToolTip() {
		DCPanel panel = new DCPanel();
		panel.setOpaque(true);
		panel.setBackground(WidgetUtils.BG_COLOR_DARK);

		JLabel iconLabel = new JLabel(IconUtils.getDescriptorIcon(_descriptor));
		iconLabel.setOpaque(false);

		JLabel nameLabel = new JLabel(_descriptor.getDisplayName());
		nameLabel.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
		nameLabel.setOpaque(false);
		nameLabel.setFont(WidgetUtils.FONT_HEADER);

		// if the bean has a description, add it in the CENTER of the tooltip
		String description = _descriptor.getDescription();
		if (StringUtils.isNullOrEmpty(description)) {

			WidgetUtils.addToGridBag(iconLabel, panel, 0, 0);
			WidgetUtils.addToGridBag(nameLabel, panel, 1, 0);

		} else {
			String[] lines = description.split("\n");

			WidgetUtils.addToGridBag(iconLabel, panel, 0, 0, 1, lines.length + 1, GridBagConstraints.WEST);
			WidgetUtils.addToGridBag(nameLabel, panel, 1, 0);

			int width = 0;
			int height = 0;

			for (int i = 0; i < lines.length; i++) {
				String line = lines[i];
				JTextArea textArea = new JTextArea();
				textArea.setText(line.trim());
				textArea.setEditable(false);
				textArea.setLineWrap(true);
				textArea.setWrapStyleWord(true);
				textArea.setOpaque(false);
				textArea.setBorder(null);
				textArea.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				textArea.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
				textArea.setColumns(30);

				Dimension ps = textArea.getPreferredSize();
				height += ps.height + 8;
				width = Math.max(ps.width, width);

				WidgetUtils.addToGridBag(textArea, panel, 1, i + 1);
			}

			// TODO: Make a more accurate width/height calculation
			width += iconLabel.getPreferredSize().width + 50;
			height += nameLabel.getPreferredSize().height + 50;
			panel.setPreferredSize(new Dimension(width, height));
		}

		Border border = new CompoundBorder(WidgetUtils.BORDER_THIN, WidgetUtils.BORDER_EMPTY);

		panel.setBorder(border);

		JToolTip toolTip = new DCToolTip(this, panel);
		return toolTip;
	}
}
