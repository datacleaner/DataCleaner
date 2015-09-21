/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
 * <p>
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 * <p>
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
 * @Since 21.9.15
 */
public class AnnotationProxy {

    public static <A extends Annotation> A newAnnotation(Class<A> anClass, Map<String, Object> properties) {
        return (A)Proxy.newProxyInstance(AnnotationProxy.class.getClassLoader(), new Class[] {anClass}, new InvHandler(properties));
    }

    private static class InvHandler implements InvocationHandler {
        Map<String, Object> properties;
        public InvHandler(Map<String, Object> properties) {
            this.properties = properties;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object result = properties.get(method.getName());
            if(result != null) {
                Class<?> retType = method.getReturnType();
                if(retType.isArray()) {
                    if(List.class.isAssignableFrom(result.getClass())) {
                        return ((List)result).toArray((Object[]) Array.newInstance(retType.getComponentType(), ((List) result).size()));
                    }
                    return result;
                }
            }
            return result;
        }
    }
}
