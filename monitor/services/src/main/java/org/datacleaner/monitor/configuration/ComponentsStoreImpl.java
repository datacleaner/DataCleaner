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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Class is for storing components.
 *
 * @author k.houzvicka
 * @since 24.7.15
 */
public class ComponentsStoreImpl implements ComponentsStore {
    private static final Logger logger = LoggerFactory.getLogger(ComponentsStore.class);

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    public static final String FOLDER_NAME = "components";

    private RepositoryFolder componentsFolder;

    private ObjectMapper objectMapper = new ObjectMapper();

    public ComponentsStoreImpl(Repository repository, String tenantId) {
        RepositoryFolder tenantFolder = repository.getFolder(tenantId);
        componentsFolder = tenantFolder.getFolder(FOLDER_NAME);
        if (componentsFolder == null) {
            componentsFolder = tenantFolder.createFolder(FOLDER_NAME);
        }

    }

    /**
     * Read file from repository and transform it to object
     *
     * @param componentId
     * @return
     */
    public ComponentsStoreHolder getConfiguration(String componentId) {
        logger.info("Read component with id: {}", componentId);
        readLock.lock();
        final ComponentsStoreHolder[] conf = new ComponentsStoreHolder[1];
        try {
            RepositoryFile configFile = componentsFolder.getFile(componentId);
            if (configFile == null) {
                return null;
            }
            configFile.readFile(new Action<InputStream>() {
                @Override
                public void run(InputStream arg) throws Exception {
                    String theString = IOUtils.toString(arg);
                    conf[0] = objectMapper.readValue(theString, ComponentsStoreHolder.class);
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
     * @param configuration
     */
    public void storeConfiguration(final ComponentsStoreHolder configuration) {
        logger.info("Store component with id: {}", configuration.getComponentId());
        writeLock.lock();
        RepositoryFile configFile = componentsFolder.getFile(configuration.getComponentId());
        if (configFile != null) {
            // I must delete old file.
            configFile.delete();
        }
        try {
            componentsFolder.createFile(configuration.getComponentId(), new Action<OutputStream>() {
                @Override
                public void run(OutputStream fileOutput) throws Exception {
                    String jsonConf = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                            configuration);
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
     * @return
     */
    public boolean removeConfiguration(String componentId) {
        writeLock.lock();
        try {
            RepositoryFile configFile = componentsFolder.getFile(componentId);
            if (configFile == null) {
                logger.info("Component with id: {} is not in store.", componentId);
                return false;
            }
            configFile.delete();
            logger.info("Component {} was removed.", componentId);
        } finally {
            writeLock.unlock();
        }
        return true;
    }


    /**
     * Read all files from repository
     *
     * @return
     */
    @Override
    public List<ComponentsStoreHolder> getAllConfiguration() {
        readLock.lock();
        final List<ComponentsStoreHolder> holderList = new ArrayList<>();
        try {
            List<RepositoryFile> files = componentsFolder.getFiles();
            for (RepositoryFile file : files) {
                file.readFile(new Action<InputStream>() {
                    @Override
                    public void run(InputStream arg) throws Exception {
                        String theString = IOUtils.toString(arg);
                        holderList.add(objectMapper.readValue(theString, ComponentsStoreHolder.class));
                    }
                });
            }
        } finally {
            readLock.unlock();
        }
        return holderList;
    }
}
