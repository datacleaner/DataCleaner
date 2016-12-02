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
package org.datacleaner.monitor.server.wizard.stringpattern.regex;

import javax.xml.parsers.DocumentBuilder;

import org.datacleaner.monitor.shared.model.DCUserInputException;
import org.datacleaner.monitor.wizard.WizardPageController;
import org.datacleaner.monitor.wizard.referencedata.AbstractReferenceDataWizardSession;
import org.datacleaner.monitor.wizard.referencedata.ReferenceDataWizardContext;
import org.datacleaner.reference.RegexStringPattern;
import org.datacleaner.reference.StringPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

final class RegexStringPatternReferenceDataWizardSession extends AbstractReferenceDataWizardSession {

    private static final Logger logger = LoggerFactory.getLogger(RegexStringPatternReferenceDataWizardSession.class);

    private String _name;
    private String _expression;
    private String _matchEntireString;

    public RegexStringPatternReferenceDataWizardSession(final ReferenceDataWizardContext context) {
        super(context);
    }

    @Override
    public WizardPageController firstPageController() {
        return new RegexStringPatternReferenceDataPage(this);
    }

    @Override
    public Integer getPageCount() {
        return 1;
    }

    @Override
    protected Element getUpdatedReferenceDataSubSection(final DocumentBuilder documentBuilder) {
        final Element stringPatternsElement = _writer.getStringPatternsElement();
        final boolean matchEntireString = (_matchEntireString != null && _matchEntireString.equals("on"));
        final StringPattern stringPattern = new RegexStringPattern(_name, _expression, matchEntireString);
        stringPatternsElement.appendChild(_writer.externalize(stringPattern));

        return stringPatternsElement;
    }

    public String getName() {
        return _name;
    }

    public void setName(final String name) {
        if (name == null || name.equals("")) {
            throw new DCUserInputException("Name can not be null or empty. ");
        }

        _name = name;
    }

    public String getExpression() {
        return _expression;
    }

    public void setExpression(final String expression) {
        if (expression == null || expression.equals("")) {
            throw new DCUserInputException("Expression can not be null or empty. ");
        }

        _expression = expression;
    }

    public String getMatchEntireString() {
        return _matchEntireString;
    }

    public void setMatchEntireString(final String matchEntireString) {
        if (matchEntireString == null) {
            _matchEntireString = "off";
        } else {
            _matchEntireString = matchEntireString;
        }
    }
}
