package org.eobjects.datacleaner.widgets.tooltip;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.Popup;
import javax.swing.PopupFactory;

public class DCPopupFactory extends PopupFactory {

	@Override
	public Popup getPopup(Component owner, Component contents, int x, int y) throws IllegalArgumentException {
		if (contents instanceof DCToolTip) {
			DCToolTip toolTip = (DCToolTip) contents;
			JComponent tooltipComponent = toolTip.getTooltipComponent();
			return super.getPopup(owner, tooltipComponent, x, y);
		}
		return super.getPopup(owner, contents, x, y);
	}
}
