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
package org.datacleaner.monitor.server.wizard.stringpattern.regexpswap;

import javax.xml.parsers.DocumentBuilder;

import org.apache.metamodel.util.Resource;
import org.datacleaner.configuration.DomConfigurationWriter;
import org.datacleaner.monitor.wizard.WizardPageController;
import org.datacleaner.monitor.wizard.referencedata.AbstractReferenceDataWizardSession;
import org.datacleaner.monitor.wizard.referencedata.ReferenceDataWizardContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

final class RegexpSwapStringPatternReferenceDataWizardSession extends AbstractReferenceDataWizardSession {

    private static final Logger logger = LoggerFactory.getLogger(RegexpSwapStringPatternReferenceDataWizardSession.class);

    private String _category;
    private String _expression;

    public RegexpSwapStringPatternReferenceDataWizardSession(ReferenceDataWizardContext context) {
        super(context);
    }

    @Override
    public WizardPageController firstPageController() {
        return new RegexpSwapStringPatternReferenceDataPage(this);
    }

    @Override
    public Integer getPageCount() {
        return 1;
    }

    @Override
    protected Element getUpdatedReferenceDataSubSection(final DocumentBuilder documentBuilder) {
        final Resource resource = getWizardContext().getTenantContext().getConfigurationFile().toResource();
        final DomConfigurationWriter writer = new DomConfigurationWriter(resource);
        final Element stringPatternsElement = writer.getStringPatternsElement();
        /*// mytodo
        Regex regex = new Regex(...);
        final StringPattern stringPattern = new RegexSwapStringPattern(regex);
        stringPatternsElement.appendChild(writer.externalize(stringPattern));
        */

        return stringPatternsElement;
    }

    public String getCategory() {
        return _category;
    }

    public void setCategory(final String category) {
        _category = category;
    }

    public String getExpression() {
        return _expression;
    }

    public void setExpression(final String expression) {
        _expression = expression;
    }
}
