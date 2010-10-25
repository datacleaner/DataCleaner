package org.eobjects.datacleaner.widgets.properties;

import java.awt.GridLayout;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.datacleaner.panels.DCPanel;

public abstract class AbstractPropertyWidget<E> extends DCPanel implements PropertyWidget<E> {

	private static final long serialVersionUID = 1L;

	private final List<PropertyWidgetListener> _listeners = new LinkedList<PropertyWidgetListener>();
	private final ConfiguredPropertyDescriptor _propertyDescriptor;

	public AbstractPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor) {
		super();
		setLayout(new GridLayout(1, 1));
		_propertyDescriptor = propertyDescriptor;
	}

	@Override
	public final ConfiguredPropertyDescriptor getPropertyDescriptor() {
		return _propertyDescriptor;
	}

	@Override
	public boolean isSet() {
		return getValue() != null;
	}

	@Override
	public final JComponent getWidget() {
		return this;
	}

	@Override
	public final void addListener(PropertyWidgetListener listener) {
		_listeners.add(listener);

		// Do an initial invocation of the value changed command. This will make
		// inform the listener of the current value as well as make sure that
		// the any initialization of the value in the constructor of the
		// PropertyWidget is recorded.
		listener.onValueChanged(this, getValue());
	}

	@Override
	public final void removeListener(PropertyWidgetListener listener) {
		_listeners.remove(listener);
	}

	protected final void fireValueChanged() {
		fireValueChanged(getValue());
	}

	protected final void fireValueChanged(Object newValue) {
		for (PropertyWidgetListener listener : _listeners) {
			listener.onValueChanged(this, newValue);
		}
	}
}
