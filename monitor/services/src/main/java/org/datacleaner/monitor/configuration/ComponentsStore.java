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
package org.datacleaner.monitor.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.metamodel.util.Action;
import org.apache.metamodel.util.FileHelper;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.repository.RepositoryFolder;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Class is for storing components.
 *
 * @author k.houzvicka
 * @since 24.7.15
 */
public class ComponentsStore {

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    public static final String FOLDER_NAME = "components";

    private Repository repository;

    private RepositoryFolder componentsFolder;

    private ObjectMapper objectMapper = new ObjectMapper();

    public ComponentsStore(Repository repository) {
        this.repository = repository;
        componentsFolder = repository.getFolder(FOLDER_NAME);
        if (componentsFolder == null) {
            componentsFolder = repository.createFolder(FOLDER_NAME);
        }

    }

    /**
     * Read file from repository and transform it to object
     *
     * @param componentId
     * @return
     */
    public ComponentsCacheConfigWrapper getConfiguration(String componentId) {
        readLock.lock();
        final ComponentsCacheConfigWrapper[] conf = new ComponentsCacheConfigWrapper[1];
        try {
            RepositoryFile configFile = componentsFolder.getFile(componentId);
            if (configFile == null) {
                return null;
            }
            configFile.readFile(new Action<InputStream>() {
                @Override
                public void run(InputStream arg) throws Exception {
                    String theString = IOUtils.toString(arg);
                    conf[0] = objectMapper.readValue(theString, ComponentsCacheConfigWrapper.class);
                }
            });
        } finally {
            readLock.unlock();
        }
        return conf[0];
    }

    /**
     * Store configuration to repository in JSON
     *
     * @param configWrapper
     */
    public void storeConfiguration(final ComponentsCacheConfigWrapper configWrapper) {
        writeLock.lock();
        try {
            componentsFolder.createFile(configWrapper.componentConfigHolder.componentId, new Action<OutputStream>() {
                @Override
                public void run(OutputStream fileOutput) throws Exception {
                    String jsonConf = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                            configWrapper);
                    InputStream jsonConfStream = IOUtils.toInputStream(jsonConf);
                    FileHelper.copy(jsonConfStream, fileOutput);
                }
            });
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Delete file from repository
     *
     * @param componentId
     */
    public void removeConfiguration(String componentId) {
        writeLock.lock();
        try {
            RepositoryFile configFile = componentsFolder.getFile(componentId);
            if (configFile == null) {
                return;
            }
            configFile.delete();
        } finally {
            writeLock.unlock();
        }
    }

}
