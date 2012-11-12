/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.user;

import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.eobjects.analyzer.configuration.ConfigurationReaderInterceptor;
import org.eobjects.analyzer.configuration.DefaultConfigurationReaderInterceptor;
import org.eobjects.metamodel.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration reader interceptor that is aware of the DataCleaner
 * environment.
 * 
 * @author Kasper Sørensen
 */
public class DataCleanerConfigurationReaderInterceptor extends DefaultConfigurationReaderInterceptor implements
        ConfigurationReaderInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(DataCleanerConfigurationReaderInterceptor.class);

    private final FileObject _dataCleanerHome;

    public DataCleanerConfigurationReaderInterceptor(FileObject dataCleanerHome) {
        _dataCleanerHome = dataCleanerHome;
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        ClassLoader classLoader = ExtensionPackage.getExtensionClassLoader();
        return Class.forName(className, true, classLoader);
    }

    @Override
    public String createFilename(String filename) {
        if (filename == null) {
            return null;
        }
        
        File file = new File(filename);
        if (file.isAbsolute()) {
            return filename;
        }

        try {
            FileObject fileObject = _dataCleanerHome.resolveFile(filename);
            return fileObject.getName().getPathDecoded();
        } catch (FileSystemException e) {
            logger.warn("Could not resolve absolute path using VFS: " + filename, e);
            return filename;
        }
    }

    @Override
    public String getTemporaryStorageDirectory() {
        try {
            if (_dataCleanerHome.isWriteable()) {
                return _dataCleanerHome.resolveFile("temp").getName().getPathDecoded();
            }
        } catch (FileSystemException e) {
            logger.warn("Could not resolve temp directory", e);
        }
        return FileHelper.getTempDir().getAbsolutePath();
    }

}
