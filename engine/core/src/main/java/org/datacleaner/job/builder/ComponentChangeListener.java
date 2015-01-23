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
package org.datacleaner.job.builder;

import org.datacleaner.job.ComponentRequirement;

public interface ComponentChangeListener<C extends ComponentBuilder> extends ComponentRemovalListener<C> {
    
    /**
     * Invoked when a component is added to the {@link AnalysisJobBuilder}.
     * 
     * @param builder
     */
    public void onAdd(C builder);

    /**
     * Invoked when the configuration of a {@link ComponentBuilder} is changed.
     * 
     * @param builder
     */
    public void onConfigurationChanged(C builder);

    /**
     * Invoked when the {@link ComponentRequirement} of a
     * {@link ComponentBuilder} is changed.
     * 
     * @param builder
     */
    public void onRequirementChanged(C builder);
}
