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

import org.datacleaner.api.Converter;
import org.datacleaner.api.InputColumn;
import org.datacleaner.components.remote.RemoteTransformer;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @Since 9/9/15
 */
public class TypeBasedConfiguredPropertyDescriptorImpl implements ConfiguredPropertyDescriptor {

    private Class type;
    private String name;
    private String description;
    private boolean required;
    private ComponentDescriptor component;
    private Set<Annotation> annotations = Collections.EMPTY_SET;

    public TypeBasedConfiguredPropertyDescriptorImpl(String name, String description, Class type, boolean required, ComponentDescriptor component) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.required = required;
        this.component = component;
    }

    @Override
    public boolean isInputColumn() {
        return InputColumn.class.isAssignableFrom(getBaseType());
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Override
    public Class<? extends Converter<?>> getCustomConverter() {
        return null;
    }

    @Override
    public String[] getAliases() {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setValue(Object component, Object value) throws IllegalArgumentException {
        ((RemoteTransformer)component).setPropertyValue(getName(), value);
    }

    @Override
    public Object getValue(Object component) throws IllegalArgumentException {
        return ((RemoteTransformer)component).getPropertyValue(getName());
    }

    @Override
    public Set<Annotation> getAnnotations() {
        return annotations;
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        for(Annotation an: annotations) {
            if(annotationClass.isAssignableFrom(an.getClass())) {
                return (A)an;
            }
        }
        return null;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public boolean isArray() {
        return type.isArray();
    }

    @Override
    public Class<?> getBaseType() {
        if (type.isArray()) {
            return type.getComponentType();
        }
        return type;
    }

    @Override
    public ComponentDescriptor<?> getComponentDescriptor() {
        return component;
    }

    @Override
    public int getTypeArgumentCount() {
        return 0;
    }

    @Override
    public Class<?> getTypeArgument(int i) throws IndexOutOfBoundsException {
        return null;
    }

    @Override
    public int compareTo(PropertyDescriptor o) {
        return getName().compareTo(o.getName());
    }

    public TypeBasedConfiguredPropertyDescriptorImpl withAnnotation(Annotation an) {
        if(annotations == Collections.EMPTY_SET) {
            annotations = new HashSet<>();
        }
        annotations.add(an);
        return this;
    }
}
