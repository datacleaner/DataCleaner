package org.datacleaner.documentation;

import java.util.Set;

import org.datacleaner.api.ComponentCategory;
import org.datacleaner.api.Concurrent;
import org.datacleaner.api.QueryOptimizedFilter;
import org.datacleaner.descriptors.AnalyzerDescriptor;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.FilterDescriptor;
import org.datacleaner.descriptors.TransformerDescriptor;
import org.datacleaner.util.ReflectionUtils;

import com.google.common.base.Strings;

/**
 * A wrapper around the {@link ComponentDescriptor} object to make it easier for
 * the documentation template to get to certain aspects that should be presented
 * in the documentation.
 */
public class ComponentDocumentationWrapper {

    private final ComponentDescriptor<?> _componentDescriptor;

    public ComponentDocumentationWrapper(ComponentDescriptor<?> componentDescriptor) {
        _componentDescriptor = componentDescriptor;
    }

    public String getName() {
        return _componentDescriptor.getDisplayName();
    }

    public String getDescription() {
        return Strings.nullToEmpty(_componentDescriptor.getDescription());
    }

    public String getSuperCategory() {
        return _componentDescriptor.getComponentSuperCategory().getName();
    }

    public String[] getCategories() {
        final Set<ComponentCategory> componentCategories = _componentDescriptor.getComponentCategories();
        final ComponentCategory[] array = componentCategories
                .toArray(new ComponentCategory[componentCategories.size()]);
        final String[] result = new String[componentCategories.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = array[i].getName();
        }
        return result;
    }

    public boolean isQueryOptimizable() {
        return ReflectionUtils.is(_componentDescriptor.getComponentClass(), QueryOptimizedFilter.class);
    }

    public String[] getAliases() {
        return _componentDescriptor.getAliases();
    }

    public boolean isDistributable() {
        return _componentDescriptor.isDistributable();
    }

    public boolean isConcurrent() {
        final Concurrent annotation = _componentDescriptor.getAnnotation(Concurrent.class);
        if (annotation != null) {
            return annotation.value();
        }
        if (isAnalyzer()) {
            return false;
        }
        return true;
    }

    public boolean isAnalyzer() {
        return _componentDescriptor instanceof AnalyzerDescriptor;
    }

    public boolean isTransformer() {
        return _componentDescriptor instanceof TransformerDescriptor;
    }

    public boolean isFilter() {
        return _componentDescriptor instanceof FilterDescriptor;
    }
}
