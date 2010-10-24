package org.eobjects.datacleaner.widgets.builder;

import javax.swing.Icon;
import javax.swing.JMenuItem;

public class MenuBuilder<E extends JMenuItem> extends WidgetBuilder<E> {

	public MenuBuilder(E component) {
		super(component);
	}

	public MenuBuilder<E> setMnemonic(char m) {
		_component.setMnemonic(m);
		return this;
	}
	
	public MenuBuilder<E> setIcon(Icon icon) {
		_component.setIcon(icon);
		return this;
	}
}
