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
package org.datacleaner.guice;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.http.client.HttpClient;
import org.apache.metamodel.util.Action;
import org.apache.metamodel.util.ImmutableRef;
import org.apache.metamodel.util.LazyRef;
import org.apache.metamodel.util.MutableRef;
import org.apache.metamodel.util.Ref;
import org.datacleaner.bootstrap.DCWindowContext;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.configuration.DataCleanerHomeFolder;
import org.datacleaner.configuration.DatastoreXmlExternalizer;
import org.datacleaner.configuration.InjectionManager;
import org.datacleaner.configuration.InjectionManagerFactory;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.descriptors.PropertyDescriptor;
import org.datacleaner.extensions.ExtensionPackage;
import org.datacleaner.extensions.ExtensionReader;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.job.runner.ReferenceDataActivationManager;
import org.datacleaner.lifecycle.LifeCycleHelper;
import org.datacleaner.reference.ReferenceDataCatalog;
import org.datacleaner.result.AnalysisResult;
import org.datacleaner.result.renderer.RendererFactory;
import org.datacleaner.storage.StorageProvider;
import org.datacleaner.user.DataCleanerConfigurationReader;
import org.datacleaner.user.DataCleanerHome;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.MutableReferenceDataCatalog;
import org.datacleaner.user.UsageLogger;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.user.UserPreferencesImpl;
import org.datacleaner.util.SystemProperties;
import org.datacleaner.util.VFSUtils;
import org.datacleaner.util.VfsResource;
import org.datacleaner.util.convert.ClasspathResourceTypeHandler;
import org.datacleaner.util.convert.DummyRepositoryResourceFileTypeHandler;
import org.datacleaner.util.convert.FileResourceTypeHandler;
import org.datacleaner.util.convert.ResourceConverter;
import org.datacleaner.util.convert.ResourceConverter.ResourceTypeHandler;
import org.datacleaner.util.convert.UrlResourceTypeHandler;
import org.datacleaner.util.convert.VfsResourceTypeHandler;
import org.datacleaner.windows.AnalysisJobBuilderWindow;
import org.datacleaner.windows.AnalysisJobBuilderWindowImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.util.Modules;

/**
 * Google Guice module for DataCleaner. Defines the main contextual components
 * of a DataCleaner session.
 */
public class DCModuleImpl extends AbstractModule implements DCModule {

    private static final Logger logger = LoggerFactory.getLogger(DCModuleImpl.class);

    private final DataCleanerConfigurationReader _undecoratedConfigurationRef;
    private final Ref<UserPreferences> _userPreferencesRef;
    private final Ref<AnalysisJobBuilder> _analysisJobBuilderRef;
    private DataCleanerConfiguration _configuration;
    private WindowContext _windowContext;

    /**
     * Creates a DCModule based on a parent module. This constructor is
     * convenient when you want to create a module with overridden getter
     * methods.
     * 
     * @param parent
     * @param analysisJobBuilder
     *            the AnalysisJobBuilder to use within this module, or null if a
     *            new AnalysisJobBuilder should be created.
     */
    public DCModuleImpl(DCModule parent, final AnalysisJobBuilder analysisJobBuilder) {
        final DCModuleImpl p = (DCModuleImpl) parent;
        _undecoratedConfigurationRef = p._undecoratedConfigurationRef;
        _userPreferencesRef = p._userPreferencesRef;
        _configuration = p._configuration;
        _windowContext = p._windowContext;
        if (analysisJobBuilder == null) {
            _analysisJobBuilderRef = new MutableRef<AnalysisJobBuilder>();
        } else {
            _analysisJobBuilderRef = ImmutableRef.of(analysisJobBuilder);
        }
    }

    public DCModuleImpl() {
        this(defaultDataCleanerHome());
    }

    private static FileObject defaultDataCleanerHome() {
        try {
            return VFSUtils.getFileSystemManager().resolveFile(".");
        } catch (FileSystemException e) {
            throw new IllegalStateException(e);
        }
    }

    public DCModuleImpl(final FileObject dataCleanerHome) {
        this(dataCleanerHome, null);
    }

    /**
     * Constructs a new DCModule based only on a DataCleaner home directory. New
     * window contexts and analysis job builder will be created. Thus this
     * constructor should only be used to create a completely new environment
     * (at bootstrap time).
     * 
     * @param dataCleanerHome
     * @param configurationFile
     *            a configuration file override, or null if not requested
     */
    public DCModuleImpl(final FileObject dataCleanerHome, FileObject configurationFile) {
        _userPreferencesRef = createUserPreferencesRef(dataCleanerHome);
        _undecoratedConfigurationRef = new DataCleanerConfigurationReader(dataCleanerHome, configurationFile,
                _userPreferencesRef);
        _analysisJobBuilderRef = new MutableRef<AnalysisJobBuilder>();
        _configuration = null;
        _windowContext = null;
    }

    private final Ref<UserPreferences> createUserPreferencesRef(final FileObject dataCleanerHome) {
        try {
            if ("true".equalsIgnoreCase(System.getProperty(SystemProperties.SANDBOX))) {
                return new ImmutableRef<UserPreferences>(new UserPreferencesImpl(null));
            }
            if (dataCleanerHome == null || !dataCleanerHome.exists()) {
                logger.info("DataCleaner home was not set or does not exist. Non-persistent user preferences will be applied.");
                return new ImmutableRef<UserPreferences>(new UserPreferencesImpl(null));
            }

            final FileObject userPreferencesFile = dataCleanerHome.resolveFile("userpreferences.dat");

            return new LazyRef<UserPreferences>() {
                @Override
                protected UserPreferences fetch() {
                    return UserPreferencesImpl.load(userPreferencesFile, true);
                }
            };
        } catch (FileSystemException e) {
            throw new IllegalStateException("Not able to resolve files in DataCleaner home: " + dataCleanerHome, e);
        }
    }

    @Override
    protected void configure() {
        bind(AnalysisJobBuilderWindow.class).to(AnalysisJobBuilderWindowImpl.class);
        bind(InjectionManagerFactory.class).to(GuiceInjectionManagerFactory.class);
        bind(DCModule.class).toInstance(this);
    }

    @Provides
    public final WindowContext getWindowContext(DataCleanerConfiguration configuration,
            UserPreferences userPreferences, UsageLogger usageLogger) {
        if (_windowContext == null) {
            synchronized (DCModuleImpl.class) {
                if (_windowContext == null) {
                    _windowContext = new DCWindowContext(configuration, userPreferences, usageLogger);
                }
            }
        }
        return _windowContext;
    }

    @Provides
    public final DataCleanerEnvironment getDataCleanerEnvironment(DataCleanerConfiguration conf) {
        return conf.getEnvironment();
    }

    @Provides
    public final TaskRunner getTaskRunner(DataCleanerEnvironment environment) {
        return environment.getTaskRunner();
    }

    @Provides
    public final DescriptorProvider getDescriptorProvider(DataCleanerEnvironment environment) {
        return environment.getDescriptorProvider();
    }

    @Provides
    public final ReferenceDataCatalog getReferenceDataCatalog(DataCleanerConfiguration conf) {
        return conf.getReferenceDataCatalog();
    }

    @Provides
    public final InjectionManager getInjectionManager(InjectionManagerFactory injectionManagerFactory,
            DataCleanerConfiguration configuration, @Nullable AnalysisJob job) {
        return injectionManagerFactory.getInjectionManager(configuration, job);
    }

    @Provides
    public final LifeCycleHelper getLifeCycleHelper(InjectionManager injectionManager,
            @Nullable ReferenceDataActivationManager referenceDataActivationManager) {
        return new LifeCycleHelper(injectionManager, referenceDataActivationManager, true);
    }

    @Provides
    public final DatastoreCatalog getDatastoreCatalog(DataCleanerConfiguration conf) {
        return conf.getDatastoreCatalog();
    }

    @Provides
    public final MutableReferenceDataCatalog getMutableReferenceDataCatalog(ReferenceDataCatalog referenceDataCatalog) {
        return (MutableReferenceDataCatalog) referenceDataCatalog;
    }

    @Provides
    public final MutableDatastoreCatalog getMutableDatastoreCatalog(DatastoreCatalog datastoreCatalog) {
        return (MutableDatastoreCatalog) datastoreCatalog;
    }

    @Provides
    @Undecorated
    public final DataCleanerConfiguration getUndecoratedAnalyzerBeansConfiguration() {
        return _undecoratedConfigurationRef.get();
    }

    @Deprecated
    @Provides
    public final org.datacleaner.configuration.AnalyzerBeansConfiguration getAnalyzerBeansConfiguration(
            @Undecorated DataCleanerConfiguration undecoratedConfiguration, UserPreferences userPreferences,
            InjectionManagerFactory injectionManagerFactory) {
        final DataCleanerConfiguration c = getDataCleanerConfiguration(undecoratedConfiguration, userPreferences,
                injectionManagerFactory);
        final DatastoreCatalog datastoreCatalog = c.getDatastoreCatalog();
        final ReferenceDataCatalog referenceDataCatalog = c.getReferenceDataCatalog();
        final DescriptorProvider descriptorProvider = c.getEnvironment().getDescriptorProvider();
        final TaskRunner taskRunner = c.getEnvironment().getTaskRunner();
        final StorageProvider storageProvider = c.getEnvironment().getStorageProvider();
        final DataCleanerHomeFolder homeFolder = c.getHomeFolder();
        return new org.datacleaner.configuration.AnalyzerBeansConfigurationImpl(datastoreCatalog, referenceDataCatalog,
                descriptorProvider, taskRunner, storageProvider, injectionManagerFactory, homeFolder);
    }

    @Provides
    public final DataCleanerConfiguration getDataCleanerConfiguration(@Undecorated DataCleanerConfiguration c,
            UserPreferences userPreferences, InjectionManagerFactory injectionManagerFactory) {
        if (_configuration == null) {
            synchronized (DCModuleImpl.class) {
                if (_configuration == null) {
                    // make the configuration mutable
                    final MutableDatastoreCatalog datastoreCatalog = new MutableDatastoreCatalog(
                            c.getDatastoreCatalog(), createDatastoreXmlExternalizer(), userPreferences);
                    final MutableReferenceDataCatalog referenceDataCatalog = new MutableReferenceDataCatalog(
                            c.getReferenceDataCatalog(), userPreferences, new LifeCycleHelper(
                                    injectionManagerFactory.getInjectionManager(c, null), null, true));
                    final DescriptorProvider descriptorProvider = c.getEnvironment().getDescriptorProvider();

                    final ExtensionReader extensionReader = new ExtensionReader();
                    final List<ExtensionPackage> internalExtensions = extensionReader.getInternalExtensions();
                    for (ExtensionPackage extensionPackage : internalExtensions) {
                        extensionPackage.loadDescriptors(descriptorProvider);
                    }

                    final List<ExtensionPackage> extensionPackages = userPreferences.getExtensionPackages();
                    for (ExtensionPackage extensionPackage : extensionPackages) {
                        extensionPackage.loadDescriptors(descriptorProvider);
                    }

                    final StorageProvider storageProvider = c.getEnvironment().getStorageProvider();

                    final TaskRunner taskRunner = c.getEnvironment().getTaskRunner();
                    final DataCleanerEnvironment environment = new DataCleanerEnvironmentImpl(taskRunner,
                            descriptorProvider, storageProvider, injectionManagerFactory);

                    _configuration = new DataCleanerConfigurationImpl(environment,
                            DataCleanerHome.getAsDataCleanerHomeFolder(), datastoreCatalog, referenceDataCatalog);
                }
            }
        }

        if (_configuration instanceof DataCleanerConfigurationImpl) {
            final DataCleanerEnvironment environment = _configuration.getEnvironment();
            if (environment.getInjectionManagerFactory() != injectionManagerFactory) {
                // Ticket #905 and #925: Always replace the injection manager
                // factory to ensure correct scope when doing injections.
                final DataCleanerEnvironment replacementEnvironment = new DataCleanerEnvironmentImpl(environment)
                        .withInjectionManagerFactory(injectionManagerFactory);
                return ((DataCleanerConfigurationImpl) _configuration).withEnvironment(replacementEnvironment);
            }
        }

        return _configuration;
    }

    private DatastoreXmlExternalizer createDatastoreXmlExternalizer() {
        final FileObject configurationFile = _undecoratedConfigurationRef.getConfigurationFile();
        if (configurationFile == null) {
            return new DatastoreXmlExternalizer();
        }
        final VfsResource resource = new VfsResource(configurationFile);
        return new DatastoreXmlExternalizer(resource) {
            @Override
            protected void onDocumentChanged(final Document document) {
                resource.write(new Action<OutputStream>() {
                    @Override
                    public void run(final OutputStream out) throws Exception {
                        final Source source = new DOMSource(document);
                        final Result outputTarget = new StreamResult(out);
                        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        final Transformer transformer = transformerFactory.newTransformer();
                        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                        transformer.transform(source, outputTarget);
                    }
                });

            }
        };
    }

    @Provides
    public AnalysisJob getAnalysisJob(@Nullable AnalysisJobBuilder builder) {
        if (builder == null) {
            return null;
        }
        return builder.toAnalysisJob(false);
    }

    @Provides
    public final RendererFactory getRendererFactory(DataCleanerConfiguration configuration) {
        return new RendererFactory(configuration);
    }

    @Provides
    public AnalysisJobBuilder getAnalysisJobBuilder(DataCleanerConfiguration configuration) {
        AnalysisJobBuilder ajb = _analysisJobBuilderRef.get();
        if (ajb == null && _analysisJobBuilderRef instanceof MutableRef) {
            ajb = new AnalysisJobBuilder(configuration);
            MutableRef<AnalysisJobBuilder> ref = (MutableRef<AnalysisJobBuilder>) _analysisJobBuilderRef;
            ref.set(ajb);
        }
        return ajb;
    }

    @Provides
    @JobFile
    public FileObject getJobFilename() {
        return null;
    }

    @Provides
    public final DCModuleImpl getModule() {
        return this;
    }

    @Provides
    public AnalysisResult getAnalysisResult() {
        return null;
    }

    @Provides
    public final UserPreferences getUserPreferences() {
        return _userPreferencesRef.get();
    }

    @Provides
    public HttpClient getHttpClient(UserPreferences userPreferences) {
        return userPreferences.createHttpClient();
    }

    @Provides
    public ResourceConverter getResourceConverter() {
        final FileObject dataCleanerHome = DataCleanerHome.get();
        final File dataCleanerHomeDirectory = VFSUtils.toFile(dataCleanerHome);

        final List<ResourceTypeHandler<?>> handlers = new ArrayList<ResourceTypeHandler<?>>();
        handlers.add(new FileResourceTypeHandler(dataCleanerHomeDirectory));
        handlers.add(new UrlResourceTypeHandler());
        handlers.add(new ClasspathResourceTypeHandler());
        handlers.add(new VfsResourceTypeHandler());
        handlers.add(new DummyRepositoryResourceFileTypeHandler());

        final ResourceConverter resourceConverter = new ResourceConverter(handlers,
                ResourceConverter.DEFAULT_DEFAULT_SCHEME);
        return resourceConverter;
    }

    @Override
    public Injector createChildInjectorForComponent(ComponentBuilder componentBuilder) {
        final ComponentBuilderModule componentBuilderModule = new ComponentBuilderModule(componentBuilder);
        final Module module = Modules.override(this).with(componentBuilderModule);
        return Guice.createInjector(module);
    }

    @Override
    public Injector createChildInjectorForProperty(ComponentBuilder componentBuilder,
            ConfiguredPropertyDescriptor propertyDescriptor) {
        final AdHocModule adHocModule = new AdHocModule();
        adHocModule.bind(PropertyDescriptor.class, propertyDescriptor);
        adHocModule.bind(ConfiguredPropertyDescriptor.class, propertyDescriptor);
        final ComponentBuilderModule componentBuilderModule = new ComponentBuilderModule(componentBuilder);
        final Module module = Modules.override(this).with(componentBuilderModule, adHocModule);
        return Guice.createInjector(module);
    }
}
