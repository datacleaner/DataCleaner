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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.util.IconUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class DocumentationLoader {

    private static final String FILENAME_TEMPLATE = "documentation_template.html";
    static final String OUTPUT_FILENAME = "documentLoaderOutput.txt";

    private Template _template;
    private Map<String, Object> _data;
    private static Logger logger = LoggerFactory.getLogger(DocumentationLoader.class);

    DocumentationLoader() {

        @SuppressWarnings("deprecation")
        final Configuration freemarkerConfiguration = new Configuration();
        freemarkerConfiguration.setClassForTemplateLoading(this.getClass(), "/");
        try {
            // Load the template
            _template = freemarkerConfiguration.getTemplate(FILENAME_TEMPLATE);
            _data = new HashMap<String, Object>();
        } catch (Exception exception) {
            logger.debug("Exception while trying to initialize the template:", exception);
        }
    }

    public void createDocumentation(ComponentDescriptor<?> componentdescriptor) {

        try {
            _data.put("component", componentdescriptor);
            final URL descriptorIconPath = IconUtils.getDescriptorIconPath(componentdescriptor);
            _data.put("icon",  descriptorIconPath);
            
            final Set<ConfiguredPropertyDescriptor> configuredProperties = componentdescriptor
                    .getConfiguredProperties();

            if (configuredProperties != null) {
                final List<ConfiguredPropertyDescriptor> properties = new ArrayList<ConfiguredPropertyDescriptor>(
                        configuredProperties);
                _data.put("properties", properties);
            }

            Writer out = new OutputStreamWriter(new FileOutputStream(OUTPUT_FILENAME));
            _template.process(_data, out);
            out.flush();
            out.close();

        } catch (IOException exception) {
            logger.debug("Exception while writing to the file:", exception);

        } catch (TemplateException exception) {
            logger.debug("Exception while loading the template:", exception);
        }
    }
}
