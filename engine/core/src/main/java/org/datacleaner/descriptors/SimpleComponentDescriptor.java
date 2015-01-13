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

import java.io.Closeable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.apache.commons.lang.ArrayUtils;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Close;
import org.datacleaner.api.ComponentCategory;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.Provided;
import org.datacleaner.api.Validate;
import org.datacleaner.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A descriptor for simple components. Simple components covers reference data
 * types (Dictionary, SynonymCatalog, StringPattern) as well as custom
 * configuration components.
 * 
 * Simple components support the @Configured, @Validate, @Initialize and @Close
 * annotations as well as the Closeable interface.
 * 
 * @see Initialize
 * @see Validate
 * @see Close
 * @see Configured
 * @see Closeable
 * 
 * 
 */
class SimpleComponentDescriptor<B> extends AbstractDescriptor<B> implements ComponentDescriptor<B> {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(SimpleComponentDescriptor.class);

    protected final Set<ConfiguredPropertyDescriptor> _configuredProperties;;
    protected final Set<ProvidedPropertyDescriptor> _providedProperties;
    protected final Set<InitializeMethodDescriptor> _initializeMethods;
    protected final Set<ValidateMethodDescriptor> _validateMethods;
    protected final Set<CloseMethodDescriptor> _closeMethods;

    /**
     * Constructor for inheriting from SimpleComponentDescriptor
     * 
     * @param beanClass
     */
    public SimpleComponentDescriptor(Class<B> beanClass) {
        this(beanClass, false);
    }

    public SimpleComponentDescriptor(final Class<B> beanClass, final boolean initialize) {
        super(beanClass);
        _configuredProperties = new TreeSet<ConfiguredPropertyDescriptor>();
        _providedProperties = new TreeSet<ProvidedPropertyDescriptor>();
        _validateMethods = new HashSet<ValidateMethodDescriptor>();
        _initializeMethods = new HashSet<InitializeMethodDescriptor>();
        _closeMethods = new HashSet<CloseMethodDescriptor>();
        if (initialize) {
            visitClass();
        }
    }

    @Override
    public String getDisplayName() {
        return getComponentClass().getSimpleName();
    }

    @Override
    public String getDescription() {
        Description description = getAnnotation(Description.class);
        if (description == null) {
            return null;
        }
        return description.value();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return ReflectionUtils.getAnnotation(getComponentClass(), annotationClass);
    }

    @Override
    public Set<Annotation> getAnnotations() {
        Annotation[] annotations = getComponentClass().getAnnotations();
        return new HashSet<Annotation>(Arrays.asList(annotations));
    }

    @Override
    public Set<ComponentCategory> getComponentCategories() {
        Categorized categorized = getAnnotation(Categorized.class);
        if (categorized == null) {
            return Collections.emptySet();
        }
        Class<? extends ComponentCategory>[] value = categorized.value();
        if (value == null || value.length == 0) {
            return Collections.emptySet();
        }

        Set<ComponentCategory> result = new HashSet<ComponentCategory>();
        for (Class<? extends ComponentCategory> categoryClass : value) {
            ComponentCategory category = ReflectionUtils.newInstance(categoryClass);
            result.add(category);
        }

        return result;
    }

    @Override
    public B newInstance() {
        try {
            return getComponentClass().newInstance();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Could not construct new instance of " + getComponentClass(), e);
        }
    }

    @Override
    protected void visitClass() {
        super.visitClass();

        if (ReflectionUtils.isCloseable(getComponentClass())) {
            try {
                Method method = getComponentClass().getMethod("close", new Class<?>[0]);
                CloseMethodDescriptorImpl cmd = new CloseMethodDescriptorImpl(method, this);
                _closeMethods.add(cmd);
            } catch (Exception e) {
                // This should be impossible since all closeable's have a no-arg
                // close() method
                logger.error("Unexpected exception while getting close() method from Closeable", e);
                assert false;
            }
        }
    }

    @Override
    protected void visitField(Field field) {
        final boolean isInject = ReflectionUtils.isAnnotationPresent(field, Inject.class);
        final boolean isConfigured = ReflectionUtils.isAnnotationPresent(field, Configured.class);
        final boolean isProvided = ReflectionUtils.isAnnotationPresent(field, Provided.class);

        if (isConfigured && isProvided) {
            throw new DescriptorException("The field " + field
                    + " is annotated with both @Configured and @Provided, which are mutually exclusive.");
        }

        if (!isConfigured && (isInject || isProvided)) {
            // provided properties = @Inject or @Provided, and NOT @Configured
            _providedProperties.add(new ProvidedPropertyDescriptorImpl(field, this));
        } else if (isConfigured) {
            if (!isInject) {
                logger.debug("No @Inject annotation found for @Configured field: {}", field);
            }
            ConfiguredPropertyDescriptor cpd = new ConfiguredPropertyDescriptorImpl(field, this);
            _configuredProperties.add(cpd);
        }
    }

    @Override
    protected void visitMethod(Method method) {
        final boolean isInitialize;
        {
            final boolean isInitializeAnnotationPresent = ReflectionUtils.isAnnotationPresent(method, Initialize.class);
            final boolean isPostConstructAnnotationPresent = ReflectionUtils.isAnnotationPresent(method,
                    PostConstruct.class);
            // @PostConstruct is a valid substitution for @Initialize
            isInitialize = isInitializeAnnotationPresent || isPostConstructAnnotationPresent;
        }
        final boolean isClose;
        {
            final boolean isPreDestroyAnnotationPresent = ReflectionUtils.isAnnotationPresent(method, PreDestroy.class);
            final boolean isCloseAnnotationPresent = ReflectionUtils.isAnnotationPresent(method, Close.class);
            // @PreDestroy is a valid substitution for @Close
            isClose = isCloseAnnotationPresent || isPreDestroyAnnotationPresent;
        }
        final boolean isValidate = ReflectionUtils.isAnnotationPresent(method, Validate.class);

        if (isInitialize) {
            _initializeMethods.add(new InitializeMethodDescriptorImpl(method, this));
        }

        if (isValidate) {
            _validateMethods.add(new ValidateMethodDescriptorImpl(method, this));
        }

        if (isClose) {
            _closeMethods.add(new CloseMethodDescriptorImpl(method, this));
        }
    }

    public Set<InitializeMethodDescriptor> getInitializeMethods() {
        return Collections.unmodifiableSet(_initializeMethods);
    }

    public Set<ConfiguredPropertyDescriptor> getConfiguredProperties() {
        return Collections.unmodifiableSet(_configuredProperties);
    }

    public Set<CloseMethodDescriptor> getCloseMethods() {
        return Collections.unmodifiableSet(_closeMethods);
    }

    @Override
    public Set<ValidateMethodDescriptor> getValidateMethods() {
        return Collections.unmodifiableSet(_validateMethods);
    }

    @Override
    public Set<ProvidedPropertyDescriptor> getProvidedProperties() {
        return Collections.unmodifiableSet(_providedProperties);
    }

    @Override
    public Set<ProvidedPropertyDescriptor> getProvidedPropertiesByType(Class<?> cls) {
        Set<ProvidedPropertyDescriptor> result = new HashSet<ProvidedPropertyDescriptor>();
        for (ProvidedPropertyDescriptor descriptor : _providedProperties) {
            if (ReflectionUtils.is(descriptor.getType(), cls)) {
                result.add(descriptor);
            }
        }
        return result;
    }

    @Override
    public ConfiguredPropertyDescriptor getConfiguredProperty(String configuredName) {
        for (ConfiguredPropertyDescriptor configuredDescriptor : _configuredProperties) {
            if (configuredName.equals(configuredDescriptor.getName())) {
                return configuredDescriptor;
            }
        }

        for (ConfiguredPropertyDescriptor configuredDescriptor : _configuredProperties) {
            String[] aliases = configuredDescriptor.getAliases();
            if (ArrayUtils.contains(aliases, configuredName)) {
                return configuredDescriptor;
            }
        }
        return null;
    }

    @Override
    public Set<ConfiguredPropertyDescriptor> getConfiguredPropertiesByAnnotation(
            Class<? extends Annotation> annotationClass) {
        Set<ConfiguredPropertyDescriptor> set = new TreeSet<ConfiguredPropertyDescriptor>();
        for (ConfiguredPropertyDescriptor configuredDescriptor : _configuredProperties) {
            Annotation annotation = configuredDescriptor.getAnnotation(annotationClass);
            if (annotation != null) {
                set.add(configuredDescriptor);
            }
        }
        return set;
    }

    @Override
    public Set<ConfiguredPropertyDescriptor> getConfiguredPropertiesByType(Class<?> type, boolean includeArrays) {
        Set<ConfiguredPropertyDescriptor> set = new TreeSet<ConfiguredPropertyDescriptor>();
        for (ConfiguredPropertyDescriptor configuredDescriptor : _configuredProperties) {
            final boolean include;
            if (includeArrays) {
                include = ReflectionUtils.is(configuredDescriptor.getBaseType(), type);
            } else {
                Class<?> baseType = configuredDescriptor.getType();
                if (baseType.isArray() == type.isArray()) {
                    include = ReflectionUtils.is(baseType, type);
                } else {
                    include = false;
                }
            }
            if (include) {
                set.add(configuredDescriptor);
            }
        }
        return set;
    }

    @Override
    public int compareTo(ComponentDescriptor<?> o) {
        if (o == null) {
            return 1;
        }
        Class<?> otherBeanClass = o.getComponentClass();
        if (otherBeanClass == null) {
            return 1;
        }
        String thisBeanClassName = this.getComponentClass().toString();
        String thatBeanClassName = otherBeanClass.toString();
        return thisBeanClassName.compareTo(thatBeanClassName);
    }
}
