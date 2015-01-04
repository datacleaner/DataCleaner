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
package org.eobjects.analyzer.lifecycle;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.eobjects.analyzer.configuration.InjectionPoint;

public final class MemberInjectionPoint<T> extends AbstractInjectionPoint<T> implements InjectionPoint<T> {

	private final Object _instance;
	private final Member _member;
	private final int _parameterIndex;

	public MemberInjectionPoint(Field field, Object instance) {
		_instance = instance;
		_member = field;
		_parameterIndex = -1;
	}

	public MemberInjectionPoint(Constructor<?> constructor, int parameterIndex) {
		_instance = null;
		_member = constructor;
		_parameterIndex = parameterIndex;
	}

	public MemberInjectionPoint(Method method, int parameterIndex, Object instance) {
		_instance = instance;
		_member = method;
		_parameterIndex = parameterIndex;
	}

	@Override
	public String toString() {
		return "MemberInjectionPoint [member=" + _member + ", parameterIndex=" + _parameterIndex + ", instance=" + _instance
				+ "]";
	}

	@Override
	public Object getInstance() {
		return _instance;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<T> getBaseType() {
		if (_member instanceof Field) {
			return (Class<T>) ((Field) _member).getType();
		} else if (_member instanceof Method) {
			return (Class<T>) ((Method) _member).getParameterTypes()[_parameterIndex];
		} else if (_member instanceof Constructor) {
			return (Class<T>) ((Constructor<?>) _member).getParameterTypes()[_parameterIndex];
		}
		return null;
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
		if (_member instanceof Field) {
			return ((Field) _member).getAnnotation(annotationClass);
		} else if (_member instanceof Method) {
			return ((Method) _member).getAnnotation(annotationClass);
		} else if (_member instanceof Constructor) {
			return ((Constructor<?>) _member).getAnnotation(annotationClass);
		}
		return null;
	}

	@Override
	protected Type getGenericType() {
		if (_member instanceof Field) {
			return ((Field) _member).getGenericType();
		} else if (_member instanceof Method) {
			return ((Method) _member).getGenericParameterTypes()[_parameterIndex];
		} else if (_member instanceof Constructor) {
			return ((Constructor<?>) _member).getGenericParameterTypes()[_parameterIndex];
		}
		return null;
	}
}
