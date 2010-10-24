package org.eobjects.datacleaner.widgets.tooltip;

import java.awt.BorderLayout;
import java.awt.Cursor;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JTextArea;
import javax.swing.JToolTip;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import org.eobjects.analyzer.descriptors.BeanDescriptor;
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
		panel.setLayout(new BorderLayout());

		JLabel nameLabel = new JLabel(_descriptor.getDisplayName());
		nameLabel.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
		nameLabel.setOpaque(false);
		nameLabel.setFont(WidgetUtils.FONT_HEADER);
		panel.add(nameLabel, BorderLayout.NORTH);

		JLabel iconLabel = new JLabel(IconUtils.getDescriptorIcon(_descriptor));
		iconLabel.setOpaque(false);
		panel.add(iconLabel, BorderLayout.WEST);

		JTextArea descriptionLabel = new JTextArea(
				"Description goes here. Foo bar. Description goes here. Foo bar. Description goes here. Foo bar. Description goes here. Foo bar. Description goes here. Foo bar.");
		descriptionLabel.setEditable(false);
		descriptionLabel.setColumns(30);
		descriptionLabel.setLineWrap(true);
		descriptionLabel.setWrapStyleWord(true);
		descriptionLabel.setOpaque(false);
		descriptionLabel.setBorder(null);
		descriptionLabel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		descriptionLabel.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
		panel.add(descriptionLabel, BorderLayout.CENTER);

		Border border = new CompoundBorder(WidgetUtils.BORDER_THIN, WidgetUtils.BORDER_EMPTY);

		panel.setBorder(border);

		JToolTip toolTip = new DCToolTip(this, panel);
		return toolTip;
	}
}
