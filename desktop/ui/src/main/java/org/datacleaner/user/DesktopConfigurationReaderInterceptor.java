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
package org.datacleaner.user;

import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.datacleaner.configuration.DefaultConfigurationReaderInterceptor;
import org.datacleaner.extensions.ClassLoaderUtils;
import org.datacleaner.extensions.ExtensionPackage;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.vfs.VfsRepository;
import org.datacleaner.util.convert.DummyRepositoryResourceFileTypeHandler;
import org.datacleaner.util.convert.RepositoryFileResourceTypeHandler;
import org.datacleaner.util.convert.ResourceConverter.ResourceTypeHandler;

/**
 * Configuration reader interceptor that is aware of the DataCleaner
 * environment.
 */
public class DesktopConfigurationReaderInterceptor extends DefaultConfigurationReaderInterceptor {

    private final FileObject _dataCleanerHome;

    public DesktopConfigurationReaderInterceptor(FileObject dataCleanerHome) {
        _dataCleanerHome = dataCleanerHome;
    }

    @Override
    public Repository getHomeFolder() {
        return new VfsRepository(_dataCleanerHome);
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        ClassLoader classLoader = ExtensionPackage.getExtensionClassLoader();
        return Class.forName(className, true, classLoader);
    }

    @Override
    protected List<ResourceTypeHandler<?>> getResourceTypeHandlers() {
        final List<ResourceTypeHandler<?>> handlers = super.getResourceTypeHandlers();
        if (ClassLoaderUtils.IS_WEB_START) {
            handlers.add(new DummyRepositoryResourceFileTypeHandler());
        } else {
            final Repository homeFolder = getHomeFolder();
            handlers.add(new RepositoryFileResourceTypeHandler(homeFolder, homeFolder));
        }
        return handlers;
    }
}
