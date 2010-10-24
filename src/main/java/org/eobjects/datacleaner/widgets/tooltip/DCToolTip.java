package org.eobjects.datacleaner.widgets.tooltip;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JToolTip;

public class DCToolTip extends JToolTip {

	private static final long serialVersionUID = 1L;
	private final JComponent _tooltipComponent;

	public DCToolTip(JComponent owner, JComponent tooltipComponent) {
		super();
		_tooltipComponent = tooltipComponent;
		setComponent(owner);
	}

	public JComponent getTooltipComponent() {
		return _tooltipComponent;
	}

	@Override
	public void paint(Graphics g) {
		System.out.println("tooltip.paint");

		g.setColor(Color.RED);
		g.fillRect(0, 0, 100, 100);
	}
}
