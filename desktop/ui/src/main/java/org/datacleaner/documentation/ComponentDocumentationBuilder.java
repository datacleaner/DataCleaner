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
import org.datacleaner.api.MappedProperty;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.ProvidedPropertyDescriptor;
import org.datacleaner.util.IconUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

/**
 * TODO list:
 * 
 * Check what is actually output - what about result type, result metrics,
 * output columns, filter outcomes.
 * 
 * Plug in to CLI to generate complete documentation based on configuration
 * 
 * Avoid reference (in CSS) to dc-logo-30.png (in src/main/resources)
 */
public class ComponentDocumentationBuilder {

    private static final String HTMLBASE64_PREFIX = "data:image/png;base64,";

    private final Configuration freemarkerConfiguration;
    private final Map<String, Object> _data = new HashMap<String, Object>();;
    private static final Logger logger = LoggerFactory.getLogger(ComponentDocumentationBuilder.class);
    private Template _template;

    public ComponentDocumentationBuilder() {
        freemarkerConfiguration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        freemarkerConfiguration.setClassForTemplateLoading(this.getClass(), ".");
    }

    /**
     * Creates the documentation reference.
     * 
     * @param componentdescriptor
     * @param outputStream
     * @throws IOException
     * @throws ParseException
     * @throws MalformedTemplateNameException
     * @throws TemplateNotFoundException
     */
    public void createDocumentation(ComponentDescriptor<?> componentdescriptor, OutputStream outputStream)
            throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException {

        final Template template = getTemplate();

        try {
            {
                _data.put("component", componentdescriptor);
                final Set<ProvidedPropertyDescriptor> providedProperties = componentdescriptor.getProvidedProperties();
                _data.put("providedproperties", providedProperties);

            }
            {
                final Set<ConfiguredPropertyDescriptor> configuredProperties = componentdescriptor
                        .getConfiguredProperties();

                if (configuredProperties != null) {
                    final List<ConfiguredPropertyDescriptor> properties = new ArrayList<ConfiguredPropertyDescriptor>(
                            configuredProperties);
                    _data.put("properties", properties);
                    Map<String, MappedProperty> mappedProperties = new HashMap<String, MappedProperty>();
                    for (ConfiguredPropertyDescriptor property : properties) {
                        final MappedProperty mappedProperty = property.getAnnotation(MappedProperty.class);
                        if (mappedProperty != null) {
                            mappedProperties.put(property.getName(), mappedProperty);
                        }
                    }
                    _data.put("mappedproperties", mappedProperties);
                }
            }

            { // Attach the image
                final Image descriptorIcon = IconUtils.getDescriptorIcon(componentdescriptor).getImage();

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

                _data.put("icon", iconHtmlRepresentation);
            }

            /* Write data to a file */
            Writer out = new OutputStreamWriter(outputStream);
            template.process(_data, out);
            out.flush();
            out.close();

        } catch (IOException exception) {
            logger.debug("Exception while writing to the file:", exception);

        } catch (TemplateException exception) {
            logger.debug("Exception while loading the template:", exception);
        }
    }

    /**
     * Gets the freemarker configuration.
     * 
     * @return
     */
    public Configuration getFreemarkerconfiguration() {
        return freemarkerConfiguration;
    }

    /**
     * Gets the template.
     * 
     * @return Template
     * @throws TemplateNotFoundException
     * @throws MalformedTemplateNameException
     * @throws ParseException
     * @throws IOException
     */
    public Template getTemplate() throws TemplateNotFoundException, MalformedTemplateNameException, ParseException,
            IOException {
        /* The template needs created only once. */
        if (_template == null) {
            final Configuration freemarkerConfiguration = getFreemarkerconfiguration();
            _template = freemarkerConfiguration.getTemplate("template.html");
        }
        return _template;
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
