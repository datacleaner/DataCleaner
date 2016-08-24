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
package org.datacleaner.monitor.server.wizard.dictionary.datastore;

import org.datacleaner.monitor.server.wizard.shared.datastore.DatastoreWizardSession;
import org.datacleaner.monitor.wizard.WizardPageController;
import org.datacleaner.monitor.wizard.referencedata.ReferenceDataWizardContext;
import org.datacleaner.reference.DatastoreDictionary;
import org.datacleaner.reference.Dictionary;
import org.w3c.dom.Element;

final class DatastoreDictionaryReferenceDataWizardSession extends DatastoreWizardSession {

    public DatastoreDictionaryReferenceDataWizardSession(ReferenceDataWizardContext context) {
        super(context);
    }

    @Override
    public WizardPageController firstPageController() {
        return new DatastoreDictionaryReferenceDataPage1(this);
    }

    @Override
    protected Element addElementToConfiguration() {
        final Element dictionariesElement = _writer.getDictionariesElement();
        final String fullColumnName = _schema + "." + _table + "." + _column;
        final Dictionary dictionary = new DatastoreDictionary(_name, _datastore, fullColumnName);
        dictionariesElement.appendChild(_writer.externalize(dictionary));

        return dictionariesElement;
    }
}
