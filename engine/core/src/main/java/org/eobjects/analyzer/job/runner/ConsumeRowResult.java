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
package org.eobjects.analyzer.job.runner;

import java.util.List;

import org.eobjects.analyzer.data.InputRow;

/**
 * Result type of {@link ConsumeRowHandler}.
 */
public class ConsumeRowResult {

    private final List<InputRow> _rows;
    private final List<FilterOutcomes> _outcomeSinks;

    public ConsumeRowResult(List<InputRow> rows, List<FilterOutcomes> outcomeSinks) {
        _rows = rows;
        _outcomeSinks = outcomeSinks;
    }

    public List<FilterOutcomes> getOutcomeSinks() {
        return _outcomeSinks;
    }

    public List<InputRow> getRows() {
        return _rows;
    }
}
