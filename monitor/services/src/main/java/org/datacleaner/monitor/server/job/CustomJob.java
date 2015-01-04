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
package org.datacleaner.monitor.server.job;

import java.io.Serializable;

import javax.inject.Inject;

import org.datacleaner.beans.api.Configured;
import org.datacleaner.beans.api.Initialize;

/**
 * Interface for jobs that can be run and scheduled in DataCleaner using the
 * {@link CustomJobEngine}.
 * 
 * Instances of this interface can have {@link Inject}ed fields, declare
 * {@link Configured} properties and {@link Initialize} methods etc., just like
 * any other AnalyzerBeans component.
 */
public interface CustomJob {

    /**
     * Called by the {@link CustomJobEngine} when the user or schedule triggers
     * job execution.
     * 
     * @param callback
     *            a callback for the job to be able to report it's progress and
     *            get contextual information.
     * @return
     * @throws Exception
     */
    public Serializable execute(CustomJobCallback callback) throws Exception;
}
