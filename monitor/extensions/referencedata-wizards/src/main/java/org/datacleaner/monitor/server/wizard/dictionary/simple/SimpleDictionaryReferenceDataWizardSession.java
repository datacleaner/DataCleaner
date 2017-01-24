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
package org.datacleaner.monitor.server.wizard.dictionary.simple;

import org.datacleaner.monitor.shared.model.DCUserInputException;
import org.datacleaner.monitor.wizard.WizardPageController;
import org.datacleaner.monitor.wizard.referencedata.AbstractReferenceDataWizardSession;
import org.datacleaner.monitor.wizard.referencedata.ReferenceDataWizardContext;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.reference.SimpleDictionary;

final class SimpleDictionaryReferenceDataWizardSession extends AbstractReferenceDataWizardSession {
    private String _name;
    private String _values;
    private String _caseSensitive;

    public SimpleDictionaryReferenceDataWizardSession(final ReferenceDataWizardContext context) {
        super(context);
    }

    @Override
    public WizardPageController firstPageController() {
        return new SimpleDictionaryReferenceDataPage(this);
    }

    @Override
    public Integer getPageCount() {
        return 1;
    }

    @Override
    protected String addReferenceData() {
        final boolean caseSensitive = (_caseSensitive != null && _caseSensitive.equals("on"));
        final Dictionary dictionary = new SimpleDictionary(_name, caseSensitive, _values.split("\n"));
        getReferenceDataDao().addDictionary(getWizardContext().getTenantContext(), dictionary);

        return _name;
    }

    public String getName() {
        return _name;
    }

    public void setName(final String name) {
        if (name == null || name.equals("")) {
            throw new DCUserInputException("Name can not be null or emtpy. ");
        }

        _name = name;
    }

    public String getValues() {
        return _values;
    }

    public void setValues(final String values) {
        if (values == null || values.equals("")) {
            throw new DCUserInputException("Values can not be null or empty. ");
        }

        _values = values;
    }

    public String getCaseSensitive() {
        return _caseSensitive;
    }

    public void setCaseSensitive(final String caseSensitive) {
        if (caseSensitive == null) {
            _caseSensitive = "off";
        } else {
            _caseSensitive = caseSensitive;
        }
    }
}
