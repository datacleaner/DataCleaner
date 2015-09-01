package org.datacleaner.descriptors;

import org.datacleaner.api.ComponentCategory;
import org.datacleaner.api.ComponentSuperCategory;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.TransformSuperCategory;
import org.datacleaner.components.remote.RemoteTransformer;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * @Author jakub
 * @Since 9/1/15
 */
public class RemoteTransformerDescriptorImpl extends SimpleComponentDescriptor  implements TransformerDescriptor {

    String displayName;

    public RemoteTransformerDescriptorImpl(String displayName, RemoteConfiguredPropertyDescriptorImpl[] configuredProperties) {
        super(RemoteTransformer.class);
        this._configuredProperties.addAll(Arrays.asList(configuredProperties));
        this.displayName = displayName;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    protected Class<? extends ComponentSuperCategory> getDefaultComponentSuperCategoryClass() {
        return TransformSuperCategory.class;
    }

}
