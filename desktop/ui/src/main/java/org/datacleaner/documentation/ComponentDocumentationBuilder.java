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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.datacleaner.api.Component;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.util.IconUtils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * An object capable of building documentation for DataCleaner {@link Component}
 * s in HTML format.
 * 
 * TODO list:
 * 
 * Check what is actually output - what about result type, result metrics,
 * output columns, filter outcomes.
 * 
 * Plug in to CLI to generate complete documentation based on configuration
 */
public class ComponentDocumentationBuilder {

    private static final String HTMLBASE64_PREFIX = "data:image/png;base64,";

    private final Configuration _freemarkerConfiguration;
    private final Template _template;
    private final boolean _breadcrumbs;

    public ComponentDocumentationBuilder() {
        this(false);
    }

    public ComponentDocumentationBuilder(boolean breadcrumbs) {
        _breadcrumbs = breadcrumbs;
        _freemarkerConfiguration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        _freemarkerConfiguration.setClassForTemplateLoading(this.getClass(), ".");
        try {
            _template = _freemarkerConfiguration.getTemplate("template.html");
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
    public void write(ComponentDescriptor<?> componentDescriptor, OutputStream outputStream)
            throws IOException {

        final Map<String, Object> data = new HashMap<>();

        try {
            data.put("breadcrumbs", _breadcrumbs);
            data.put("component", new ComponentDocumentationWrapper(componentDescriptor));

            {
                final Set<ConfiguredPropertyDescriptor> configuredProperties = componentDescriptor
                        .getConfiguredProperties();
                final List<ConfiguredPropertyDescriptor> properties = new ArrayList<ConfiguredPropertyDescriptor>(
                        configuredProperties);
                final List<ConfiguredPropertyDocumentationWrapper> propertyList = new ArrayList<>();
                for (ConfiguredPropertyDescriptor property : properties) {
                    ConfiguredPropertyDocumentationWrapper wrapper = new ConfiguredPropertyDocumentationWrapper(
                            property);
                    propertyList.add(wrapper);
                }

                data.put("properties", propertyList);
            }

            { // Attach the image
                final Image descriptorIcon = IconUtils.getDescriptorIcon(componentDescriptor).getImage();

                /* We need a buffered image type in order to obtain the */
                final BufferedImage bufferedImage = toBufferedImage(descriptorIcon);
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "png", baos);
                byte[] imageInByte = baos.toByteArray();
                baos.flush();
                baos.close();

                /* Encode the image */
                final String encodedImage = Base64.encodeBase64String(imageInByte);

                /*
                 * Atach the prefix that will make html <img> know how to decode
                 * the image
                 */
                final String iconHtmlRepresentation = HTMLBASE64_PREFIX + encodedImage;

                data.put("icon", iconHtmlRepresentation);
            }

            /* Write data to a file */
            Writer out = new OutputStreamWriter(outputStream);
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
