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
package org.datacleaner.configuration;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.util.LazyRef;
import org.datacleaner.api.ComponentContext;
import org.datacleaner.api.OutputRowCollector;
import org.datacleaner.api.Provided;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.connection.SchemaNavigator;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.job.concurrent.ThreadLocalOutputRowCollector;
import org.datacleaner.job.runner.ComponentContextImpl;
import org.datacleaner.reference.ReferenceDataCatalog;
import org.datacleaner.result.renderer.RendererFactory;
import org.datacleaner.storage.CollectionFactory;
import org.datacleaner.storage.CollectionFactoryImpl;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;
import org.datacleaner.storage.RowAnnotationHandler;
import org.datacleaner.storage.RowAnnotationSampleContainer;
import org.datacleaner.util.convert.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple injection manager implementation, which is aware of catalogs used
 * within the {@link AnalyzerBeansConfiguration}, but not anymore.
 */
public class InjectionManagerImpl implements InjectionManager {

    private static final Logger logger = LoggerFactory.getLogger(InjectionManagerImpl.class);

    private final DataCleanerConfiguration _configuration;
    private final AnalysisJob _job;
    private final Supplier<RowAnnotationFactory> _rowAnntationFactoryRef;

    /**
     * Constructs an {@link InjectionManager} for use within the scope of a job
     * execution.
     *
     * @param configuration
     * @param job
     */
    public InjectionManagerImpl(final DataCleanerConfiguration configuration, final AnalysisJob job) {
        _configuration = configuration;
        _job = job;
        _rowAnntationFactoryRef = createRowAnnotationFactoryRef();
    }

    /**
     * Creates a new {@link InjectionManager} without any job-context.
     * Convenient for use outside of an actual job, mimicing a job situation
     * etc.
     *
     * @param configuration
     */
    public InjectionManagerImpl(final DataCleanerConfiguration configuration) {
        this(configuration, null);
    }

    private Supplier<RowAnnotationFactory> createRowAnnotationFactoryRef() {
        return new LazyRef<RowAnnotationFactory>() {
            @Override
            protected RowAnnotationFactory fetch() {
                logger.info("Creating RowAnnotationFactory for job: {}", _job);
                final RowAnnotationFactory rowAnnotationFactory =
                        _configuration.getEnvironment().getStorageProvider().createRowAnnotationFactory();
                if (rowAnnotationFactory == null) {
                    throw new IllegalStateException("Storage provider returned null RowAnnotationFactory!");
                }
                return rowAnnotationFactory;
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <E> E getInstance(final InjectionPoint<E> injectionPoint) {
        final Object instance = getInstanceInternal(injectionPoint);
        if (instance == null) {
            logger.debug("Could not handle injection for injection point: {}", injectionPoint);
        }
        return (E) instance;
    }

    private DataCleanerConfiguration getConfiguration() {
        if (_configuration == null) {
            return new DataCleanerConfigurationImpl();
        }
        return _configuration;
    }

    @SuppressWarnings("deprecation")
    protected Object getInstanceInternal(final InjectionPoint<?> injectionPoint) {
        final Class<?> baseType = injectionPoint.getBaseType();
        if (baseType == ReferenceDataCatalog.class) {
            return getConfiguration().getReferenceDataCatalog();
        } else if (baseType == OutputRowCollector.class) {
            return new ThreadLocalOutputRowCollector();
        } else if (baseType == DatastoreCatalog.class) {
            return getConfiguration().getDatastoreCatalog();
        } else if (baseType == CollectionFactory.class) {
            return new CollectionFactoryImpl(getConfiguration().getEnvironment().getStorageProvider());
        } else if (baseType == RendererFactory.class) {
            return new RendererFactory(getConfiguration());
        } else if (baseType == RowAnnotationFactory.class || baseType == RowAnnotationSampleContainer.class
                || baseType == RowAnnotationHandler.class) {
            return _rowAnntationFactoryRef.get();
        } else if (baseType == RowAnnotation.class) {
            return _rowAnntationFactoryRef.get().createAnnotation();
        } else if (baseType == DataCleanerConfiguration.class) {
            return getConfiguration();
        } else if (baseType == DataCleanerEnvironment.class) {
            return getConfiguration().getEnvironment();
        } else if (baseType == DataCleanerHomeFolder.class) {
            return getConfiguration().getHomeFolder();
        } else if (baseType == TaskRunner.class) {
            return getConfiguration().getEnvironment().getTaskRunner();
        } else if (baseType == AnalysisJob.class) {
            return _job;
        } else if (baseType == StringConverter.class) {
            // create a child injection manager (instead of using 'this') to
            // ensure that any wrapping/decoration is preserved
            if (getConfiguration() == null) {
                return new StringConverter(this);
            }
            return new StringConverter(getConfiguration(), _job);
        } else if (baseType == ComponentContext.class) {
            return new ComponentContextImpl(_job);
        } else if (baseType == Datastore.class && _job != null) {
            return _job.getDatastore();
        } else if (baseType == DatastoreConnection.class && _job != null) {
            throw new UnsupportedOperationException("DatastoreConnections cannot be injected as of AnalyzerBeans 0.16. "
                    + "Inject a Datastore and manage a connection instead.");
        } else if (baseType == DataContext.class && _job != null) {
            throw new UnsupportedOperationException("DataContext cannot be injected as of AnalyzerBeans 0.16. "
                    + "Inject a Datastore and manage a connection instead.");
        } else if (baseType == SchemaNavigator.class && _job != null) {
            throw new UnsupportedOperationException("SchemaNavigator cannot be injected as of AnalyzerBeans 0.16. "
                    + "Inject a Datastore and manage a connection instead.");
        } else {
            // only inject persistent lists, sets, maps into @Provided fields.
            if (injectionPoint.getAnnotation(Provided.class) != null && injectionPoint.isGenericType()) {
                final Class<?> clazz1 = injectionPoint.getGenericTypeArgument(0);
                if (baseType == List.class) {
                    return getConfiguration().getEnvironment().getStorageProvider().createList(clazz1);
                } else if (baseType == Set.class) {
                    return getConfiguration().getEnvironment().getStorageProvider().createSet(clazz1);
                } else if (baseType == Map.class) {
                    final Class<?> clazz2 = (Class<?>) injectionPoint.getGenericTypeArgument(1);
                    return getConfiguration().getEnvironment().getStorageProvider().createMap(clazz1, clazz2);
                }
            }
        }

        // unsupported injection type
        return null;
    }

}
