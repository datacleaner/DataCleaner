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

import org.eobjects.analyzer.beans.api.Provided;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.reference.ReferenceDataCatalog;
import org.eobjects.analyzer.result.renderer.RendererFactory;
import org.eobjects.datacleaner.bootstrap.DCWindowContext;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.widgets.result.DCRendererInitializer;
import org.eobjects.datacleaner.windows.AnalysisJobBuilderWindow;
import org.eobjects.datacleaner.windows.AnalysisJobBuilderWindowImpl;
import org.eobjects.metamodel.util.ImmutableRef;
import org.eobjects.metamodel.util.LazyRef;
import org.eobjects.metamodel.util.Ref;

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

	private final AnalyzerBeansConfiguration _configuration;
	private final AnalysisJobBuilder _analysisJobBuilder;
	private final Ref<WindowContext> _windowContext;
	private final UserPreferences _userPreferences;

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
	public DCModule(DCModule parent, AnalysisJobBuilder analysisJobBuilder) {
		_configuration = parent._configuration;
		if (analysisJobBuilder == null) {
			_analysisJobBuilder = new AnalysisJobBuilder(_configuration);
		} else {
			_analysisJobBuilder = analysisJobBuilder;
		}
		_windowContext = parent._windowContext;
		_userPreferences = parent._userPreferences;
	}

	/**
	 * Creates a DCModule that derives from an existing window context and
	 * optionally also from an existing analysis job builder.
	 * 
	 * @param configuration
	 * @param windowContext
	 * @param analysisJobBuilder
	 * @param userPreferences
	 */
	public DCModule(AnalyzerBeansConfiguration configuration, WindowContext windowContext,
			AnalysisJobBuilder analysisJobBuilder, UserPreferences userPreferences) {
		_configuration = configuration;
		_userPreferences = userPreferences;
		if (windowContext == null) {
			_windowContext = new LazyRef<WindowContext>() {
				@Override
				protected WindowContext fetch() {
					return new DCWindowContext(_configuration, _userPreferences);
				}
			};
		} else {
			_windowContext = ImmutableRef.of(windowContext);
		}

		if (analysisJobBuilder == null) {
			_analysisJobBuilder = new AnalysisJobBuilder(_configuration);
		} else {
			_analysisJobBuilder = analysisJobBuilder;
		}
	}

	/**
	 * Constructs a new DCModule based only on a configuration. New window
	 * contexts and analysis job builder will be created. Thus this constructor
	 * should only be used to create a completely new environment (at bootstrap
	 * time).
	 * 
	 * @param configuration
	 */
	public DCModule(AnalyzerBeansConfiguration configuration) {
		this(configuration, null, null, UserPreferences.getInstance());
	}

	@Override
	protected void configure() {
		bind(DatastoreCatalog.class).toInstance(_configuration.getDatastoreCatalog());
		bind(ReferenceDataCatalog.class).toInstance(_configuration.getReferenceDataCatalog());
		bind(DescriptorProvider.class).toInstance(_configuration.getDescriptorProvider());
		bind(TaskRunner.class).toInstance(_configuration.getTaskRunner());
		bind(AnalysisJobBuilderWindow.class).to(AnalysisJobBuilderWindowImpl.class);

		// @Provided variants
		bind(WindowContext.class).annotatedWith(Provided.class).toProvider(new Provider<WindowContext>() {
			@Override
			public WindowContext get() {
				return getWindowContext();
			}
		});
	}

	@Provides
	public final AnalyzerBeansConfiguration getConfiguration() {
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
	public RendererFactory getRendererFactory(DescriptorProvider descriptorProvider,
			DCRendererInitializer rendererInitializer) {
		return new RendererFactory(descriptorProvider, rendererInitializer);
	}

	@Provides
	public Datastore getDatastore() {
		return null;
	}

	@Provides
	public AnalysisJobBuilder getAnalysisJobBuilder() {
		return _analysisJobBuilder;
	}

	@Provides
	@DatastoreName
	public String getDatastoreName() {
		return null;
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
	public final WindowContext getWindowContext() {
		return _windowContext.get();
	}

	@Provides
	public final UserPreferences getUserPreferences() {
		return _userPreferences;
	}
}
