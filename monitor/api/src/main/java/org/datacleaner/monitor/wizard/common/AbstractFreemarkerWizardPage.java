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
package org.datacleaner.monitor.wizard.common;

import java.io.StringWriter;
import java.util.Map;

import org.datacleaner.monitor.wizard.WizardPageController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.Version;

/**
 * Abstract {@link WizardPageController} which uses Freemarker to render the
 * form contents.
 */
public abstract class AbstractFreemarkerWizardPage extends AbstractWizardPage implements WizardPageController {

    private static final Logger logger = LoggerFactory.getLogger(AbstractFreemarkerWizardPage.class);
    
    protected final Configuration _templateConfiguration;

    public AbstractFreemarkerWizardPage() {
        final Version freeMarkerVersion = Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS;
        _templateConfiguration = new Configuration(freeMarkerVersion);
        _templateConfiguration.setObjectWrapper(new DefaultObjectWrapper(freeMarkerVersion));
    }
    
    @Override
    public final String getFormInnerHtml() {
        // load templates from the package of the (concrete) class.
        final Class<?> templateFriendlyClass = getTemplateFriendlyClass();
        final TemplateLoader templateLoader = new ClassTemplateLoader(templateFriendlyClass, "");
        _templateConfiguration.setTemplateLoader(templateLoader);

        final Map<String, Object> formModel = getFormModel();
        final String templateFilename = getTemplateFilename();

        logger.debug("Rendering freemarker template {} with form model: {}", templateFilename, formModel);

        final StringWriter out = new StringWriter();
        try {
            final Template template = _templateConfiguration.getTemplate(templateFilename);
            template.process(formModel, out);
            out.flush();
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            
            throw new IllegalStateException("Could not render freemarker template: " + templateFilename, e);
        }

        return out.toString();
    }

    protected Class<?> getTemplateFriendlyClass() {
        return getClass();
    }

    protected abstract String getTemplateFilename();

    protected abstract Map<String, Object> getFormModel();
}
