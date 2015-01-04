/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.descriptors;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eobjects.analyzer.util.ReflectionUtils;

/**
 * Abstract implementation of {@link PropertyDescriptor}. Convenient when
 * implementing sub-interfaces such as {@link ConfiguredPropertyDescriptor} and
 * {@link ProvidedPropertyDescriptor}.
 * 
 * 
 */
public abstract class AbstractPropertyDescriptor implements PropertyDescriptor {

	private static final long serialVersionUID = 1L;

	private final transient Field _field;
	private final transient Type _genericType;
	private final String _name;
	private final Class<?> _baseType;
	private final ComponentDescriptor<?> _componentDescriptor;

	public AbstractPropertyDescriptor(Field field, ComponentDescriptor<?> componentDescriptor) {
		if (field == null) {
			throw new IllegalArgumentException("field cannot be null");
		}
		_field = field;
		_field.setAccessible(true);
		_name = _field.getName();
		_baseType = _field.getType();
		_genericType = _field.getGenericType();
		_componentDescriptor = componentDescriptor;
	}

	public Field getField() {
		if (_field == null) {
			return ReflectionUtils.getField(_componentDescriptor.getComponentClass(), _name);
		}
		return _field;
	}
	
	public Type getGenericType() {
		if (_genericType == null) {
			return getField().getGenericType();
		}
		return _genericType;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public void setValue(Object component, Object value) throws IllegalArgumentException {
		try {
			getField().set(component, value);
		} catch (Exception e) {
			throw new IllegalStateException("Could not assign value '" + value + "' to " + _field, e);
		}
	}

	@Override
	public Object getValue(Object bean) throws IllegalArgumentException {
		if (bean == null) {
			throw new IllegalArgumentException("bean cannot be null");
		}
		try {
			Field field = getField();
            return field.get(bean);
		} catch (Exception e) {
		    if (e instanceof RuntimeException) {
		        throw (RuntimeException) e;
		    }
			throw new IllegalArgumentException("Could not retrieve property '" + getName() + "' from bean: " + bean, e);
		}
	}

	@Override
	public Set<Annotation> getAnnotations() {
		Annotation[] annotations = getField().getAnnotations();
		return new HashSet<Annotation>(Arrays.asList(annotations));
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
		return ReflectionUtils.getAnnotation(getField(), annotationClass);
	}

	@Override
	public int getTypeArgumentCount() {
		return ReflectionUtils.getTypeParameterCount(getGenericType());
	}

	@Override
	public Class<?> getTypeArgument(int i) throws IndexOutOfBoundsException {
		return ReflectionUtils.getTypeParameter(getField(), i);
	}

	@Override
	public Class<?> getBaseType() {
		if (_baseType.isArray()) {
			return _baseType.getComponentType();
		}
		return _baseType;
	}

	@Override
	public boolean isArray() {
		return _baseType.isArray();
	}

	@Override
	public Class<?> getType() {
		return _baseType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_name == null) ? 0 : _name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractPropertyDescriptor other = (AbstractPropertyDescriptor) obj;
		if (_name == null) {
			if (other._name != null)
				return false;
		} else if (!_name.equals(other._name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[field=" + _name + ",baseType=" + _baseType + "]";
	}

	@Override
	public int compareTo(PropertyDescriptor o) {
		if (o == null) {
			return 1;
		}
		if (o == this) {
			return 0;
		}
		int result = getName().compareTo(o.getName());
		if (result == 0) {
			result = getComponentDescriptor().compareTo(o.getComponentDescriptor());
		}
		if (result == 0) {
			result = getType().hashCode() - o.getType().hashCode();
		}
		return result;
	}

	@Override
	public ComponentDescriptor<?> getComponentDescriptor() {
		return _componentDescriptor;
	}
}
