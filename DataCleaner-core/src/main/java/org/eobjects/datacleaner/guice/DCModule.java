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

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.reference.ReferenceDataCatalog;
import org.eobjects.datacleaner.bootstrap.DCWindowContext;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.metamodel.util.ImmutableRef;
import org.eobjects.metamodel.util.LazyRef;
import org.eobjects.metamodel.util.Ref;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * Google Guice module for DataCleaner.
 * 
 * @author Kasper Sørensen
 */
public class DCModule extends AbstractModule {

	private final AnalyzerBeansConfiguration _configuration;
	private final Ref<WindowContext> _windowContext;

	public DCModule(AnalyzerBeansConfiguration configuration, WindowContext windowContext) {
		_configuration = configuration;
		if (windowContext == null) {
			_windowContext = new LazyRef<WindowContext>() {
				@Override
				protected WindowContext fetch() {
					return new DCWindowContext(_configuration);
				}
			};
		} else {
			_windowContext = ImmutableRef.of(windowContext);
		}
	}

	public DCModule(AnalyzerBeansConfiguration configuration) {
		this(configuration, null);
	}

	@Override
	protected void configure() {
		bind(AnalyzerBeansConfiguration.class).toInstance(_configuration);
		bind(DatastoreCatalog.class).toInstance(_configuration.getDatastoreCatalog());
		bind(ReferenceDataCatalog.class).toInstance(_configuration.getReferenceDataCatalog());
		bind(DescriptorProvider.class).toInstance(_configuration.getDescriptorProvider());
		bind(TaskRunner.class).toInstance(_configuration.getTaskRunner());

		// optional bindings are all set to null to begin with
		// bind(AnalysisJobBuilder.class).toProvider(Providers.<AnalysisJobBuilder>
		// of(null));
		// bind(String.class).annotatedWith(JobFilename.class).toProvider(Providers.<String>
		// of(null));
		// bind(String.class).annotatedWith(DatastoreName.class).toProvider(Providers.<String>
		// of(null));
		// bind(Datastore.class).toProvider(Providers.<Datastore> of(null));
	}

	@Provides
	public Datastore getDatastore() {
		return null;
	}

	@Provides
	public AnalysisJobBuilder getAnalysisJobBuilder() {
		return null;
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
	protected WindowContext getWindowContext() {
		return _windowContext.get();
	}

	@Provides
	protected UserPreferences getUserPreferences() {
		return UserPreferences.getInstance();
	}
}
