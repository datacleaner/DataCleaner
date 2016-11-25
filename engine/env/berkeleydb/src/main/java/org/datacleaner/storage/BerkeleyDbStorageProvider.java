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

import java.io.File;
import java.lang.reflect.Type;
import java.util.UUID;

import org.datacleaner.descriptors.ProvidedPropertyDescriptor;
import org.datacleaner.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.bind.ByteArrayBinding;
import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.tuple.BooleanBinding;
import com.sleepycat.bind.tuple.ByteBinding;
import com.sleepycat.bind.tuple.CharacterBinding;
import com.sleepycat.bind.tuple.DoubleBinding;
import com.sleepycat.bind.tuple.FloatBinding;
import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.bind.tuple.ShortBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.collections.StoredKeySet;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

/**
 * Berkeley DB based implementation of the {@link StorageProvider} interface.
 *
 *
 */
public final class BerkeleyDbStorageProvider implements StorageProvider {

    private static final String DIRECTORY_PREFIX = "analyzerBeans_";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final File _parentDirectory;
    private File _targetDir;
    private Environment _environment;
    private boolean _deleteOnExit = false;

    public BerkeleyDbStorageProvider(final File parentDirectory) {
        if (!parentDirectory.exists()) {
            if (!parentDirectory.mkdirs()) {
                throw new IllegalArgumentException("Could not create directory: " + parentDirectory);
            }
        }
        _parentDirectory = parentDirectory;
    }

    public File getParentDirectory() {
        return _parentDirectory;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (_environment != null) {
            _environment.close();
            cleanDirectory();
        }
    }

    /**
     * Cleans the parent directory of this storage provider. This action will
     * delete all previous collection storages made in this directory, and thus
     * it should only be invoked either before any collections has been made or
     * when all collections are ensured to be unused.
     */
    public void cleanDirectory() {
        final File[] directories = _parentDirectory.listFiles((dir, name) -> name.startsWith(DIRECTORY_PREFIX));
        for (final File directory : directories) {
            delete(directory);
        }
    }

    /**
     * Recursively deletes a directory and all it's files
     *
     * @param file
     */
    private void delete(final File file) {
        if (file.isDirectory()) {
            final File[] children = file.listFiles();
            for (final File child : children) {
                delete(child);
            }
        }
        if (!file.delete()) {
            if (!file.isDirectory()) {
                logger.warn("Unable to clean/delete file: {}", file);
            } else {
                logger.debug("Unable to clean/delete directory: {}", file);
            }
        }
    }

    public Object createProvidedCollection(final ProvidedPropertyDescriptor providedDescriptor) {
        final Type typeArgument = providedDescriptor.getTypeArgument(0);
        final Class<?> clazz1 = (Class<?>) typeArgument;
        if (providedDescriptor.isList()) {
            return createList(clazz1);
        } else if (providedDescriptor.isSet()) {
            return createSet(clazz1);
        } else if (providedDescriptor.isMap()) {
            final Class<?> clazz2 = (Class<?>) providedDescriptor.getTypeArgument(1);
            return createMap(clazz1, clazz2);
        } else {
            // This should never happen (is checked by the
            // ProvidedDescriptor)
            throw new IllegalStateException();
        }
    }

    protected Environment getEnvironment() throws DatabaseException {
        if (_environment == null) {
            final EnvironmentConfig config = new EnvironmentConfig();
            config.setAllowCreate(true);
            final File targetDir = getTargetDir();
            _environment = new Environment(targetDir, config);
        }
        return _environment;
    }

    private File getTargetDir() {
        if (_targetDir == null) {
            while (_targetDir == null) {
                try {
                    final File candidateDir =
                            new File(_parentDirectory, DIRECTORY_PREFIX + UUID.randomUUID().toString());
                    if (!candidateDir.exists() && candidateDir.mkdir()) {
                        _targetDir = candidateDir;
                        _deleteOnExit = true;
                    }
                } catch (final Exception e) {
                    logger.error("Exception thrown while trying to create targetDir inside tempDir", e);
                    _targetDir = _parentDirectory;
                }
            }
            if (logger.isInfoEnabled()) {
                logger.info("Using target directory for persistent collections (deleteOnExit=" + _deleteOnExit + "): "
                        + _targetDir.getAbsolutePath());
            }
            initDeleteOnExit(_targetDir);
        }
        return _targetDir;
    }

    private void initDeleteOnExit(final File dir) {
        final File[] files = dir.listFiles();
        dir.deleteOnExit();
        for (final File file : files) {
            if (file.isDirectory()) {
                initDeleteOnExit(file);
            } else if (file.isFile()) {
                file.deleteOnExit();
            } else {
                logger.warn("Unable to set the deleteOnExit flag on file: " + file);
            }
        }
    }

    private Database createDatabase() throws DatabaseException {
        final DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.setAllowCreate(true);
        final String databaseName = UUID.randomUUID().toString();
        return getEnvironment().openDatabase(null, databaseName, databaseConfig);
    }

    @Override
    public <E> BerkeleyDbList<E> createList(final Class<E> valueType) throws IllegalStateException {
        final BerkeleyDbMap<Integer, E> map = createMap(Integer.class, valueType);

        // Berkeley StoredLists are non-functional!
        // return new StoredList<E>(createDatabase(), valueBinding, true);

        return new BerkeleyDbList<>(map);
    }

    @Override
    public <E> BerkeleyDbSet<E> createSet(final Class<E> valueType) throws IllegalStateException {
        try {
            final Database database = createDatabase();
            final StoredKeySet set = new StoredKeySet(database, createBinding(valueType), true);
            return new BerkeleyDbSet<>(getEnvironment(), database, set);
        } catch (final DatabaseException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public <K, V> BerkeleyDbMap<K, V> createMap(final Class<K> keyType, final Class<V> valueType)
            throws IllegalStateException {
        try {
            final EntryBinding keyBinding = createBinding(keyType);
            final EntryBinding valueBinding = createBinding(valueType);
            final Database database = createDatabase();
            final StoredMap map = new StoredMap(database, keyBinding, valueBinding, true);
            return new BerkeleyDbMap<>(getEnvironment(), database, map);
        } catch (final DatabaseException e) {
            throw new IllegalStateException(e);
        }
    }

    private EntryBinding createBinding(final Type type) throws UnsupportedOperationException {
        if (ReflectionUtils.isString(type)) {
            return new StringBinding();
        }
        if (ReflectionUtils.isInteger(type)) {
            return new IntegerBinding();
        }
        if (ReflectionUtils.isLong(type)) {
            return new LongBinding();
        }
        if (ReflectionUtils.isBoolean(type)) {
            return new BooleanBinding();
        }
        if (ReflectionUtils.isShort(type)) {
            return new ShortBinding();
        }
        if (ReflectionUtils.isByte(type)) {
            return new ByteBinding();
        }
        if (ReflectionUtils.isDouble(type)) {
            return new DoubleBinding();
        }
        if (ReflectionUtils.isFloat(type)) {
            return new FloatBinding();
        }
        if (ReflectionUtils.isCharacter(type)) {
            return new CharacterBinding();
        }
        if (ReflectionUtils.isByteArray(type)) {
            return new ByteArrayBinding();
        }
        throw new UnsupportedOperationException("Cannot provide collection of type " + type);
    }

    @Override
    public RowAnnotationFactory createRowAnnotationFactory() {
        // TODO: Create a persistent RowAnnotationFactory
        return new InMemoryRowAnnotationFactory2();
    }
}
