package org.eobjects.datacleaner.widgets.builder;

import java.awt.Dimension;

import javax.swing.JComponent;

public class WidgetBuilder<E extends JComponent> {

	protected E _component;

	public WidgetBuilder(E component) {
		_component = component;
	}

	public E toComponent() {
		return _component;
	}

	public WidgetBuilder<E> applySize(Dimension d) {
		_component.setSize(d);
		_component.setPreferredSize(d);
		return this;
	}

	public WidgetBuilder<E> applySize(Integer width, Integer height) {
		Dimension d = new Dimension();
		if (width != null) {
			d.width = width;
		}
		if (height != null) {
			d.height = height;
		}
		return applySize(d);
	}

	public WidgetBuilder<E> applyTooltip(String text) {
		_component.setToolTipText(text);
		return this;
	}
}