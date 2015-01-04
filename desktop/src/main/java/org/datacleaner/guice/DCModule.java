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
import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.configuration.AnalyzerBeansConfigurationImpl;
import org.datacleaner.configuration.DatastoreXmlExternalizer;
import org.datacleaner.configuration.InjectionManager;
import org.datacleaner.configuration.InjectionManagerFactory;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.job.runner.ReferenceDataActivationManager;
import org.datacleaner.lifecycle.LifeCycleHelper;
import org.datacleaner.reference.ReferenceDataCatalog;
import org.datacleaner.result.AnalysisResult;
import org.datacleaner.result.renderer.RendererFactory;
import org.datacleaner.storage.StorageProvider;
import org.datacleaner.util.VFSUtils;
import org.datacleaner.util.VfsResource;
import org.datacleaner.util.convert.ClasspathResourceTypeHandler;
import org.datacleaner.util.convert.FileResourceTypeHandler;
import org.datacleaner.util.convert.ResourceConverter;
import org.datacleaner.util.convert.ResourceConverter.ResourceTypeHandler;
import org.datacleaner.util.convert.UrlResourceTypeHandler;
import org.datacleaner.util.convert.VfsResourceTypeHandler;
import org.datacleaner.bootstrap.DCWindowContext;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.extensions.ExtensionReader;
import org.datacleaner.user.AuthenticationService;
import org.datacleaner.user.DCAuthenticationService;
import org.datacleaner.user.DataCleanerConfigurationReader;
import org.datacleaner.user.DataCleanerHome;
import org.datacleaner.user.DummyRepositoryResourceFileTypeHandler;
import org.datacleaner.user.ExtensionPackage;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.MutableReferenceDataCatalog;
import org.datacleaner.user.UsageLogger;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.user.UserPreferencesImpl;
import org.datacleaner.util.SystemProperties;
import org.datacleaner.windows.AnalysisJobBuilderWindow;
import org.datacleaner.windows.AnalysisJobBuilderWindowImpl;
import org.apache.metamodel.util.Action;
import org.apache.metamodel.util.ImmutableRef;
import org.apache.metamodel.util.LazyRef;
import org.apache.metamodel.util.MutableRef;
import org.apache.metamodel.util.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * Google Guice module for DataCleaner. Defines the main contextual components
 * of a DataCleaner session.
 */
public class DCModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(DCModule.class);

    private final DataCleanerConfigurationReader _undecoratedConfigurationRef;
    private final Ref<UserPreferences> _userPreferencesRef;
    private final Ref<AnalysisJobBuilder> _analysisJobBuilderRef;
    private AnalyzerBeansConfiguration _configuration;
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
    public DCModule(DCModule parent, final AnalysisJobBuilder analysisJobBuilder) {
        _undecoratedConfigurationRef = parent._undecoratedConfigurationRef;
        _userPreferencesRef = parent._userPreferencesRef;
        _configuration = parent._configuration;
        _windowContext = parent._windowContext;
        if (analysisJobBuilder == null) {
            _analysisJobBuilderRef = new MutableRef<AnalysisJobBuilder>();
        } else {
            _analysisJobBuilderRef = ImmutableRef.of(analysisJobBuilder);
        }
    }

    public DCModule() {
        this(defaultDataCleanerHome());
    }

    private static FileObject defaultDataCleanerHome() {
        try {
            return VFSUtils.getFileSystemManager().resolveFile(".");
        } catch (FileSystemException e) {
            throw new IllegalStateException(e);
        }
    }

    public DCModule(final FileObject dataCleanerHome) {
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
     * 
     */
    public DCModule(final FileObject dataCleanerHome, FileObject configurationFile) {
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
        bind(AuthenticationService.class).to(DCAuthenticationService.class);
        bind(InjectionManagerFactory.class).to(GuiceInjectionManagerFactory.class);
    }

    @Provides
    public final WindowContext getWindowContext(AnalyzerBeansConfiguration configuration,
            UserPreferences userPreferences, UsageLogger usageLogger) {
        if (_windowContext == null) {
            synchronized (DCModule.class) {
                if (_windowContext == null) {
                    _windowContext = new DCWindowContext(configuration, userPreferences, usageLogger);
                }
            }
        }
        return _windowContext;
    }

    @Provides
    public final TaskRunner getTaskRunner(AnalyzerBeansConfiguration conf) {
        return conf.getTaskRunner();
    }

    @Provides
    public final DescriptorProvider getDescriptorProvider(AnalyzerBeansConfiguration conf) {
        return conf.getDescriptorProvider();
    }

    @Provides
    public final ReferenceDataCatalog getReferenceDataCatalog(AnalyzerBeansConfiguration conf) {
        return conf.getReferenceDataCatalog();
    }

    @Provides
    public final InjectionManager getInjectionManager(InjectionManagerFactory injectionManagerFactory,
            AnalyzerBeansConfiguration configuration, @Nullable AnalysisJob job) {
        return injectionManagerFactory.getInjectionManager(configuration, job);
    }

    @Provides
    public final LifeCycleHelper getLifeCycleHelper(InjectionManager injectionManager,
            @Nullable ReferenceDataActivationManager referenceDataActivationManager) {
        return new LifeCycleHelper(injectionManager, referenceDataActivationManager, true);
    }

    @Provides
    public final DatastoreCatalog getDatastoreCatalog(AnalyzerBeansConfiguration conf) {
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
    public final AnalyzerBeansConfiguration getUndecoratedAnalyzerBeansConfiguration() {
        return _undecoratedConfigurationRef.get();
    }

    @Provides
    public final AnalyzerBeansConfiguration getAnalyzerBeansConfiguration(@Undecorated AnalyzerBeansConfiguration c,
            UserPreferences userPreferences, InjectionManagerFactory injectionManagerFactory) {
        if (_configuration == null) {
            synchronized (DCModule.class) {
                if (_configuration == null) {
                    // make the configuration mutable
                    final MutableDatastoreCatalog datastoreCatalog = new MutableDatastoreCatalog(
                            c.getDatastoreCatalog(), createDatastoreXmlExternalizer(), userPreferences);
                    final MutableReferenceDataCatalog referenceDataCatalog = new MutableReferenceDataCatalog(
                            c.getReferenceDataCatalog(), userPreferences, new LifeCycleHelper(
                                    injectionManagerFactory.getInjectionManager(c, null), null, true));
                    final DescriptorProvider descriptorProvider = c.getDescriptorProvider();

                    final ExtensionReader extensionReader = new ExtensionReader();
                    final List<ExtensionPackage> internalExtensions = extensionReader.getInternalExtensions();
                    for (ExtensionPackage extensionPackage : internalExtensions) {
                        extensionPackage.loadDescriptors(descriptorProvider);
                    }

                    final List<ExtensionPackage> extensionPackages = userPreferences.getExtensionPackages();
                    for (ExtensionPackage extensionPackage : extensionPackages) {
                        extensionPackage.loadDescriptors(descriptorProvider);
                    }

                    final StorageProvider storageProvider = c.getStorageProvider();

                    _configuration = new AnalyzerBeansConfigurationImpl(datastoreCatalog, referenceDataCatalog,
                            descriptorProvider, c.getTaskRunner(), storageProvider, injectionManagerFactory);
                }
            }
        }

        if (_configuration instanceof AnalyzerBeansConfigurationImpl) {
            // Ticket #905 and #925: Always replace the injection manager
            // factory to ensure correct scope when doing injections.
            return ((AnalyzerBeansConfigurationImpl) _configuration).replace(injectionManagerFactory);
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
    public final RendererFactory getRendererFactory(AnalyzerBeansConfiguration configuration) {
        return new RendererFactory(configuration);
    }

    @Provides
    public AnalysisJobBuilder getAnalysisJobBuilder(AnalyzerBeansConfiguration configuration) {
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
    public final DCModule getModule() {
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
}
