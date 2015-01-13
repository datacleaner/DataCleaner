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
package org.datacleaner.job.runner;

import java.util.List;

/**
 * Interface for components that are aware of errors in a job and is able to
 * inform other components of them.
 */
public interface ErrorAware {

    /**
     * Determines if an error has occurred in the job.
     * 
     * @return
     */
    public boolean isErrornous();

    /**
     * Determines if the job has been cancelled.
     * 
     * @return
     */
    public boolean isCancelled();

    /**
     * Gets a list of errors, if there was any errors reported.
     * 
     * @return a list of errors (Throwables) if errors was reported.
     * 
     * @see #isErrornous()
     */
    public List<Throwable> getErrors();
}
