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
package org.datacleaner.monitor.server.wizard.stringpattern.simple;

import javax.xml.parsers.DocumentBuilder;

import org.datacleaner.monitor.shared.model.DCUserInputException;
import org.datacleaner.monitor.wizard.WizardPageController;
import org.datacleaner.monitor.wizard.referencedata.AbstractReferenceDataWizardSession;
import org.datacleaner.monitor.wizard.referencedata.ReferenceDataWizardContext;
import org.datacleaner.reference.SimpleStringPattern;
import org.datacleaner.reference.StringPattern;
import org.w3c.dom.Element;

final class SimpleStringPatternReferenceDataWizardSession extends AbstractReferenceDataWizardSession {

    private String _name;
    private String _expression;

    public SimpleStringPatternReferenceDataWizardSession(final ReferenceDataWizardContext context) {
        super(context);
    }

    @Override
    public WizardPageController firstPageController() {
        return new SimpleStringPatternReferenceDataPage(this);
    }

    @Override
    public Integer getPageCount() {
        return 1;
    }

    @Override
    protected String addReferenceData() {
        final StringPattern stringPattern = new SimpleStringPattern(_name, _expression);
        getReferenceDataDao().addStringPattern(getWizardContext().getTenantContext(), stringPattern);

        return _name;
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
}
