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
package org.eobjects.analyzer.util;

import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.apache.metamodel.util.Action;

public class WriteBufferTest extends TestCase {

    public void testFlushOnBufferSizeReached() throws Exception {
        final AtomicInteger counter = new AtomicInteger();

        final WriteBuffer buffer = new WriteBuffer(5, new Action<Iterable<Object[]>>() {
            @SuppressWarnings("unused")
            @Override
            public void run(Iterable<Object[]> rows) throws Exception {
                for (Object[] row : rows) {
                    counter.incrementAndGet();
                }
            }
        });

        buffer.addToBuffer(new Object[0]);
        buffer.addToBuffer(new Object[0]);
        buffer.addToBuffer(new Object[0]);
        buffer.addToBuffer(new Object[0]);
        assertEquals(4, buffer.getBuffer().size());
        assertEquals(0, counter.get());
        buffer.addToBuffer(new Object[0]);
        assertEquals(5, buffer.getBuffer().size());
        assertEquals(0, counter.get());
        buffer.addToBuffer(new Object[0]);
        assertEquals(1, buffer.getBuffer().size());
        assertEquals(5, counter.get());
    }
}
