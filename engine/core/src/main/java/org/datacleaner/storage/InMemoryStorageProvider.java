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
package org.datacleaner.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * StorageProvider that actually doesn't store data on disk, but only in memory.
 * This implementation is prone to out of memory errors, but is on the other
 * hand very quick for small jobs.
 *
 *
 */
public final class InMemoryStorageProvider implements StorageProvider {

    public static final int DEFAULT_MAX_SAMPLE_SETS = 1000;
    public static final int DEFAULT_MAX_SAMPLE_RECORDS = 150;

    private final int _maxSampleSets;
    private final int _maxSampleRecords;

    public InMemoryStorageProvider() {
        this(DEFAULT_MAX_SAMPLE_SETS, DEFAULT_MAX_SAMPLE_RECORDS);
    }

    public InMemoryStorageProvider(final int maxSampleRecords) {
        this(Math.min(10, DEFAULT_MAX_SAMPLE_SETS * DEFAULT_MAX_SAMPLE_RECORDS / maxSampleRecords), maxSampleRecords);
    }

    public InMemoryStorageProvider(final int maxSampleSets, final int maxSampleRecords) {
        _maxSampleSets = Math.max(0, maxSampleSets);
        _maxSampleRecords = Math.max(0, maxSampleRecords);
    }

    @Override
    public <E> List<E> createList(final Class<E> valueType) throws IllegalStateException {
        return new ArrayList<>();
    }

    @Override
    public <K, V> Map<K, V> createMap(final Class<K> keyType, final Class<V> valueType) throws IllegalStateException {
        return new HashMap<>();
    }

    @Override
    public <E> Set<E> createSet(final Class<E> valueType) throws IllegalStateException {
        return new HashSet<>();
    }

    @Override
    public RowAnnotationFactory createRowAnnotationFactory() {
        return RowAnnotations.getInMemoryFactory(_maxSampleSets, _maxSampleRecords);
    }
}
