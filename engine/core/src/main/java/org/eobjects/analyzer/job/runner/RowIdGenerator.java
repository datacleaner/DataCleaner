/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.job.runner;

import org.eobjects.analyzer.beans.api.OutputRowCollector;
import org.eobjects.analyzer.data.InputRow;

/**
 * Interface for components that generate {@link InputRow} id's. These should be
 * guaranteed unique.
 */
public interface RowIdGenerator {

    /**
     * Gets the next unique id, for a physical row.
     * 
     * @return
     */
    public int nextPhysicalRowId();

    /**
     * Gets a new unique id, for a virtual/transformed row. This will only be
     * invoked in case of transformers that use the {@link OutputRowCollector}
     * 
     * @return
     */
    public int nextVirtualRowId();
}
