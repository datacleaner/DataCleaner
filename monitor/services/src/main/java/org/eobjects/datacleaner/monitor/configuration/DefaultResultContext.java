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
import org.eobjects.metamodel.util.Func;

public class DefaultResultContext implements ResultContext {

    private final RepositoryFile _repositoryFile;

    public DefaultResultContext(final RepositoryFile resultFile) {
        _repositoryFile = resultFile;
    }

    @Override
    public AnalysisResult getAnalysisResult() {
        final AnalysisResult analysisResult = _repositoryFile.readFile(new Func<InputStream, AnalysisResult>() {
            @Override
            public AnalysisResult eval(InputStream in) {
                try {
                    final ChangeAwareObjectInputStream inputStream = new ChangeAwareObjectInputStream(in);
                    final AnalysisResult analysisResult = (AnalysisResult) inputStream.readObject();
                    return analysisResult;
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        });
        return analysisResult;
    }

    @Override
    public RepositoryFile getResultFile() {
        return _repositoryFile;
    }

}
