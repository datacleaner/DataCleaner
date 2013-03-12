/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.monitor.server;

import java.io.InputStream;
import java.io.OutputStream;

import org.eobjects.datacleaner.monitor.configuration.JobContext;

/**
 * Interface for the component which intercepts a tenant's server-side
 * configuration and transforms it into a client-side configuration.
 */
public interface ConfigurationInterceptor {

    /**
     * Intercepts a configuration {@link InputStream} and after conversion
     * creates a consumable {@link OutputStream}.
     * 
     * @param tenantId
     * @param job
     *            optionally the job which is needs the resulting configuration
     *            to execute. May be null if no specific job is requested.
     * @param datastoreName
     *            optionally the datastore name of interest. May be null if no
     *            specific datastore is requested.
     * @param in
     * @param out
     * @throws Exception
     */
    public void intercept(final String tenantId, JobContext job, final String datastoreName, final InputStream in,
            final OutputStream out) throws Exception;
}
