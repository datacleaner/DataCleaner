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

import java.util.List;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.datacleaner.repository.RepositoryFolder;

/**
 * Defines a context for a <i>single</i> tenant in which access to shared entries in
 * the repository can be reached.
 */
public interface TenantContext {
    
    public String getTenantId();
    
    public List<String> getJobNames();

    public JobContext getJob(String jobName);
    
    public RepositoryFolder getJobFolder();
    
    public RepositoryFolder getResultFolder();
    
    public RepositoryFolder getTimelineFolder();

    public AnalyzerBeansConfiguration getConfiguration();

    public boolean containsJob(String jobName);
}
