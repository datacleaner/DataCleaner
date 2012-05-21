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
package org.eobjects.datacleaner.guice;

import java.io.File;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.configuration.InjectionManager;
import org.eobjects.analyzer.configuration.InjectionManagerFactory;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.job.runner.ReferenceDataActivationManager;
import org.eobjects.analyzer.lifecycle.LifeCycleHelper;
import org.eobjects.analyzer.reference.ReferenceDataCatalog;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.result.renderer.RendererFactory;
import org.eobjects.analyzer.storage.StorageProvider;
import org.eobjects.datacleaner.bootstrap.DCWindowContext;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.user.AuthenticationService;
import org.eobjects.datacleaner.user.DCAuthenticationService;
import org.eobjects.datacleaner.user.DataCleanerConfigurationReader;
import org.eobjects.datacleaner.user.ExtensionPackage;
import org.eobjects.datacleaner.user.MutableDatastoreCatalog;
import org.eobjects.datacleaner.user.MutableReferenceDataCatalog;
import org.eobjects.datacleaner.user.UsageLogger;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.user.UserPreferencesImpl;
import org.eobjects.datacleaner.util.HttpXmlUtils;
import org.eobjects.datacleaner.windows.AnalysisJobBuilderWindow;
import org.eobjects.datacleaner.windows.AnalysisJobBuilderWindowImpl;
import org.eobjects.metamodel.util.ImmutableRef;
import org.eobjects.metamodel.util.LazyRef;
import org.eobjects.metamodel.util.MutableRef;
import org.eobjects.metamodel.util.Ref;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * Google Guice module for DataCleaner. Defines the main contextual components
 * of a DataCleaner session.
 * 
 * @author Kasper Sørensen
 */
public class DCModule extends AbstractModule {

    private final Ref<AnalyzerBeansConfiguration> _undecoratedConfigurationRef;
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

    public DCModule(final File dataCleanerHome) {
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
    public DCModule(final File dataCleanerHome, File configurationFile) {
        _userPreferencesRef = createUserPreferencesRef(dataCleanerHome);
        _undecoratedConfigurationRef = new DataCleanerConfigurationReader(dataCleanerHome, configurationFile,
                _userPreferencesRef);
        _analysisJobBuilderRef = new MutableRef<AnalysisJobBuilder>();
        _configuration = null;
        _windowContext = null;
    }

    private final Ref<UserPreferences> createUserPreferencesRef(final File dataCleanerHome) {
        return new LazyRef<UserPreferences>() {
            @Override
            protected UserPreferences fetch() {
                final File userPreferencesFile = new File(dataCleanerHome, "userpreferences.dat");
                return UserPreferencesImpl.load(userPreferencesFile, true);
            }
        };
    }

    @Override
    protected void configure() {
        bind(AnalysisJobBuilderWindow.class).to(AnalysisJobBuilderWindowImpl.class);
        bind(AuthenticationService.class).to(DCAuthenticationService.class);
        bind(InjectionManagerFactory.class).to(DCInjectionManagerFactory.class);
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
        return new LifeCycleHelper(injectionManager, referenceDataActivationManager);
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
                            c.getDatastoreCatalog(), userPreferences);
                    final MutableReferenceDataCatalog referenceDataCatalog = new MutableReferenceDataCatalog(
                            c.getReferenceDataCatalog(), userPreferences, new LifeCycleHelper(
                                    injectionManagerFactory.getInjectionManager(c, null), null));
                    final DescriptorProvider descriptorProvider = c.getDescriptorProvider();

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
        return _configuration;
    }

    @Provides
    public AnalysisJob getAnalysisJob(@Nullable AnalysisJobBuilder builder) {
        if (builder == null) {
            return null;
        }
        return builder.toAnalysisJob();
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
    @JobFilename
    public String getJobFilename() {
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
    public HttpClient getHttpClient(HttpXmlUtils httpXmlUtils) {
        return httpXmlUtils.getHttpClient();
    }
}
