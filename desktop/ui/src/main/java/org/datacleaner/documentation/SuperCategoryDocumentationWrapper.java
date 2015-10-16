package org.datacleaner.documentation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.datacleaner.api.ComponentCategory;
import org.datacleaner.api.ComponentSuperCategory;
import org.datacleaner.util.HasNameComparator;

public class SuperCategoryDocumentationWrapper implements Comparable<SuperCategoryDocumentationWrapper> {

    private final ComponentSuperCategory _superCategory;
    private final List<ComponentDocumentationWrapper> _components;
    private final Map<ComponentCategory, CategoryDocumentationWrapper> _categories;

    public SuperCategoryDocumentationWrapper(ComponentSuperCategory superCategory) {
        _superCategory = superCategory;
        _components = new ArrayList<>();
        _categories = new TreeMap<>(new HasNameComparator());
    }

    @Override
    public int compareTo(SuperCategoryDocumentationWrapper o) {
        return _superCategory.compareTo(o._superCategory);
    }

    public void addComponent(ComponentDocumentationWrapper component) {
        _components.add(component);
    }

    public List<ComponentDocumentationWrapper> getComponents() {
        return _components;
    }

    public String getName() {
        return _superCategory.getName();
    }

    public Collection<CategoryDocumentationWrapper> getCategories() {
        return _categories.values();
    }

    public void addComponent(ComponentCategory componentCategory, ComponentDocumentationWrapper component) {
        CategoryDocumentationWrapper categoryWrapper = _categories.get(componentCategory);
        if (categoryWrapper == null) {
            categoryWrapper = new CategoryDocumentationWrapper(componentCategory);
            _categories.put(componentCategory, categoryWrapper);
        }
        categoryWrapper.addComponent(component);
    }

}
