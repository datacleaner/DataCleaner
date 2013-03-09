/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.monitor.configuration;

import java.io.InputStream;

import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.util.ChangeAwareObjectInputStream;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.metamodel.util.FileHelper;
import org.eobjects.metamodel.util.Func;

/**
 * Default implementation of the {@link ResultContext} interface.
 */
public class DefaultResultContext implements ResultContext {

    private final TenantContext _tenantContext;
    private final RepositoryFile _repositoryFile;

    public DefaultResultContext(final TenantContext tenantContext, final RepositoryFile resultFile) {
        _tenantContext = tenantContext;
        _repositoryFile = resultFile;
    }

    @Override
    public AnalysisResult getAnalysisResult() throws IllegalStateException {
        final AnalysisResult analysisResult = _repositoryFile.readFile(new Func<InputStream, AnalysisResult>() {
            @Override
            public AnalysisResult eval(InputStream in) {
            	ChangeAwareObjectInputStream inputStream = null;
                try {
                	inputStream = new ChangeAwareObjectInputStream(in);
                    final AnalysisResult analysisResult = (AnalysisResult) inputStream.readObject();
                    return analysisResult;
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                } finally {
                	FileHelper.safeClose(inputStream);
                }
            }
        });
        return analysisResult;
    }

    @Override
    public RepositoryFile getResultFile() {
        return _repositoryFile;
    }

    @Override
    public JobContext getJob() {
        final String resultFilename = _repositoryFile.getName();
        // we assume a filename pattern like this:
        // {job}-{timestamp}.analysis.result.dat
        final int lastIndexOfDash = resultFilename.lastIndexOf('-');
        assert lastIndexOfDash != -1;
        final String jobName = resultFilename.substring(0, lastIndexOfDash);
        return _tenantContext.getJob(jobName);
    }

}
