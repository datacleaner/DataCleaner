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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.metamodel.util.Resource;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.configuration.DataCleanerHomeFolder;
import org.datacleaner.configuration.DataCleanerHomeFolderImpl;
import org.datacleaner.configuration.DefaultConfigurationReaderInterceptor;
import org.datacleaner.descriptors.ClasspathScanDescriptorProvider;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.extensions.ClassLoaderUtils;
import org.datacleaner.extensions.ExtensionPackage;
import org.datacleaner.job.concurrent.MultiThreadedTaskRunner;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.file.FileRepository;
import org.datacleaner.repository.vfs.VfsRepository;
import org.datacleaner.util.convert.DummyRepositoryResourceFileTypeHandler;
import org.datacleaner.util.convert.RepositoryFileResourceTypeHandler;
import org.datacleaner.util.convert.ResourceConverter.ResourceTypeHandler;

/**
 * Configuration reader interceptor that is aware of the DataCleaner
 * environment.
 */
public class DesktopConfigurationReaderInterceptor extends DefaultConfigurationReaderInterceptor {

    private static final TaskRunner TASK_RUNNER = new MultiThreadedTaskRunner();
    private static final DescriptorProvider DESCRIPTOR_PROVIDER = new ClasspathScanDescriptorProvider(TASK_RUNNER)
            .scanPackage("org.datacleaner", true).scanPackage("com.hi", true).scanPackage("com.neopost", true);
    private static final DataCleanerEnvironment BASE_ENVIRONMENT = new DataCleanerEnvironmentImpl()
            .withTaskRunner(TASK_RUNNER).withDescriptorProvider(DESCRIPTOR_PROVIDER);

    private final Repository _homeRepository;

    public DesktopConfigurationReaderInterceptor(FileObject dataCleanerHome) {
        this(new VfsRepository(dataCleanerHome));
    }

    public DesktopConfigurationReaderInterceptor(FileObject dataCleanerHome, Resource propertiesResource) {
        this(new VfsRepository(dataCleanerHome), propertiesResource);
    }

    public DesktopConfigurationReaderInterceptor(File dataCleanerHome) {
        this(new FileRepository(dataCleanerHome));
    }

    public DesktopConfigurationReaderInterceptor(File dataCleanerHome, Resource propertiesResource) {
        this(new FileRepository(dataCleanerHome), propertiesResource);
    }

    public DesktopConfigurationReaderInterceptor(Repository homeRepository) {
        this(homeRepository, null);
    }

    public DesktopConfigurationReaderInterceptor(Repository homeRepository, Resource propertiesResource) {
        super(propertiesResource, BASE_ENVIRONMENT);
        _homeRepository = homeRepository;
    }

    @Override
    public DataCleanerHomeFolder getHomeFolder() {
        return new DataCleanerHomeFolderImpl(getHomeRepository());
    }

    private Repository getHomeRepository() {
        return _homeRepository;
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        ClassLoader classLoader = ExtensionPackage.getExtensionClassLoader();
        return Class.forName(className, true, classLoader);
    }

    @Override
    protected List<ResourceTypeHandler<?>> getExtraResourceTypeHandlers() {
        final List<ResourceTypeHandler<?>> handlers = new ArrayList<>();
        if (ClassLoaderUtils.IS_WEB_START) {
            handlers.add(new DummyRepositoryResourceFileTypeHandler());
        } else {
            final Repository homeFolder = getHomeRepository();
            handlers.add(new RepositoryFileResourceTypeHandler(homeFolder, homeFolder));
        }
        return handlers;
    }
}
