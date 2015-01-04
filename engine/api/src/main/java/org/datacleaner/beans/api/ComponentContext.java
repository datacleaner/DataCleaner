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
package org.datacleaner.beans.api;

import javax.inject.Inject;

import org.datacleaner.job.AnalysisJob;

/**
 * Injectable (using {@link Inject} and/or {@link Provided}) context object for
 * any component ( {@link Transformer}, {@link Analyzer} or {@link Filter}) that
 * provides information and actions that affect the outside environment of the
 * component.
 */
public interface ComponentContext {

    /**
     * Gets the {@link AnalysisJob} (if any) that the component is configured
     * in.
     * 
     * @return
     */
    public AnalysisJob getAnalysisJob();
    
    /**
     * Publishes a {@link ComponentMessage} containing information to any
     * appropriate listeners.
     * 
     * @param message
     */
    public void publishMessage(ComponentMessage message);
}
