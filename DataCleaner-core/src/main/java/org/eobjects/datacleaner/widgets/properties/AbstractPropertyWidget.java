/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.widgets.properties;

import java.awt.GridLayout;

import javax.swing.JComponent;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.metamodel.util.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of the PropertyWidget interface. An implementing
 * class should preferably:
 * 
 * <ul>
 * <li>add(...) a single widget in the constructor.</li>
 * <li>call fireValueChanged() each time the contents/value of the widget has
 * changed.</li>
 * </ul>
 * 
 * @author Kasper Sørensen
 * 
 * @param <E>
 */
public abstract class AbstractPropertyWidget<E> extends DCPanel implements PropertyWidget<E> {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(AbstractPropertyWidget.class);

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

	public final AbstractBeanJobBuilder<?, ?, ?> getBeanJobBuilder() {
		return _beanJobBuilder;
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
		try {
			_beanJobBuilder.setConfiguredProperty(_propertyDescriptor, newValue);
		} catch (Exception e) {
			// an exception will be thrown here if setting an invalid property
			// value (which may just be work in progress, so we don't make a
			// fuzz about it)
			if (logger.isWarnEnabled()) {
				logger.warn("Exception thrown when setting configured property " + _propertyDescriptor, e);
			}
		}
	}

	protected final AnalysisJobBuilder getAnalysisJobBuilder() {
		return _beanJobBuilder.getAnalysisJobBuilder();
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
	}

	public void onValueTouched(E value) {
		E existingValue = getValue();
		if (EqualsBuilder.equals(value, existingValue)) {
			return;
		}
		setValue(value);
	}

	protected abstract void setValue(E value);
}
