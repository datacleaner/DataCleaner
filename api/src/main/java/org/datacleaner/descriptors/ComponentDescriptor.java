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
import java.util.Set;

import org.datacleaner.api.Alias;
import org.datacleaner.api.Close;
import org.datacleaner.api.Component;
import org.datacleaner.api.ComponentCategory;
import org.datacleaner.api.ComponentSuperCategory;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Distributed;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.Provided;
import org.datacleaner.api.Validate;

/**
 * Defines an interface for descriptors of {@link Component}s that support
 * initialization, closing and configuration properties. See {@link Component}
 * for a general description.
 */
public interface ComponentDescriptor<B> extends Comparable<ComponentDescriptor<?>>, Serializable {

    /**
     * @return a humanly readable display name for this bean.
     */
    public String getDisplayName();

    /**
     * Constructs an instance of this component
     * 
     * @return a new (uninitialized) instance of the component.
     */
    public B newInstance();

    /**
     * Gets the component's class
     * 
     * @return the component's class
     */
    public Class<B> getComponentClass();

    /**
     * Gets all configuration properties of the component
     * 
     * @see Configured
     * 
     * @return a set of all properties
     */
    public Set<ConfiguredPropertyDescriptor> getConfiguredProperties();

    /**
     * Determines if the bean is a distributable component or not.
     * 
     * @return true if the component can be distributed.
     * 
     * @see Distributed
     */
    public boolean isDistributable();

    /**
     * Gets the configured properties that have {@link InputColumn} type.
     * 
     * @return a set containing all configured property descriptors of
     *         {@link InputColumn}s in the bean.
     */
    public Set<ConfiguredPropertyDescriptor> getConfiguredPropertiesForInput();

    /**
     * Gets the configured properties that have {@link InputColumn} type.
     * 
     * @param includeOptional
     *            a boolean indicating if optional properties should be
     *            returned. If false, only required properties will be included.
     * @return a set containing all configured property descriptors of
     *         {@link InputColumn}s in the bean.
     */
    public Set<ConfiguredPropertyDescriptor> getConfiguredPropertiesForInput(boolean includeOptional);

    /**
     * Gets an annotation of a specific type
     * 
     * @param <A>
     * @param annotationClass
     * @return an annotation of the specified type, or null of no such
     *         annotation exists.
     */
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass);

    /**
     * @return a humanly readable description of the component.
     */
    public String getDescription();

    /**
     * @return a set of component categories that the component has been
     *         assigned to.
     */
    public Set<ComponentCategory> getComponentCategories();

    /**
     * @return the {@link ComponentSuperCategory} that this component pertains
     *         to.
     */
    public ComponentSuperCategory getComponentSuperCategory();

    /**
     * @return a set of annotations for the component.
     */
    public Set<Annotation> getAnnotations();

    /**
     * Gets all configuration properties with a particular annotation.
     * 
     * @param annotation
     *            the annotation type to look for.
     * @return a set of properties that match the specified annotation query
     */
    public Set<ConfiguredPropertyDescriptor> getConfiguredPropertiesByAnnotation(Class<? extends Annotation> annotation);

    /**
     * Gets all configuration properties of a particular type (including
     * subtypes)
     * 
     * @see Configured
     * 
     * @param type
     *            the type of property to look for
     * @param includeArrays
     *            a boolean indicating whether or not configuration properties
     *            that are arrays of the provided type should be included
     * @return a set of properties that match the specified type query
     */
    public Set<ConfiguredPropertyDescriptor> getConfiguredPropertiesByType(Class<?> type, boolean includeArrays);

    /**
     * Gets a configured property by name
     * 
     * @see Configured
     * 
     * @param name
     *            the name of the property
     * @return a configured property, or null if no such property exists
     */
    public ConfiguredPropertyDescriptor getConfiguredProperty(String name);

    /**
     * Gets the validation methods of the component
     * 
     * @see Validate
     * 
     * @return a set of validate method descriptors
     */
    public Set<ValidateMethodDescriptor> getValidateMethods();

    /**
     * Gets the initialize methods of the component
     * 
     * @see Initialize
     * 
     * @return a set of initialize method descriptors
     */
    public Set<InitializeMethodDescriptor> getInitializeMethods();

    /**
     * Gets the close methods of the component
     * 
     * @see Close
     * 
     * @return a set of close method descriptors
     */
    public Set<CloseMethodDescriptor> getCloseMethods();

    /**
     * Gets the provided properties of the component
     * 
     * @see Provided
     * 
     * @return a set of provided properties.
     */
    public Set<ProvidedPropertyDescriptor> getProvidedProperties();

    /**
     * Gets the provided properties of a particular type in the component
     * 
     * @see Provided
     * 
     * @param cls
     *            the type of the provided properties
     * @return a set of provided properties.
     */
    public Set<ProvidedPropertyDescriptor> getProvidedPropertiesByType(Class<?> cls);

    /**
     * Gets all known aliases of this component.
     * 
     * @see Alias
     * 
     * @return an array of aliases as strings
     */
    public String[] getAliases();
}
