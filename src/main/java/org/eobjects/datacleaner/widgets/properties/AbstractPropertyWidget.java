package org.eobjects.datacleaner.widgets.properties;

import java.awt.GridLayout;

import javax.swing.JComponent;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.util.CompareUtils;
import org.eobjects.datacleaner.panels.DCPanel;

public abstract class AbstractPropertyWidget<E> extends DCPanel implements PropertyWidget<E> {

	private static final long serialVersionUID = 1L;

	private final AbstractBeanJobBuilder<?, ?, ?> _beanJobBuilder;
	private final ConfiguredPropertyDescriptor _propertyDescriptor;

	public AbstractPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			ConfiguredPropertyDescriptor propertyDescriptor) {
		super();
		_beanJobBuilder = beanJobBuilder;
		_propertyDescriptor = propertyDescriptor;
		setLayout(new GridLayout(1, 1));
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

	protected final void fireValueChanged() {
		fireValueChanged(getValue());
	}

	protected final void fireValueChanged(Object newValue) {
		_beanJobBuilder.setConfiguredProperty(_propertyDescriptor, newValue);
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
	}

	public void onValueTouched(E value) {
		E existingValue = getValue();
		if (CompareUtils.equals(value, existingValue)) {
			return;
		}
		setValue(value);
	}

	protected abstract void setValue(E value);
}
