/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.metamodel.util.ImmutableRef;
import org.apache.metamodel.util.LazyRef;
import org.apache.metamodel.util.MutableRef;
import org.datacleaner.bootstrap.DCWindowContext;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.configuration.DomConfigurationWriter;
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
import org.datacleaner.lifecycle.LifeCycleHelper;
import org.datacleaner.reference.ReferenceDataCatalog;
import org.datacleaner.result.AnalysisResult;
import org.datacleaner.result.renderer.RendererFactory;
import org.datacleaner.storage.StorageProvider;
import org.datacleaner.user.DataCleanerConfigurationReader;
import org.datacleaner.user.DataCleanerHome;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.MutableReferenceDataCatalog;
import org.datacleaner.user.MutableServerInformationCatalog;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.user.UserPreferencesImpl;
import org.datacleaner.util.SystemProperties;
import org.datacleaner.util.VFSUtils;
import org.datacleaner.util.VfsResource;
import org.datacleaner.util.convert.DummyRepositoryResourceFileTypeHandler;
import org.datacleaner.util.convert.ResourceConverter;
import org.datacleaner.util.xml.XmlUtils;
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
    private final Supplier<UserPreferences> _userPreferencesRef;
    private final Supplier<AnalysisJobBuilder> _analysisJobBuilderRef;
    private DataCleanerConfiguration _configuration;
    private WindowContext _windowContext;
    private FileObject _jobFilename;
    private AnalysisResult _analysisResult;

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
    public DCModuleImpl(final DCModule parent, final AnalysisJobBuilder analysisJobBuilder) {
        final DCModuleImpl p = (DCModuleImpl) parent;
        _undecoratedConfigurationRef = p._undecoratedConfigurationRef;
        _userPreferencesRef = p._userPreferencesRef;
        _configuration = p._configuration;
        _windowContext = p._windowContext;
        if (analysisJobBuilder == null) {
            _analysisJobBuilderRef = new MutableRef<>();
        } else {
            _analysisJobBuilderRef = ImmutableRef.of(analysisJobBuilder);
        }
    }

    public DCModuleImpl() {
        this(defaultDataCleanerHome());
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
    public DCModuleImpl(final FileObject dataCleanerHome, final FileObject configurationFile) {
        _userPreferencesRef = createUserPreferencesRef(dataCleanerHome);
        _undecoratedConfigurationRef =
                new DataCleanerConfigurationReader(dataCleanerHome, configurationFile);
        _analysisJobBuilderRef = new MutableRef<>();
        _configuration = null;
        _windowContext = null;
    }

    private static FileObject defaultDataCleanerHome() {
        try {
            return VFSUtils.getFileSystemManager().resolveFile(".");
        } catch (final FileSystemException e) {
            throw new IllegalStateException(e);
        }
    }

    private Supplier<UserPreferences> createUserPreferencesRef(final FileObject dataCleanerHome) {
        try {
            if ("true".equalsIgnoreCase(System.getProperty(SystemProperties.SANDBOX))) {
                return new ImmutableRef<>(new UserPreferencesImpl(null));
            }
            if (dataCleanerHome == null || !dataCleanerHome.exists()) {
                logger.info(
                        "DataCleaner home was not set or does not exist. Non-persistent user preferences will be applied.");
                return new ImmutableRef<>(new UserPreferencesImpl(null));
            }

            final FileObject userPreferencesFile = dataCleanerHome.resolveFile(UserPreferencesImpl.DEFAULT_FILENAME);

            return new LazyRef<UserPreferences>() {
                @Override
                protected UserPreferences fetch() {
                    return UserPreferencesImpl.load(userPreferencesFile, true);
                }
            };
        } catch (final FileSystemException e) {
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
    public final WindowContext getWindowContext(final DataCleanerConfiguration configuration,
            final UserPreferences userPreferences) {
        if (_windowContext == null) {
            synchronized (DCModuleImpl.class) {
                if (_windowContext == null) {
                    _windowContext = new DCWindowContext(configuration, userPreferences);
                }
            }
        }
        return _windowContext;
    }

    @Provides
    public final DataCleanerEnvironment getDataCleanerEnvironment(final DataCleanerConfiguration conf) {
        return conf.getEnvironment();
    }

    @Provides
    public final TaskRunner getTaskRunner(final DataCleanerEnvironment environment) {
        return environment.getTaskRunner();
    }

    @Provides
    public final DescriptorProvider getDescriptorProvider(final DataCleanerEnvironment environment) {
        return environment.getDescriptorProvider();
    }

    @Provides
    public final ReferenceDataCatalog getReferenceDataCatalog(final DataCleanerConfiguration conf) {
        return conf.getReferenceDataCatalog();
    }

    @Provides
    public final InjectionManager getInjectionManager(final InjectionManagerFactory injectionManagerFactory,
            final DataCleanerConfiguration configuration, @Nullable final AnalysisJob job) {
        return injectionManagerFactory.getInjectionManager(configuration, job);
    }

    @Provides
    public final LifeCycleHelper getLifeCycleHelper(final InjectionManager injectionManager) {
        return new LifeCycleHelper(injectionManager, true);
    }

    @Provides
    public final DatastoreCatalog getDatastoreCatalog(final DataCleanerConfiguration conf) {
        return conf.getDatastoreCatalog();
    }

    @Provides
    public final MutableReferenceDataCatalog getMutableReferenceDataCatalog(
            final ReferenceDataCatalog referenceDataCatalog) {
        return (MutableReferenceDataCatalog) referenceDataCatalog;
    }

    @Provides
    public final MutableDatastoreCatalog getMutableDatastoreCatalog(final DatastoreCatalog datastoreCatalog) {
        return (MutableDatastoreCatalog) datastoreCatalog;
    }

    @Provides
    @Undecorated
    public final DataCleanerConfiguration getUndecoratedAnalyzerBeansConfiguration() {
        return _undecoratedConfigurationRef.get();
    }

    @Provides
    public final DataCleanerConfiguration getDataCleanerConfiguration(@Undecorated final DataCleanerConfiguration c,
            final UserPreferences userPreferences, final InjectionManagerFactory injectionManagerFactory) {
        if (_configuration == null) {
            synchronized (DCModuleImpl.class) {
                if (_configuration == null) {
                    // make the configuration mutable
                    final DomConfigurationWriter configurationWriter = createConfigurationWriter();
                    final MutableDatastoreCatalog datastoreCatalog =
                            new MutableDatastoreCatalog(c.getDatastoreCatalog(), configurationWriter, userPreferences);
                    final MutableReferenceDataCatalog referenceDataCatalog =
                            new MutableReferenceDataCatalog(c.getReferenceDataCatalog(), configurationWriter,
                                    userPreferences,
                                    new LifeCycleHelper(injectionManagerFactory.getInjectionManager(c, null), true));
                    final MutableServerInformationCatalog serverInformationCatalog =
                            new MutableServerInformationCatalog(c.getServerInformationCatalog(), configurationWriter);
                    final DescriptorProvider descriptorProvider = c.getEnvironment().getDescriptorProvider();

                    final ExtensionReader extensionReader = new ExtensionReader();
                    final List<ExtensionPackage> internalExtensions = extensionReader.getInternalExtensions();
                    for (final ExtensionPackage extensionPackage : internalExtensions) {
                        extensionPackage.loadDescriptors(descriptorProvider);
                    }

                    final List<ExtensionPackage> extensionPackages = userPreferences.getExtensionPackages();
                    for (final ExtensionPackage extensionPackage : extensionPackages) {
                        extensionPackage.loadDescriptors(descriptorProvider);
                    }

                    final StorageProvider storageProvider = c.getEnvironment().getStorageProvider();

                    final TaskRunner taskRunner = c.getEnvironment().getTaskRunner();

                    final DataCleanerEnvironment environment =
                            new DataCleanerEnvironmentImpl(taskRunner, descriptorProvider, storageProvider,
                                    injectionManagerFactory);

                    _configuration =
                            new DataCleanerConfigurationImpl(environment, DataCleanerHome.getAsDataCleanerHomeFolder(),
                                    datastoreCatalog, referenceDataCatalog, serverInformationCatalog);
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

    private DomConfigurationWriter createConfigurationWriter() {
        final FileObject configurationFile = _undecoratedConfigurationRef.getConfigurationFile();
        if (configurationFile == null) {
            return new DomConfigurationWriter();
        }
        final VfsResource resource = new VfsResource(configurationFile);
        return new DomConfigurationWriter(resource) {
            @Override
            protected void onDocumentChanged(final Document document) {
                resource.write(out -> XmlUtils.writeDocument(document, out));

            }
        };
    }

    @Provides
    public final AnalysisJob getAnalysisJob(@Nullable final AnalysisJobBuilder builder) {
        if (builder == null) {
            return null;
        }
        return builder.toAnalysisJob(false);
    }

    @Provides
    public final RendererFactory getRendererFactory(final DataCleanerConfiguration configuration) {
        return new RendererFactory(configuration);
    }

    @Provides
    public final AnalysisJobBuilder getAnalysisJobBuilder(final DataCleanerConfiguration configuration) {
        AnalysisJobBuilder ajb = _analysisJobBuilderRef.get();
        if (ajb == null && _analysisJobBuilderRef instanceof MutableRef) {
            ajb = new AnalysisJobBuilder(configuration);
            final MutableRef<AnalysisJobBuilder> ref = (MutableRef<AnalysisJobBuilder>) _analysisJobBuilderRef;
            ref.set(ajb);
        }
        return ajb;
    }

    @Provides
    @JobFile
    public final FileObject getJobFilename() {
        return _jobFilename;
    }
    
    public void setJobFilename(FileObject jobFilename) {
        _jobFilename = jobFilename;
    }

    @Provides
    public final DCModuleImpl getModule() {
        return this;
    }
    
    public void setAnalysisResult(AnalysisResult analysisResult) {
        _analysisResult = analysisResult;
    }

    @Provides
    public final AnalysisResult getAnalysisResult() {
        return _analysisResult;
    }

    @Provides
    public final UserPreferences getUserPreferences() {
        return _userPreferencesRef.get();
    }

    @Provides
    public final CloseableHttpClient getHttpClient(final UserPreferences userPreferences) {
        return userPreferences.createHttpClient();
    }

    @Provides
    public final ResourceConverter getResourceConverter() {
        return new ResourceConverter(_configuration)
                .withExtraHandlers(Collections.singletonList(new DummyRepositoryResourceFileTypeHandler()));
    }

    @Override
    public InjectorBuilder createInjectorBuilder() {
        return new InjectorBuilder(this, Guice.createInjector(this));
    }

    @Override
    public Injector createChildInjectorForComponent(final ComponentBuilder componentBuilder) {
        final ComponentBuilderModule componentBuilderModule = new ComponentBuilderModule(componentBuilder);
        final Module module = Modules.override(this).with(componentBuilderModule);
        return Guice.createInjector(module);
    }

    @Override
    public Injector createChildInjectorForProperty(final ComponentBuilder componentBuilder,
            final ConfiguredPropertyDescriptor propertyDescriptor) {
        final AdHocModule adHocModule = new AdHocModule();
        adHocModule.bind(PropertyDescriptor.class, propertyDescriptor);
        adHocModule.bind(ConfiguredPropertyDescriptor.class, propertyDescriptor);
        final ComponentBuilderModule componentBuilderModule = new ComponentBuilderModule(componentBuilder);
        final Module module = Modules.override(this).with(componentBuilderModule, adHocModule);
        return Guice.createInjector(module);
    }
}
