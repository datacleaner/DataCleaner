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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.io.IOUtils;
import org.apache.metamodel.util.Action;
import org.apache.metamodel.util.FileHelper;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.repository.RepositoryFolder;
import org.datacleaner.restclient.Serializator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Class is for storing components.
 *
 * @since 24.7.15
 */
public class ComponentStoreImpl implements ComponentStore {

    public static final String FOLDER_NAME = "components";
    
    private static final Logger logger = LoggerFactory.getLogger(ComponentStore.class);
    
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();
    private final ObjectMapper objectMapper = Serializator.getJacksonObjectMapper();
    private final RepositoryFolder componentsFolder;

    public ComponentStoreImpl(Repository repository, String tenantId) {
        RepositoryFolder tenantFolder = repository.getFolder(tenantId);
        componentsFolder = tenantFolder.getOrCreateFolder(FOLDER_NAME);
    }

    /**
     * Read file from repository and transform it to object
     *
     * @param instanceId
     * @return
     */
    public ComponentStoreHolder get(String instanceId) {
        logger.info("Read component with id: {}", instanceId);
        readLock.lock();
        final ComponentStoreHolder[] conf = new ComponentStoreHolder[1];
        try {
            RepositoryFile configFile = componentsFolder.getFile(instanceId);
            if (configFile == null) {
                return null;
            }
            configFile.readFile(new Action<InputStream>() {
                @Override
                public void run(InputStream arg) throws Exception {
                    String theString = IOUtils.toString(arg);
                    conf[0] = objectMapper.readValue(theString, ComponentStoreHolder.class);
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
    public void store(final ComponentStoreHolder configuration) {
        logger.info("Store component with id: {}", configuration.getInstanceId());
        writeLock.lock();
        RepositoryFile configFile = componentsFolder.getFile(configuration.getInstanceId());
        if (configFile != null) {
            // I must delete old file.
            configFile.delete();
        }
        try {
            componentsFolder.createFile(configuration.getInstanceId(), new Action<OutputStream>() {
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
     * @param instanceId
     * @return
     */
    public boolean remove(String instanceId) {
        writeLock.lock();
        try {
            RepositoryFile configFile = componentsFolder.getFile(instanceId);
            if (configFile == null) {
                logger.info("Component with id: {} is not in store.", instanceId);
                return false;
            }
            configFile.delete();
            logger.info("Component {} was removed.", instanceId);
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
    public List<ComponentStoreHolder> getList() {
        readLock.lock();
        final List<ComponentStoreHolder> holderList = new ArrayList<>();
        try {
            List<RepositoryFile> files = componentsFolder.getFiles();
            for (RepositoryFile file : files) {
                file.readFile(new Action<InputStream>() {
                    @Override
                    public void run(InputStream arg) throws Exception {
                        String theString = IOUtils.toString(arg);
                        holderList.add(objectMapper.readValue(theString, ComponentStoreHolder.class));
                    }
                });
            }
        } finally {
            readLock.unlock();
        }
        return holderList;
    }
}
