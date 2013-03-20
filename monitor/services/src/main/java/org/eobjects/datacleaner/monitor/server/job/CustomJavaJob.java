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
package org.eobjects.datacleaner.monitor.server.job;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Initialize;

/**
 * Interface for jobs that can be run and scheduled in DataCleaner using the
 * {@link CustomJavaJobEngine}.
 * 
 * Instances of this interface can have {@link Inject}ed fields, declare
 * {@link Configured} properties and {@link Initialize} methods etc., just like
 * any other AnalyzerBeans component.
 */
public interface CustomJavaJob {

    /**
     * Called by the {@link CustomJavaJobEngine} when the user or schedule
     * triggers job execution.
     * 
     * @throws Exception
     */
    public void execute() throws Exception;
}