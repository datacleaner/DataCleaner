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

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.servlet.ServletContext;

import org.datacleaner.api.RenderingFormat;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.configuration.InjectionManagerFactory;
import org.datacleaner.descriptors.ClasspathScanDescriptorProvider;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.job.concurrent.MultiThreadedTaskRunner;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.restclient.Serializator;
import org.datacleaner.result.renderer.SwingRenderingFormat;
import org.datacleaner.result.renderer.TextRenderingFormat;
import org.datacleaner.storage.InMemoryStorageProvider;
import org.datacleaner.storage.StorageProvider;
import org.datacleaner.util.CollectionUtils2;
import org.datacleaner.util.ExtensionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Bean factory for {@link AnalyzerBeansConfiguration} elements in the DC
 * monitor application, like the {@link TaskRunner} and
 * {@link DescriptorProvider}.
 * 
 * Since the scannedPackages property (and to some extent also the numThreads
 * property) is meant to be externalized, this class is NOT annotated with
 * {@link Component}. Add it in the spring beans xml file.
 */
public class ConfigurationFactory {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationFactory.class);

    private List<String> _scannedPackages;
    private Integer _numThreads;
    private boolean scanWebInfFolder = true;

    public boolean isScanWebInfFolder() {
        return scanWebInfFolder;
    }

    public void setScanWebInfFolder(boolean scanWebInfFolder) {
        this.scanWebInfFolder = scanWebInfFolder;
    }

    public List<String> getScannedPackages() {
        return _scannedPackages;
    }

    public void setScannedPackages(List<String> scannedPackages) {
        _scannedPackages = scannedPackages;
    }

    public Integer getNumThreads() {
        return _numThreads;
    }

    public void setNumThreads(Integer numThreads) {
        _numThreads = numThreads;
    }

    @Bean(name = "published-components")
    public RemoteComponentsConfiguration createRemoteComponentsConfiguration() {
        return new SimpleRemoteComponentsConfigurationImpl();
    }

    @Bean(name = "taskRunner", destroyMethod = "shutdown")
    public TaskRunner createTaskRunner() {
        if (_numThreads == null) {
            throw new IllegalStateException("Number of threads have not been configured.");
        }
        logger.info("Creating shared task runner with {} threads", _numThreads);
        return new MultiThreadedTaskRunner(_numThreads);
    }

    @Bean(name = "descriptorProvider")
    public DescriptorProvider createDescriptorProvider(TaskRunner taskRunner, ServletContext servletContext) {
        final File[] files = getJarFilesForDescriptorProvider(servletContext);

        if (logger.isDebugEnabled()) {
            logger.debug("Using JAR files: {}", Arrays.toString(files));
        }

        logger.info("Creating shared descriptor provider with packages: {}", _scannedPackages);

        final Collection<Class<? extends RenderingFormat<?>>> excludedRenderingFormats = new HashSet<Class<? extends RenderingFormat<?>>>();
        excludedRenderingFormats.add(SwingRenderingFormat.class);
        excludedRenderingFormats.add(TextRenderingFormat.class);
        final ClasspathScanDescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider(taskRunner,
                excludedRenderingFormats, true);
        final ClassLoader classLoader = getClass().getClassLoader();
        logger.info("Using classloader: {}", classLoader);

        for (String packageName : _scannedPackages) {
            descriptorProvider.scanPackage(packageName, true, classLoader, false, files);
        }
        return descriptorProvider;
    }

    @Bean(name = "storageProvider")
    public StorageProvider createStorageProvider() {
        return new InMemoryStorageProvider(1000, 100);
    }

    @Bean(name = "dataCleanerEnvironment")
    public DataCleanerEnvironment createDataCleanerEnvironment(TaskRunner taskRunner,
            DescriptorProvider descriptorProvider, StorageProvider storageProvider,
            InjectionManagerFactory injectionManagerFactory) {
        return new DataCleanerEnvironmentImpl(taskRunner, descriptorProvider, storageProvider, injectionManagerFactory);
    }

    @Bean(name = "jacksonObjectMapper")
    public ObjectMapper createJacksonObjectMapper() {
        return Serializator.getJacksonObjectMapper();
    }

    private File[] getJarFilesForDescriptorProvider(final ServletContext servletContext) {
        if (!scanWebInfFolder) {
            logger.debug("scanWebInfFolder is set to false, will not attempt loading JAR files from WEB-INF");
            return null;
        }

        if (servletContext == null) {
            logger.warn("ServletContext is null, will not attempt loading JAR files from WEB-INF");
            return null;
        }

        final String classesPath = servletContext.getRealPath("/WEB-INF/classes");
        final String libPath = servletContext.getRealPath("/WEB-INF/lib");

        logger.debug("Path of 'classes': {}", classesPath);
        logger.debug("Path of 'lib': {}", libPath);

        if (classesPath == null && libPath == null) {
            logger.info("ServletContext.getRealPath(...) returned null, will not attempt loading JAR files from WEB-INF");
            return null;
        }

        final File classesDirectory = new File(classesPath);
        final File[] jarFiles = new File(libPath).listFiles(new ExtensionFilter(null, ".jar"));
        if (jarFiles == null || jarFiles.length == 0) {
            logger.debug("No JAR files found in WEB-INF/lib.");
            return null;
        }

        return CollectionUtils2.array(File.class, jarFiles, classesDirectory);
    }
}
