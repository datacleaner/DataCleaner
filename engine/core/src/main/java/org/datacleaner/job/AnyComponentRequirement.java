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

import java.util.Collection;
import java.util.Collections;

import org.datacleaner.api.InputRow;

/**
 * An outcome that represents "Any outcome", ie. all other requirements/outcomes
 * are satisifed.
 */
public class AnyComponentRequirement implements ComponentRequirement {

    public static final String KEYWORD = "_any_";
    private static final long serialVersionUID = 1L;
    private static final ComponentRequirement INSTANCE = new AnyComponentRequirement();

    private AnyComponentRequirement() {
    }

    public static ComponentRequirement get() {
        return INSTANCE;
    }

    @Override
    public String toString() {
        return "AnyComponentRequirement[]";
    }

    @Override
    public String getSimpleName() {
        return KEYWORD;
    }

    @Override
    public boolean isSatisfied(final InputRow row, final FilterOutcomes outcomes) {
        return true;
    }

    @Override
    public Collection<FilterOutcome> getProcessingDependencies() {
        return Collections.emptyList();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj == this || obj instanceof AnyComponentRequirement;
    }

    @Override
    public int hashCode() {
        return 42;
    }
}
