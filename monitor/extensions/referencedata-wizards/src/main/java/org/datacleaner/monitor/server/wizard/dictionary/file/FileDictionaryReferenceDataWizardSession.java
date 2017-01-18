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
package org.datacleaner.monitor.server.wizard.dictionary.file;

import org.datacleaner.monitor.server.wizard.shared.file.FileWizardSession;
import org.datacleaner.monitor.wizard.WizardPageController;
import org.datacleaner.monitor.wizard.referencedata.ReferenceDataWizardContext;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.reference.TextFileDictionary;
import org.w3c.dom.Element;

final class FileDictionaryReferenceDataWizardSession extends FileWizardSession {

    public FileDictionaryReferenceDataWizardSession(final ReferenceDataWizardContext context) {
        super(context);
    }

    @Override
    public WizardPageController firstPageController() {
        return new FileDictionaryReferenceDataPage(this);
    }

    @Override
    protected String addReferenceData() {
        final boolean caseSensitive = (_caseSensitive != null && _caseSensitive.equals("on"));
        final Dictionary dictionary = new TextFileDictionary(_name, _filePath, _encoding, caseSensitive);
        getReferenceDataDao().addDictionary(getWizardContext().getTenantContext(), dictionary);
        return _name;
    }
}
