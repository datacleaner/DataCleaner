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
package org.datacleaner.documentation;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.datacleaner.api.ComponentCategory;
import org.datacleaner.api.ComponentSuperCategory;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.widgets.DescriptorMenuBuilder;
import org.datacleaner.widgets.DescriptorMenuBuilder.MenuCallback;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class IndexDocumentationBuilder {

    private final DescriptorProvider _descriptorProvider;
    private final Configuration _freemarkerConfiguration;
    private final Template _template;

    public IndexDocumentationBuilder(DescriptorProvider descriptorProvider) {
        _descriptorProvider = descriptorProvider;
        _freemarkerConfiguration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);

        final TemplateLoader templateLoader = new ClassTemplateLoader(this.getClass(), "");
        _freemarkerConfiguration.setTemplateLoader(templateLoader);
        try {
            _template = _freemarkerConfiguration.getTemplate("template_index.html");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load template", e);
        }
    }

    public void write(OutputStream outputStream) throws IOException {
        final Map<ComponentSuperCategory, SuperCategoryDocumentationWrapper> superCategories = new TreeMap<>();

        final MenuCallback callback = new MenuCallback() {
            @Override
            public void addComponentDescriptor(ComponentDescriptor<?> descriptor) {
                final ComponentSuperCategory superCategory = descriptor.getComponentSuperCategory();
                SuperCategoryDocumentationWrapper superCategoryWrapper = superCategories.get(superCategory);
                if (superCategoryWrapper == null) {
                    superCategoryWrapper = new SuperCategoryDocumentationWrapper(superCategory);
                    superCategories.put(superCategory, superCategoryWrapper);
                }

                final Set<ComponentCategory> componentCategories = descriptor.getComponentCategories();
                final ComponentDocumentationWrapper componentWrapper = new ComponentDocumentationWrapper(descriptor);

                if (componentCategories.isEmpty()) {
                    superCategoryWrapper.addComponent(componentWrapper);
                } else {
                    for (ComponentCategory componentCategory : componentCategories) {
                        superCategoryWrapper.addComponent(componentCategory, componentWrapper);
                    }
                }
            }

            @Override
            public void addCategory(ComponentCategory category) {
            }

        };
        DescriptorMenuBuilder.createMenuStructure(callback, _descriptorProvider.getComponentDescriptors(), true);

        final Map<String, Object> data = new HashMap<>();

        try {
            data.put("superCategories", superCategories.values());

            /* Write data to a file */
            final Writer out = new OutputStreamWriter(outputStream);
            _template.process(data, out);
            out.flush();
            out.close();
        } catch (TemplateException e) {
            throw new IllegalStateException("Unexpected templare exception", e);
        }
    }

    /**
     * Gets the freemarker configuration.
     * 
     * @return
     */
    public Configuration getFreemarkerconfiguration() {
        return _freemarkerConfiguration;
    }
}
