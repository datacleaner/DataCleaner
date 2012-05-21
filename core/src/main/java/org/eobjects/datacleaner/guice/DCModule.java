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
import org.eobjects.analyzer.beans.api.Provided;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.configuration.InjectionManager;
import org.eobjects.analyzer.configuration.InjectionManagerFactory;
import org.eobjects.analyzer.configuration.InjectionManagerFactoryImpl;
import org.eobjects.analyzer.configuration.InjectionManagerImpl;
import org.eobjects.analyzer.configuration.JaxbConfigurationReader;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.descriptors.SimpleDescriptorProvider;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.concurrent.SingleThreadedTaskRunner;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.lifecycle.LifeCycleHelper;
import org.eobjects.analyzer.reference.ReferenceDataCatalog;
import org.eobjects.analyzer.reference.ReferenceDataCatalogImpl;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.result.renderer.RendererFactory;
import org.eobjects.analyzer.storage.InMemoryStorageProvider;
import org.eobjects.analyzer.storage.StorageProvider;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.datacleaner.bootstrap.DCWindowContext;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.user.AuthenticationService;
import org.eobjects.datacleaner.user.DCAuthenticationService;
import org.eobjects.datacleaner.user.DataCleanerConfigurationReaderInterceptor;
import org.eobjects.datacleaner.user.ExtensionPackage;
import org.eobjects.datacleaner.user.MutableDatastoreCatalog;
import org.eobjects.datacleaner.user.MutableReferenceDataCatalog;
import org.eobjects.datacleaner.user.UsageLogger;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.user.UserPreferencesImpl;
import org.eobjects.datacleaner.util.HttpXmlUtils;
import org.eobjects.datacleaner.util.ResourceManager;
import org.eobjects.datacleaner.widgets.result.DCRendererInitializer;
import org.eobjects.datacleaner.windows.AnalysisJobBuilderWindow;
import org.eobjects.datacleaner.windows.AnalysisJobBuilderWindowImpl;
import org.eobjects.metamodel.util.ImmutableRef;
import org.eobjects.metamodel.util.LazyRef;
import org.eobjects.metamodel.util.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;

/**
 * Google Guice module for DataCleaner. Defines the main contextual components
 * of a DataCleaner session.
 * 
 * @author Kasper Sørensen
 */
public class DCModule extends AbstractModule {

	private static final Logger logger = LoggerFactory.getLogger(DCModule.class);

	private final Ref<AnalyzerBeansConfiguration> _configurationRef;
	private final Ref<AnalysisJobBuilder> _analysisJobBuilderRef;
	private final Ref<WindowContext> _windowContextRef;
	private final Ref<UserPreferences> _userPreferencesRef;
	private final Ref<UsageLogger> _usageLoggerRef;

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
		_configurationRef = parent._configurationRef;
		_usageLoggerRef = parent._usageLoggerRef;
		if (analysisJobBuilder == null) {
			_analysisJobBuilderRef = new LazyRef<AnalysisJobBuilder>() {
				@Override
				protected AnalysisJobBuilder fetch() {
					return new AnalysisJobBuilder(getConfiguration());
				}
			};
		} else {
			_analysisJobBuilderRef = ImmutableRef.of(analysisJobBuilder);
		}
		_windowContextRef = parent._windowContextRef;
		_userPreferencesRef = parent._userPreferencesRef;
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
		_configurationRef = createConfigurationRef(dataCleanerHome, configurationFile);
		_userPreferencesRef = createUserPreferencesRef(dataCleanerHome);
		_analysisJobBuilderRef = new LazyRef<AnalysisJobBuilder>() {
			@Override
			protected AnalysisJobBuilder fetch() {
				return new AnalysisJobBuilder(getConfiguration());
			}
		};
		_windowContextRef = new LazyRef<WindowContext>() {
			@Override
			protected WindowContext fetch() {
				UserPreferences userPreferences = getUserPreferences();
				return new DCWindowContext(getConfiguration(), userPreferences,
						getUsageLogger(getHttpXmlUtils(userPreferences)));
			}
		};
		_usageLoggerRef = new LazyRef<UsageLogger>() {
			@Override
			protected UsageLogger fetch() {
				UserPreferences userPreferences = getUserPreferences();
				return new UsageLogger(userPreferences, getHttpXmlUtils(userPreferences));
			}
		};
	}

	@Provides
	public HttpXmlUtils getHttpXmlUtils(UserPreferences userPreferences) {
		return new HttpXmlUtils(userPreferences);
	}

	@Provides
	public UsageLogger getUsageLogger(HttpXmlUtils httpXmlUtils) {
		return _usageLoggerRef.get();
	}

	private Ref<UserPreferences> createUserPreferencesRef(final File dataCleanerHome) {
		return new LazyRef<UserPreferences>() {

			@Override
			protected UserPreferences fetch() {
				final File userPreferencesFile = new File(dataCleanerHome, "userpreferences.dat");
				return UserPreferencesImpl.load(userPreferencesFile, true);
			}
		};
	}

	private Ref<AnalyzerBeansConfiguration> createConfigurationRef(final File dataCleanerHome,
			final File configurationFile) {
		return new LazyRef<AnalyzerBeansConfiguration>() {
			@Override
			protected AnalyzerBeansConfiguration fetch() {

				// load user preferences first, since we need it while reading
				// the configuration (some custom elements may refer to classes
				// within the extensions)
				final UserPreferences userPreferences = getUserPreferences();
				final List<ExtensionPackage> extensionPackages = userPreferences.getExtensionPackages();
				for (ExtensionPackage extensionPackage : extensionPackages) {
					extensionPackage.loadExtension();
				}

				// load the configuration file
				final JaxbConfigurationReader configurationReader = new JaxbConfigurationReader(
						new DataCleanerConfigurationReaderInterceptor(dataCleanerHome));

				final File file;
				if (configurationFile == null) {
					file = new File(dataCleanerHome, "conf.xml");
				} else {
					file = configurationFile;
				}

				AnalyzerBeansConfiguration c;
				try {
					c = configurationReader.create(file);
					logger.info("Succesfully read configuration from {}", file.getAbsolutePath());
				} catch (Exception ex1) {
					logger.warn("Unexpected error while reading conf.xml from DataCleanerHome!", ex1);
					logger.info("Reading conf.xml from classpath");
					try {
						c = configurationReader.create(ResourceManager.getInstance()
								.getUrl("datacleaner-home/conf.xml").openStream());
					} catch (Exception ex2) {
						logger.warn("Unexpected error while reading conf.xml from classpath!", ex2);
						logger.warn("Creating a bare-minimum configuration because of previous errors!");
						c = new AnalyzerBeansConfigurationImpl(new DatastoreCatalogImpl(),
								new ReferenceDataCatalogImpl(), new SimpleDescriptorProvider(),
								new SingleThreadedTaskRunner(), new InMemoryStorageProvider());
					}
				}

				// make the configuration mutable
				final MutableDatastoreCatalog datastoreCatalog = new MutableDatastoreCatalog(c.getDatastoreCatalog(),
						userPreferences);
				final MutableReferenceDataCatalog referenceDataCatalog = new MutableReferenceDataCatalog(
						c.getReferenceDataCatalog(), userPreferences, new LifeCycleHelper(new InjectionManagerImpl(c), null));
				final DescriptorProvider descriptorProvider = c.getDescriptorProvider();

				for (ExtensionPackage extensionPackage : extensionPackages) {
					extensionPackage.loadDescriptors(descriptorProvider);
				}

				final StorageProvider storageProvider = c.getStorageProvider();
				
				// TODO: 'c' does not have the new/improved catalogs in it at this point.

				final InjectionManagerFactory injectionManagerFactory = new InjectionManagerFactoryImpl() {
					@Override
					public InjectionManager getInjectionManager(AnalyzerBeansConfiguration configuration, AnalysisJob job) {
						InjectionManager injectionManager = super.getInjectionManager(configuration, job);
						return new DCInjectionManager(injectionManager, DCModule.this);
					}
				};

				AnalyzerBeansConfiguration configuration = new AnalyzerBeansConfigurationImpl(datastoreCatalog,
						referenceDataCatalog, descriptorProvider, c.getTaskRunner(), storageProvider,
						injectionManagerFactory);
				return configuration;
			}
		};
	}

	@Override
	protected void configure() {
		bind(DatastoreCatalog.class).toInstance(getConfiguration().getDatastoreCatalog());
		bind(ReferenceDataCatalog.class).toInstance(getConfiguration().getReferenceDataCatalog());
		bind(DescriptorProvider.class).toInstance(getConfiguration().getDescriptorProvider());
		bind(TaskRunner.class).toInstance(getConfiguration().getTaskRunner());
		bind(AnalysisJobBuilderWindow.class).to(AnalysisJobBuilderWindowImpl.class);
		bind(AuthenticationService.class).to(DCAuthenticationService.class);

		synchronized (ReflectionUtils.ANNOTATION_REFLECTION_LOCK) {
			// @Provided variants
			bind(WindowContext.class).annotatedWith(Provided.class).toProvider(new Provider<WindowContext>() {
				@Override
				public WindowContext get() {
					return getWindowContext();
				}
			});
		}
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
	public final AnalyzerBeansConfiguration getConfiguration() {
		return _configurationRef.get();
	}

	@Provides
	public AnalysisJob getAnalysisJob(@Nullable AnalysisJobBuilder builder) {
		if (builder == null) {
			return null;
		}
		return builder.toAnalysisJob();
	}

	@Provides
	public RendererFactory getRendererFactory(DescriptorProvider descriptorProvider,
			DCRendererInitializer rendererInitializer) {
		return new RendererFactory(descriptorProvider, rendererInitializer);
	}

	@Provides
	public AnalysisJobBuilder getAnalysisJobBuilder() {
		return _analysisJobBuilderRef.get();
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
	public final WindowContext getWindowContext() {
		return _windowContextRef.get();
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
