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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple implementation of {@link RowIdGenerator}, based on
 * {@link AtomicInteger}s.
 */
class SimpleRowIdGenerator implements RowIdGenerator {

    private final AtomicInteger _physicalCounter;
    private final AtomicInteger _virtualCounter;

    public SimpleRowIdGenerator(int offset) {
        _physicalCounter = new AtomicInteger(offset);
        _virtualCounter = new AtomicInteger(Integer.MIN_VALUE + (offset * 3));
    }

    public SimpleRowIdGenerator() {
        this(0);
    }

    @Override
    public int nextPhysicalRowId() {
        return _physicalCounter.incrementAndGet();
    }

    @Override
    public int nextVirtualRowId() {
        return _virtualCounter.incrementAndGet();
    }

}
