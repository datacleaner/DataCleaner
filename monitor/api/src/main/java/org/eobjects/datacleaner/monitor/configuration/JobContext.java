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

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.datacleaner.repository.RepositoryFile;

/**
 * Defines a context around an {@link AnalysisJob}.
 */
public interface JobContext {
    
    public String getName();
    
    public AnalysisJob getAnalysisJob(Map<String, String> variableOverrides);

    public AnalysisJob getAnalysisJob();

    public String getSourceDatastoreName();
    
    public List<String> getSourceColumnPaths();
    
    public Map<String, String> getVariables();

    public void toXml(OutputStream out);

    public RepositoryFile getJobFile();
}
