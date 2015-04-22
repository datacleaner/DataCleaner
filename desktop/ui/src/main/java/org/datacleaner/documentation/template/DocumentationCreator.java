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

package org.datacleaner.documentation.template;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

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

public class DocumentationCreator {

    private static final String HTMLBASE64_PREFIX = "data:image/png;base64,";
    private static final File cssFile = new File("src/main/resources/documentation.css");
    private static final String FILENAME_TEMPLATE = "documentation_template.html";

    @SuppressWarnings("deprecation")
    private static final Configuration freemarkerConfiguration = new Configuration();
    private final Map<String, Object> _data = new HashMap<String, Object>();;
    private static final Logger logger = LoggerFactory.getLogger(DocumentationCreator.class);
    private Template _template;

    public DocumentationCreator() {
        freemarkerConfiguration.setClassForTemplateLoading(this.getClass(), "/");
    }

    /**
     * Creates the documentation reference
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
                _data.put("cssPath", cssFile.getAbsolutePath());
            }
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
                final byte[] bytesEncoded = Base64.getEncoder().encode(imageInByte);
                final String encodedImage = new String(bytesEncoded);

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

    public static Configuration getFreemarkerconfiguration() {
        return freemarkerConfiguration;
    }

    public static String getFilenameTemplate() {
        return FILENAME_TEMPLATE;
    }

    /**
     * Gets the template.
     * 
     * @return
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
            final String filenameTemplate = getFilenameTemplate();
            _template = freemarkerConfiguration.getTemplate(filenameTemplate);
        }
        return _template;
    }

    /**
     * Used to convert an image object to buffered image.
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
