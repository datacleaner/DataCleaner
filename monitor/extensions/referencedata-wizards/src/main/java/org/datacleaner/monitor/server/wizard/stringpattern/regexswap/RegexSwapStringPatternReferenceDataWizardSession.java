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
package org.datacleaner.monitor.server.wizard.stringpattern.regexswap;

import javax.xml.parsers.DocumentBuilder;

import org.apache.http.impl.client.HttpClients;
import org.apache.metamodel.util.Resource;
import org.datacleaner.configuration.DomConfigurationWriter;
import org.datacleaner.monitor.wizard.WizardPageController;
import org.datacleaner.monitor.wizard.referencedata.AbstractReferenceDataWizardSession;
import org.datacleaner.monitor.wizard.referencedata.ReferenceDataWizardContext;
import org.datacleaner.reference.StringPattern;
import org.datacleaner.reference.regexswap.Regex;
import org.datacleaner.reference.regexswap.RegexSwapClient;
import org.datacleaner.reference.regexswap.RegexSwapStringPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

final class RegexSwapStringPatternReferenceDataWizardSession extends AbstractReferenceDataWizardSession {

    private static final Logger logger =
            LoggerFactory.getLogger(RegexSwapStringPatternReferenceDataWizardSession.class);

    private String _category;
    private String _name;
    private final RegexSwapClient _client;

    public RegexSwapStringPatternReferenceDataWizardSession(ReferenceDataWizardContext context) {
        super(context);
        _client = new RegexSwapClient(HttpClients.createSystem());
    }

    @Override
    public WizardPageController firstPageController() {
        return new RegexSwapStringPatternReferenceDataPage1(this);
    }

    @Override
    public Integer getPageCount() {
        return 2;
    }

    @Override
    protected Element getUpdatedReferenceDataSubSection(final DocumentBuilder documentBuilder) {
        final Resource resource = getWizardContext().getTenantContext().getConfigurationFile().toResource();
        final DomConfigurationWriter writer = new DomConfigurationWriter(resource);
        final Element stringPatternsElement = writer.getStringPatternsElement();
        final Regex regex = getClient().getRegexByName(_name);
        final StringPattern stringPattern = new RegexSwapStringPattern(regex);
        stringPatternsElement.appendChild(writer.externalize(stringPattern));

        return stringPatternsElement;
    }

    public RegexSwapClient getClient() {
        return _client;
    }

    public String getCategory() {
        return _category;
    }

    public void setCategory(final String category) {
        _category = category;
    }

    public String getName() {
        return _name;
    }

    public void setName(final String name) {
        _name = name;
    }
}
