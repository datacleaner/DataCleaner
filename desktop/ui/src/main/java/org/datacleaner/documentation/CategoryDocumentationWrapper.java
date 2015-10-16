package org.datacleaner.documentation;

import java.util.ArrayList;
import java.util.List;

import org.datacleaner.api.ComponentCategory;

public class CategoryDocumentationWrapper {

    private final List<ComponentDocumentationWrapper> _components;
    private final ComponentCategory _componentCategory;

    public CategoryDocumentationWrapper(ComponentCategory componentCategory) {
        _componentCategory = componentCategory;
        _components = new ArrayList<>();
    }
    
    public String getName() {
        return _componentCategory.getName();
    }

    public void addComponent(ComponentDocumentationWrapper component) {
        _components.add(component);
    }

    public List<ComponentDocumentationWrapper> getComponents() {
        return _components;
    }
}
