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
package org.datacleaner.monitor.server.wizard.shared.file;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;

import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.Resource;
import org.datacleaner.configuration.DomConfigurationWriter;
import org.datacleaner.monitor.shared.model.DCUserInputException;
import org.datacleaner.monitor.wizard.referencedata.AbstractReferenceDataWizardSession;
import org.datacleaner.monitor.wizard.referencedata.ReferenceDataWizardContext;
import org.w3c.dom.Element;

public abstract class FileWizardSession extends AbstractReferenceDataWizardSession {

    protected String _name;
    protected String _filePath;
    protected String _sessionKey;
    protected String _encoding;
    protected String _caseSensitive;
    
    protected final DomConfigurationWriter _writer;

    public FileWizardSession(ReferenceDataWizardContext context) {
        super(context);

        final Resource resource = getWizardContext().getTenantContext().getConfigurationFile().toResource();
        _writer = new DomConfigurationWriter(resource);
    }

    @Override
    public Integer getPageCount() {
        return 1;
    }

    @Override
    protected Element getUpdatedReferenceDataSubSection(final DocumentBuilder documentBuilder) {
        copyUploadedFileToReferenceDataDirectory();
        return addElementToConfiguration();
    }
    
    protected abstract Element addElementToConfiguration();
    
    private void copyUploadedFileToReferenceDataDirectory() {
        final File tenantHome = getWizardContext().getTenantContext().getConfiguration().getHomeFolder().toFile(); 
        final File referenceDataDirectory = new File(tenantHome.getAbsolutePath() + File.separator + "reference-data");
        final File targetFile = new File(referenceDataDirectory.getAbsolutePath() + File.separator + _filePath);
        
        if (!referenceDataDirectory.exists() && !referenceDataDirectory.mkdirs()) {
            throw new DCUserInputException("Could not create directory for file:\n" + targetFile.getAbsolutePath());
        }
        
        if (!referenceDataDirectory.canWrite()) {
            throw new DCUserInputException("Cannot write data to directory of file:\n" + targetFile.getAbsolutePath());
        }

        final File temporaryFile = (File) getWizardContext().getHttpSession().eval(_sessionKey);
        FileHelper.copy(temporaryFile, targetFile);
        _filePath = targetFile.getAbsolutePath();
    }

    public String getName() {
        return _name;
    }

    public void setName(final String name) {
        _name = name;
    }

    public String getFilePath() {
        return _filePath;
    }

    public void setFilePath(final String filePath) {
        _filePath = filePath;
    }

    public String getSessionKey() {
        return _sessionKey;
    }

    public void setSessionKey(final String sessionKey) {
        _sessionKey = sessionKey;
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
