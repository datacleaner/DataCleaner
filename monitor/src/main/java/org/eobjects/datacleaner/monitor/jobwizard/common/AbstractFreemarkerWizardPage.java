/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.monitor.jobwizard.common;

import java.io.StringWriter;
import java.util.Map;

import org.eobjects.datacleaner.monitor.jobwizard.api.JobWizardPageController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

/**
 * Abstract {@link JobWizardPageController} which uses Freemarker to render the
 * form contents.
 */
public abstract class AbstractFreemarkerWizardPage implements JobWizardPageController {

    private static final Logger logger = LoggerFactory.getLogger(AbstractFreemarkerWizardPage.class);

    @Override
    public final String getFormInnerHtml() {
        final Configuration configuration = new Configuration();
        configuration.setObjectWrapper(new DefaultObjectWrapper());

        // load templates from the package of the (concrete) class.
        final TemplateLoader templateLoader = new ClassTemplateLoader(getTemplateFriendlyClass(), "");
        configuration.setTemplateLoader(templateLoader);

        final Map<String, Object> formModel = getFormModel();
        final String templateFilename = getTemplateFilename();

        logger.info("Rendering freemarker template {} with form model: {}", templateFilename, formModel);

        final StringWriter out = new StringWriter();
        try {
            final Template template = configuration.getTemplate(templateFilename);
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
