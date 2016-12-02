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
package org.datacleaner.descriptors;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.metamodel.util.BaseObject;
import org.datacleaner.util.ReflectionUtils;

/**
 * Abstract descriptor for things that are represented by a method on a
 * component class.
 */
class AbstractMethodDescriptor extends BaseObject implements Serializable {

    private static final long serialVersionUID = 1L;

    private final transient Method _method;
    private final ComponentDescriptor<?> _componentDescriptor;
    private final String _name;

    AbstractMethodDescriptor(final Method method, final ComponentDescriptor<?> componentDescriptor) {
        if (method.getReturnType() != void.class) {
            throw new DescriptorException("Method can only be void");
        }
        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length > 0) {
            throw new DescriptorException("Method cannot have parameters");
        }

        _method = method;
        _method.setAccessible(true);
        _name = method.getName();
        _componentDescriptor = componentDescriptor;
    }

    public ComponentDescriptor<?> getComponentDescriptor() {
        return _componentDescriptor;
    }

    public final Method getMethod() {
        if (_method == null) {
            return ReflectionUtils.getMethod(_componentDescriptor.getComponentClass(), _name);
        }
        return _method;
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() + "[method=" + _name + "]";
    }

    protected final void invoke(final Object component) throws RuntimeException, IllegalStateException {
        try {
            _method.invoke(component);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final InvocationTargetException e) {
            throw convertThrownException(component, e);
        } catch (final Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new IllegalStateException("Could not invoke method " + getMethod(), e);
        }
    }

    protected RuntimeException convertThrownException(final Object component, final InvocationTargetException e) {
        final Throwable targetException = e.getTargetException();
        if (targetException instanceof RuntimeException) {
            throw (RuntimeException) targetException;
        }
        throw new RuntimeException(targetException);
    }

    public final Set<Annotation> getAnnotations() {
        final Annotation[] annotations = getMethod().getAnnotations();
        return new HashSet<>(Arrays.asList(annotations));
    }

    public final <A extends Annotation> A getAnnotation(final Class<A> annotationClass) {
        return ReflectionUtils.getAnnotation(getMethod(), annotationClass);
    }

    @Override
    protected void decorateIdentity(final List<Object> list) {
        list.add(getMethod());
    }
}
