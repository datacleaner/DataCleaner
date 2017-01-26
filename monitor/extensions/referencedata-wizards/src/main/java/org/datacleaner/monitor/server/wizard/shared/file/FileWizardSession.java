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

import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.Resource;
import org.datacleaner.configuration.DomConfigurationWriter;
import org.datacleaner.monitor.shared.model.DCUserInputException;
import org.datacleaner.monitor.wizard.referencedata.AbstractReferenceDataWizardSession;
import org.datacleaner.monitor.wizard.referencedata.ReferenceDataWizardContext;

public abstract class FileWizardSession extends AbstractReferenceDataWizardSession {

    protected final DomConfigurationWriter _writer;
    protected String _name;
    protected String _filePath;
    protected String _sessionKey;
    protected String _encoding;
    protected String _caseSensitive;

    public FileWizardSession(final ReferenceDataWizardContext context) {
        super(context);

        final Resource resource = getWizardContext().getTenantContext().getConfigurationFile().toResource();
        _writer = new DomConfigurationWriter(resource);
    }

    @Override
    public Integer getPageCount() {
        return 1;
    }

    protected void copyUploadedFileToReferenceDataDirectory() {
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
        if (name == null || name.equals("")) {
            throw new DCUserInputException("Name can not be null or empty. ");
        }

        _name = name;
    }

    public String getFilePath() {
        return _filePath;
    }

    public void setFilePath(final String filePath) {
        if (filePath == null || filePath.equals("")) {
            throw new DCUserInputException("File path can not be null or empty. ");
        }

        _filePath = filePath;
    }

    public String getSessionKey() {
        return _sessionKey;
    }

    public void setSessionKey(final String sessionKey) {
        if (sessionKey == null || sessionKey.equals("")) {
            throw new DCUserInputException("Session key can not be null or empty. ");
        }

        _sessionKey = sessionKey;
    }

    public String getEncoding() {
        return _encoding;
    }

    public void setEncoding(final String encoding) {
        if (encoding == null || encoding.equals("")) {
            throw new DCUserInputException("Encoding can not be null or empty. ");
        }

        _encoding = encoding;
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
