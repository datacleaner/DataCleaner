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
package org.datacleaner.job.runner;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.datacleaner.api.Close;
import org.datacleaner.api.Initialize;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.reference.AbstractReferenceData;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.reference.DictionaryConnection;
import org.junit.Ignore;

@Ignore
public class MockMonitoredDictionary extends AbstractReferenceData implements Dictionary {

    private static final long serialVersionUID = 1L;

    private static int id = 0;

    private final AtomicInteger _initCount = new AtomicInteger(0);
    private final AtomicInteger _closeCount = new AtomicInteger(0);

    public MockMonitoredDictionary() {
        super("mock_dict_" + ++id);
    }

    @Initialize
    public void init() {
        _initCount.incrementAndGet();
    }

    @Close
    public void close() {
        _closeCount.incrementAndGet();
    }

    public int getInitCount() {
        return _initCount.get();
    }

    public int getCloseCount() {
        return _closeCount.get();
    }

    @Override
    public DictionaryConnection openConnection(final DataCleanerConfiguration arg0) {
        return new DictionaryConnection() {
            @Override
            public Iterator<String> getLengthSortedValues() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Iterator<String> getAllValues() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean containsValue(final String value) {
                return false;
            }

            @Override
            public void close() {
            }
        };
    }

}
