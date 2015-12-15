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

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.datacleaner.api.Component;
import org.datacleaner.api.HiddenProperty;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * An object capable of building documentation for DataCleaner {@link Component}
 * s in HTML format.
 */
public class ComponentDocumentationBuilder {

    private final Configuration _freemarkerConfiguration;
    private final Template _template;
    private final boolean _breadcrumbs;

    public ComponentDocumentationBuilder() {
        this(false);
    }

    public ComponentDocumentationBuilder(boolean breadcrumbs) {
        _breadcrumbs = breadcrumbs;
        _freemarkerConfiguration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);

        final TemplateLoader templateLoader = new ClassTemplateLoader(this.getClass(), "");
        _freemarkerConfiguration.setTemplateLoader(templateLoader);
        try {
            _template = _freemarkerConfiguration.getTemplate("template_component.html");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load template", e);
        }
    }

    /**
     * Creates the reference documentation for a {@link Component}.
     * 
     * @param componentDescriptor
     *            the {@link ComponentDescriptor} of the {@link Component} of
     *            interest.
     * @param outputStream
     *            the target {@link OutputStream} to write to
     * @throws IOException
     */
    public void write(ComponentDescriptor<?> componentDescriptor, OutputStream outputStream) throws IOException {
        write(new ComponentDocumentationWrapper(componentDescriptor), outputStream);
    }

    /**
     * Creates the reference documentation for a {@link Component}.
     * 
     * @param componentWrapper
     *            the {@link ComponentDocumentationWrapper} of the
     *            {@link Component} of interest.
     * @param outputStream
     *            the target {@link OutputStream} to write to
     * @throws IOException
     */
    public void write(ComponentDocumentationWrapper componentWrapper, OutputStream outputStream) throws IOException {

        final Map<String, Object> data = new HashMap<>();

        try {
            data.put("breadcrumbs", _breadcrumbs);
            data.put("component", componentWrapper);

            {
                final Set<ConfiguredPropertyDescriptor> configuredProperties = componentWrapper
                        .getComponentDescriptor().getConfiguredProperties();
                final List<ConfiguredPropertyDescriptor> properties = new ArrayList<ConfiguredPropertyDescriptor>(
                        configuredProperties);
                final List<ConfiguredPropertyDocumentationWrapper> propertyList = new ArrayList<>();
                for (ConfiguredPropertyDescriptor property : properties) {
                    final HiddenProperty hiddenProperty = property.getAnnotation(HiddenProperty.class);
                    final Deprecated deprecatedProperty = property.getAnnotation(Deprecated.class);

                    // we do not show hidden or deprecated properties in docs
                    if ((hiddenProperty == null || hiddenProperty.hiddenForLocalAccess() == false)
                            && deprecatedProperty == null) {
                        final ConfiguredPropertyDocumentationWrapper wrapper = new ConfiguredPropertyDocumentationWrapper(
                                property);
                        propertyList.add(wrapper);
                    }
                }

                data.put("properties", propertyList);
            }

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

    /**
     * Used to convert an image object to buffered image. Used in
     * {@link #createDocumentation(ComponentDescriptor, OutputStream)()}
     * 
     * @param image
     * @return buffered image
     */
    public static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }

        // Create a buffered image with transparency
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);

        final Graphics2D bufferedGraphics = bufferedImage.createGraphics();
        bufferedGraphics.drawImage(image, 0, 0, null);
        bufferedGraphics.dispose();

        // Return the buffered image
        return bufferedImage;
    }
}
