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
package org.eobjects.analyzer.job;

import java.util.List;

import org.eobjects.analyzer.job.builder.LazyFilterOutcome;
import org.apache.metamodel.util.BaseObject;

/**
 * Provides hashCode, equals and toString implementations for FilterOutcome,
 * making them comparable across different implementations.
 * 
 * Specifically this has been designed to make it possible to use the
 * equals(...) method with both ImmutableFilterOutcome and LazyFilterOutcome
 * instances.
 * 
 * @see ImmutableFilterOutcome
 * @see LazyFilterOutcome
 */
public abstract class AbstractFilterOutcome extends BaseObject implements FilterOutcome {

    private static final long serialVersionUID = 1L;
    
    @Override
    public boolean isEquals(FilterOutcome filterOutcome) {
        return equals(filterOutcome);
    }

    @Override
    protected final void decorateIdentity(List<Object> identifiers) {
        identifiers.add(getCategory());
        identifiers.add(getSource());
    }

    @Override
    protected final boolean classEquals(BaseObject obj) {
        // works with all subtypes
        return obj instanceof FilterOutcome;
    }

    @Override
    public String toString() {
        return "FilterOutcome[category=" + getCategory() + "]";
    }
    
    @Override
    public final String getSimpleName() {
        return getCategory().toString();
    }
}
