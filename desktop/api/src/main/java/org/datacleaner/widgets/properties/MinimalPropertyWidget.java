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
package org.datacleaner.widgets.properties;

import java.awt.event.ActionListener;
import java.util.Map;

import org.apache.metamodel.util.EqualsBuilder;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimalistic abstract implementation of the {@link PropertyWidget} interface.
 *
 * @param <E>
 */
public abstract class MinimalPropertyWidget<E> implements PropertyWidget<E> {

    private static final Logger logger = LoggerFactory.getLogger(MinimalPropertyWidget.class);

    private final ComponentBuilder _componentBuilder;
    private final ConfiguredPropertyDescriptor _propertyDescriptor;

    private transient int _updating;

    public MinimalPropertyWidget(final ComponentBuilder componentBuilder,
            final ConfiguredPropertyDescriptor propertyDescriptor) {
        _componentBuilder = componentBuilder;
        _propertyDescriptor = propertyDescriptor;
        _updating = 0;
    }

    @Override
    public void initialize(final E value) {
    }

    @Override
    public final ConfiguredPropertyDescriptor getPropertyDescriptor() {
        return _propertyDescriptor;
    }

    public final ComponentBuilder getComponentBuilder() {
        return _componentBuilder;
    }

    @Override
    public boolean isSet() {
        return getValue() != null;
    }

    public final void fireValueChanged() {
        final E value = getValue();
        fireValueChanged(value);
    }

    protected ActionListener fireValueChangedActionListener() {
        return e -> fireValueChanged();
    }

    protected final AnalysisJobBuilder getAnalysisJobBuilder() {
        return getComponentBuilder().getAnalysisJobBuilder();
    }

    @Override
    public void onValueTouched(final E value) {
        if (isUpdating()) {
            // prevent update loops from a widget's own configuration change
            // notification
            return;
        }
        final E existingValue = getValue();
        if (EqualsBuilder.equals(value, existingValue)) {
            return;
        }
        setValue(value);
    }

    /**
     * Determines whether a property widget is currently updating/setting it's
     * property value. When true, this property will not treat incoming
     * notifications, since they will be triggered by itself.
     *
     * @return
     */
    protected boolean isUpdating() {
        return _updating > 0;
    }

    /**
     * Provides a method for setting the "updating" flag (see
     * {@link #isUpdating()}).
     *
     * @param updating
     */
    protected void setUpdating(final boolean updating) {
        if (updating) {
            _updating++;
        } else {
            assert _updating > 0;
            _updating--;
        }
    }

    /**
     * Gets the current value of the property in the job builder.
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    protected E getCurrentValue() {
        return (E) getComponentBuilder().getConfiguredProperty(getPropertyDescriptor());
    }

    protected abstract void setValue(E value);

    protected final void fireValueChanged(final Object newValue) {
        setUpdating(true);
        try {
            _componentBuilder.setConfiguredProperty(_propertyDescriptor, newValue);
        } catch (final Exception e) {
            // an exception will be thrown here if setting an invalid property
            // value (which may just be work in progress, so we don't make a
            // fuzz about it)
            if (logger.isWarnEnabled()) {
                logger.warn(
                        "Unexpected exception when setting property: " + _propertyDescriptor + ": " + e.getMessage(),
                        e);
            }
        } finally {
            setUpdating(false);
        }
    }

    protected final void fireValuesChanged(final Map<ConfiguredPropertyDescriptor, Object> properties) {
        // TODO: For now we accept the code duplication with fireValueChanged above, since
        // escalateToMultipleJobs requires setConfiguredProperty as it overrides it.
        setUpdating(true);
        try {
            _componentBuilder.setConfiguredProperties(properties);
        } catch (final Exception e) {
            // an exception will be thrown here if setting an invalid property
            // value (which may just be work in progress, so we don't make a
            // fuzz about it)
            if (logger.isWarnEnabled()) {
                logger.warn(
                        "Unexpected exception when setting property: " + _propertyDescriptor + ": " + e.getMessage(),
                        e);
            }
        } finally {
            setUpdating(false);
        }
    }
}
