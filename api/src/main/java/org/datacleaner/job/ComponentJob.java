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
package org.datacleaner.job;

import java.io.Serializable;
import java.util.Map;

import org.datacleaner.descriptors.ComponentDescriptor;
import org.apache.metamodel.util.HasName;

/**
 * Super-interface for all job entries in an Analysis. A {@link ComponentJob}
 * represents a component's configuration in a {@link AnalysisJob}.
 */
public interface ComponentJob extends HasName, HasComponentRequirement, Serializable {

    /**
     * Gets the descriptor of this component type.
     * 
     * @return a descriptor of this component type
     */
    public ComponentDescriptor<?> getDescriptor();

    /**
     * Gets the name of this component job.
     * 
     * @return an optional name given by the user to identify this component in
     *         a job (can be null if no name is assigned).
     */
    @Override
    public String getName();

    /**
     * Gets metadata properties associated with this component.
     * 
     * @return
     */
    public Map<String, String> getMetadataProperties();
}
