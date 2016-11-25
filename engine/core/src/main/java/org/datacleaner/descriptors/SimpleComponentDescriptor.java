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
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.ArrayUtils;
import org.datacleaner.api.Alias;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Close;
import org.datacleaner.api.ComponentCategory;
import org.datacleaner.api.ComponentSuperCategory;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Distributed;
import org.datacleaner.api.HasDistributionAdvice;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.MultiStreamComponent;
import org.datacleaner.api.Provided;
import org.datacleaner.api.Validate;
import org.datacleaner.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ComponentDescriptor} for simple components. Simple components covers
 * reference data types (Dictionary, SynonymCatalog, StringPattern) as well as
 * custom configuration components.
 *
 * Simple components support the {@link Configured}, {@link Validate},
 * {@link Initialize} and {@link Close} annotations as well as the
 * {@link Closeable} interface.
 *
 * @see Initialize
 * @see Validate
 * @see Close
 * @see Configured
 * @see Closeable
 */
class SimpleComponentDescriptor<B> extends AbstractDescriptor<B> implements ComponentDescriptor<B> {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(SimpleComponentDescriptor.class);

    protected final Set<ConfiguredPropertyDescriptor> _configuredProperties;
    protected final Set<ProvidedPropertyDescriptor> _providedProperties;
    protected final Set<InitializeMethodDescriptor> _initializeMethods;
    protected final Set<ValidateMethodDescriptor> _validateMethods;
    protected final Set<CloseMethodDescriptor> _closeMethods;

    /**
     * Constructor for inheriting from SimpleComponentDescriptor
     *
     * @param beanClass
     */
    public SimpleComponentDescriptor(final Class<B> beanClass) {
        this(beanClass, false);
    }

    public SimpleComponentDescriptor(final Class<B> beanClass, final boolean initialize) {
        super(beanClass);
        _configuredProperties = new TreeSet<>();
        _providedProperties = new TreeSet<>();
        _validateMethods = new HashSet<>();
        _initializeMethods = new HashSet<>();
        _closeMethods = new HashSet<>();
        if (initialize) {
            visitClass();
        }
    }

    @Override
    public String getDisplayName() {
        final Named named = getAnnotation(Named.class);
        if (named == null) {
            return getComponentClass().getSimpleName();
        }
        return named.value();
    }

    @Override
    public final String getDescription() {
        final Description description = getAnnotation(Description.class);
        if (description == null) {
            return null;
        }
        return description.value();
    }

    @Override
    public <A extends Annotation> A getAnnotation(final Class<A> annotationClass) {
        return ReflectionUtils.getAnnotation(getComponentClass(), annotationClass);
    }

    @Override
    public Set<Annotation> getAnnotations() {
        final Annotation[] annotations = getComponentClass().getAnnotations();
        return new HashSet<>(Arrays.asList(annotations));
    }

    @Override
    public Set<ComponentCategory> getComponentCategories() {
        final Categorized categorized = getAnnotation(Categorized.class);
        if (categorized == null) {
            return Collections.emptySet();
        }
        final Class<? extends ComponentCategory>[] value = categorized.value();
        if (value == null || value.length == 0) {
            return Collections.emptySet();
        }

        final Set<ComponentCategory> result = new HashSet<>();
        for (final Class<? extends ComponentCategory> categoryClass : value) {
            if (categoryClass != ComponentCategory.class) {
                final ComponentCategory category = ReflectionUtils.newInstance(categoryClass);
                result.add(category);
            }
        }

        return result;
    }

    @Override
    public ComponentSuperCategory getComponentSuperCategory() {
        final Categorized categorized = getAnnotation(Categorized.class);

        Class<? extends ComponentSuperCategory> superCategoryClass;
        if (categorized == null) {
            superCategoryClass = getDefaultComponentSuperCategoryClass();
        } else {
            superCategoryClass = categorized.superCategory();
            if (superCategoryClass == ComponentSuperCategory.class) {
                superCategoryClass = getDefaultComponentSuperCategoryClass();
            }
        }
        return ReflectionUtils.newInstance(superCategoryClass);
    }

    /**
     * Defines the {@link ComponentSuperCategory} to return, if no
     * {@link ComponentSuperCategory} was defined
     *
     * @return
     */
    protected Class<? extends ComponentSuperCategory> getDefaultComponentSuperCategoryClass() {
        return ComponentSuperCategory.class;
    }

    @Override
    public B newInstance() {
        try {
            return getComponentClass().newInstance();
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new IllegalStateException("Could not construct new instance of " + getComponentClass(), e);
        }
    }

    @Override
    protected void visitClass() {
        super.visitClass();

        if (ReflectionUtils.isCloseable(getComponentClass())) {
            try {
                final Method method = getComponentClass().getMethod("close", new Class<?>[0]);
                final CloseMethodDescriptorImpl cmd = new CloseMethodDescriptorImpl(method, this);
                _closeMethods.add(cmd);
            } catch (final Exception e) {
                // This should be impossible since all closeable's have a no-arg
                // close() method
                logger.error("Unexpected exception while getting close() method from Closeable", e);
                assert false;
            }
        }
    }

    @Override
    protected void visitField(final Field field) {
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
            final ConfiguredPropertyDescriptor cpd = new ConfiguredPropertyDescriptorImpl(field, this);
            _configuredProperties.add(cpd);
        }
    }

    @Override
    protected void visitMethod(final Method method) {
        final boolean isInitialize;
        {
            final boolean isInitializeAnnotationPresent = ReflectionUtils.isAnnotationPresent(method, Initialize.class);
            final boolean isPostConstructAnnotationPresent =
                    ReflectionUtils.isAnnotationPresent(method, PostConstruct.class);
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

    public final Set<InitializeMethodDescriptor> getInitializeMethods() {
        return Collections.unmodifiableSet(_initializeMethods);
    }

    public final Set<ConfiguredPropertyDescriptor> getConfiguredProperties() {
        return Collections.unmodifiableSet(_configuredProperties);
    }

    public final Set<CloseMethodDescriptor> getCloseMethods() {
        return Collections.unmodifiableSet(_closeMethods);
    }

    @Override
    public final Set<ValidateMethodDescriptor> getValidateMethods() {
        return Collections.unmodifiableSet(_validateMethods);
    }

    @Override
    public final Set<ProvidedPropertyDescriptor> getProvidedProperties() {
        return Collections.unmodifiableSet(_providedProperties);
    }

    @Override
    public final Set<ProvidedPropertyDescriptor> getProvidedPropertiesByType(final Class<?> cls) {
        final Set<ProvidedPropertyDescriptor> result = new HashSet<>();
        for (final ProvidedPropertyDescriptor descriptor : _providedProperties) {
            if (ReflectionUtils.is(descriptor.getType(), cls)) {
                result.add(descriptor);
            }
        }
        return result;
    }

    @Override
    public final ConfiguredPropertyDescriptor getConfiguredProperty(final String configuredName) {
        for (final ConfiguredPropertyDescriptor configuredDescriptor : _configuredProperties) {
            if (configuredName.equals(configuredDescriptor.getName())) {
                return configuredDescriptor;
            }
        }

        for (final ConfiguredPropertyDescriptor configuredDescriptor : _configuredProperties) {
            final String[] aliases = configuredDescriptor.getAliases();
            if (ArrayUtils.contains(aliases, configuredName)) {
                return configuredDescriptor;
            }
        }
        return null;
    }

    @Override
    public final Set<ConfiguredPropertyDescriptor> getConfiguredPropertiesByAnnotation(
            final Class<? extends Annotation> annotationClass) {
        final Set<ConfiguredPropertyDescriptor> set = new TreeSet<>();
        for (final ConfiguredPropertyDescriptor configuredDescriptor : _configuredProperties) {
            final Annotation annotation = configuredDescriptor.getAnnotation(annotationClass);
            if (annotation != null) {
                set.add(configuredDescriptor);
            }
        }
        return set;
    }

    @Override
    public final Set<ConfiguredPropertyDescriptor> getConfiguredPropertiesByType(final Class<?> type,
            final boolean includeArrays) {
        final Set<ConfiguredPropertyDescriptor> set = new TreeSet<>();
        for (final ConfiguredPropertyDescriptor configuredDescriptor : _configuredProperties) {
            final boolean include;
            if (includeArrays) {
                include = ReflectionUtils.is(configuredDescriptor.getBaseType(), type);
            } else {
                final Class<?> baseType = configuredDescriptor.getType();
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
    public int compareTo(final ComponentDescriptor<?> o) {
        if (o == null) {
            return 1;
        }

        final Class<?> otherComponentClass = o.getComponentClass();
        if (otherComponentClass == null) {
            return 1;
        }

        int diff = this.getDisplayName().compareTo(o.getDisplayName());
        if (diff == 0) {
            final String thisBeanClassName = this.getComponentClass().toString();
            final String thatBeanClassName = otherComponentClass.toString();
            diff = thisBeanClassName.compareTo(thatBeanClassName);
        }

        return diff;
    }

    @Override
    public final boolean isDistributable() {
        final Distributed distributed = getAnnotation(Distributed.class);
        if (distributed != null) {
            return distributed.value();
        }
        final boolean hasDistributionAdvice = ReflectionUtils.is(getComponentClass(), HasDistributionAdvice.class);
        if (hasDistributionAdvice) {
            return true;
        }
        return isDistributableByDefault();
    }

    protected boolean isDistributableByDefault() {
        return false;
    }

    @Override
    public boolean isMultiStreamComponent() {
        return ReflectionUtils.is(getComponentClass(), MultiStreamComponent.class);
    }

    @Override
    public final String[] getAliases() {
        final Alias alias = getAnnotation(Alias.class);
        if (alias == null) {
            return new String[0];
        }
        return alias.value();
    }

    @Override
    public final Set<ConfiguredPropertyDescriptor> getConfiguredPropertiesForInput() {
        return getConfiguredPropertiesForInput(true);
    }

    @Override
    public final Set<ConfiguredPropertyDescriptor> getConfiguredPropertiesForInput(final boolean includeOptional) {
        final Set<ConfiguredPropertyDescriptor> descriptors = new TreeSet<>(_configuredProperties);
        for (final Iterator<ConfiguredPropertyDescriptor> it = descriptors.iterator(); it.hasNext(); ) {
            final ConfiguredPropertyDescriptor propertyDescriptor = it.next();
            if (!propertyDescriptor.isInputColumn()) {
                it.remove();
            } else if (!includeOptional && !propertyDescriptor.isRequired()) {
                it.remove();
            }
        }
        return descriptors;
    }
}
