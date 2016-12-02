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

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A storage provider that delegates to different backing storage providers,
 * depending on which type of storage entity to use.
 *
 *
 */
public final class CombinedStorageProvider implements StorageProvider {

    private final StorageProvider _collectionsStorageProvider;
    private final StorageProvider _rowAnnotationStorageProvider;

    /**
     *
     * @param collectionsStorageProvider
     *            the StorageProvider to use for provided Collections
     * @param rowAnnotationStorageProvider
     *            the StorageProvider to use for provided RowAnnotations and
     *            RowAnnotationFactories.
     */
    public CombinedStorageProvider(final StorageProvider collectionsStorageProvider,
            final StorageProvider rowAnnotationStorageProvider) {
        _collectionsStorageProvider = collectionsStorageProvider;
        _rowAnnotationStorageProvider = rowAnnotationStorageProvider;
    }

    public StorageProvider getCollectionsStorageProvider() {
        return _collectionsStorageProvider;
    }

    public StorageProvider getRowAnnotationsStorageProvider() {
        return _rowAnnotationStorageProvider;
    }

    @Override
    public <E> List<E> createList(final Class<E> valueType) throws IllegalStateException {
        return _collectionsStorageProvider.createList(valueType);
    }

    @Override
    public <E> Set<E> createSet(final Class<E> valueType) throws IllegalStateException {
        return _collectionsStorageProvider.createSet(valueType);
    }

    @Override
    public <K, V> Map<K, V> createMap(final Class<K> keyType, final Class<V> valueType) throws IllegalStateException {
        return _collectionsStorageProvider.createMap(keyType, valueType);
    }

    @Override
    public RowAnnotationFactory createRowAnnotationFactory() {
        return _rowAnnotationStorageProvider.createRowAnnotationFactory();
    }

}
