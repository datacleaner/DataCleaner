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

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

/**
 * This tricky class allows us to create new annotations dynamically. Java api
 * doesn't provide anything like this, annotations can be created either via
 * source code, or specific class can be defined. But to create a dynamic
 * annotation of specific interface by reflection, that is not available in
 * standard java api.
 *
 * @Since 21.9.15
 */
public class AnnotationProxy {

    /**
     * Factory method for annotations which is able to create an instance of any
     * annotation class.
     *
     * @param anClass
     *            Annotation interface that should be instantiated
     * @param properties
     *            map of values for the annotation. The name (key) of a property
     *            is interpreted as a name of a method from the annotation
     *            interface. The created annotation proxy then returns this
     *            value when this method is called.
     * @param <A>
     * @return An annotation instance providing given values by its interface
     *         methods.
     */
    public static <A extends Annotation> A newAnnotation(Class<A> anClass, Map<String, Object> properties) {
        final InvHandler handler = new InvHandler(properties);
        ClassLoader classLoader = AnnotationProxy.class.getClassLoader();
        @SuppressWarnings("unchecked")
        final A proxy = (A) Proxy.newProxyInstance(classLoader, new Class[] { anClass }, handler);
        return proxy;
    }

    private static class InvHandler implements InvocationHandler {
        Map<String, Object> properties;

        public InvHandler(Map<String, Object> properties) {
            this.properties = properties;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object result = properties.get(method.getName());
            if (result != null) {
                Class<?> retType = method.getReturnType();
                if (retType.isArray()) {
                    if (List.class.isAssignableFrom(result.getClass())) {
                        return ((List<?>) result).toArray(
                                (Object[]) Array.newInstance(retType.getComponentType(), ((List<?>) result).size()));
                    }
                    return result;
                }
            }
            return result;
        }
    }
}
