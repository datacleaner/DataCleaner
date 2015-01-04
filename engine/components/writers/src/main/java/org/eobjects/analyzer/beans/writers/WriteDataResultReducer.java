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
package org.eobjects.analyzer.beans.writers;

import java.util.Collection;

import org.eobjects.analyzer.result.AnalyzerResultReducer;

/**
 * Reducer class for {@link WriteDataResult}s.
 */
public class WriteDataResultReducer implements AnalyzerResultReducer<WriteDataResult> {

    @Override
    public WriteDataResult reduce(Collection<? extends WriteDataResult> results) {
        int writes = 0;
        int updates = 0;
        int errors = 0;
        for (WriteDataResult result : results) {
            writes += result.getWrittenRowCount();
            updates += result.getUpdatesCount();
            errors += result.getErrorRowCount();
        }
        return new WriteDataResultImpl(writes, updates, errors);
    }

}
