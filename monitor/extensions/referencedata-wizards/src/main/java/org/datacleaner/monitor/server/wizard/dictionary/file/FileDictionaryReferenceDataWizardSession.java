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

import javax.xml.parsers.DocumentBuilder;

import org.datacleaner.monitor.wizard.WizardPageController;
import org.datacleaner.monitor.wizard.referencedata.AbstractReferenceDataWizardSession;
import org.datacleaner.monitor.wizard.referencedata.ReferenceDataWizardContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

final class FileDictionaryReferenceDataWizardSession extends AbstractReferenceDataWizardSession {

    private static final Logger logger = LoggerFactory.getLogger(FileDictionaryReferenceDataWizardSession.class);

    private String _name;
    private String _file;
    private String _encoding;
    private String _caseSensitive;

    public FileDictionaryReferenceDataWizardSession(ReferenceDataWizardContext context) {
        super(context);
    }

    @Override
    public WizardPageController firstPageController() {
        return new FileDictionaryReferenceDataPage(this);
    }

    @Override
    public Integer getPageCount() {
        return 1;
    }

    @Override
    protected Element createReferenceDataElement(final DocumentBuilder documentBuilder) {
        final Document doc = documentBuilder.newDocument();
        final Element element = doc.createElement("file-dictionary-reference-data");
        element.setAttribute("name", _name);
        element.setAttribute("file", _file);
        element.setAttribute("encoding", _encoding);
        element.setAttribute("case_sensitive", _caseSensitive);

        return element;
    }

    public String getName() {
        return _name;
    }

    public void setName(final String name) {
        _name = name;
    }

    public String getFile() {
        return _file;
    }

    public void setFile(final String file) {
        _file = file;
    }

    public String getEncoding() {
        return _encoding;
    }

    public void setEncoding(final String encoding) {
        _encoding = encoding;
    }

    public String getCaseSensitive() {
        return _caseSensitive;
    }

    public void setCaseSensitive(final String caseSensitive) {
        _caseSensitive = caseSensitive;
    }
}
