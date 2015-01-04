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

import org.apache.metamodel.DataContext;
import org.apache.metamodel.util.LazyRef;
import org.apache.metamodel.util.Ref;
import org.datacleaner.beans.api.OutputRowCollector;
import org.datacleaner.beans.api.Provided;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.job.concurrent.ThreadLocalOutputRowCollector;
import org.datacleaner.reference.ReferenceDataCatalog;
import org.datacleaner.result.renderer.RendererFactory;
import org.datacleaner.storage.CollectionFactory;
import org.datacleaner.storage.CollectionFactoryImpl;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;
import org.datacleaner.util.SchemaNavigator;
import org.datacleaner.util.convert.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple injection manager implementation, which is aware of catalogs used
 * within the {@link AnalyzerBeansConfiguration}, but not anymore.
 */
public class InjectionManagerImpl implements InjectionManager {

    private static final Logger logger = LoggerFactory.getLogger(InjectionManagerImpl.class);

    private final AnalyzerBeansConfiguration _configuration;
    private final AnalysisJob _job;
    private final Ref<RowAnnotationFactory> _rowAnntationFactoryRef;

    /**
     * Constructs an {@link InjectionManager} for use within the scope of a job
     * execution.
     * 
     * @param configuration
     * @param job
     */
    public InjectionManagerImpl(AnalyzerBeansConfiguration configuration, AnalysisJob job) {
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
    public InjectionManagerImpl(AnalyzerBeansConfiguration configuration) {
        this(configuration, null);
    }

    private Ref<RowAnnotationFactory> createRowAnnotationFactoryRef() {
        return new LazyRef<RowAnnotationFactory>() {
            @Override
            protected RowAnnotationFactory fetch() {
                logger.info("Creating RowAnnotationFactory for job: {}", _job);
                RowAnnotationFactory rowAnnotationFactory = _configuration.getStorageProvider()
                        .createRowAnnotationFactory();
                if (rowAnnotationFactory == null) {
                    throw new IllegalStateException("Storage provider returned null RowAnnotationFactory!");
                }
                return rowAnnotationFactory;
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <E> E getInstance(InjectionPoint<E> injectionPoint) {
        Object instance = getInstanceInternal(injectionPoint);
        if (instance == null) {
            logger.debug("Could not handle injection for injection point: {}", injectionPoint);
        }
        return (E) instance;
    }

    protected Object getInstanceInternal(InjectionPoint<?> injectionPoint) {
        final Class<?> baseType = injectionPoint.getBaseType();
        if (baseType == ReferenceDataCatalog.class) {
            return _configuration.getReferenceDataCatalog();
        } else if (baseType == OutputRowCollector.class) {
            return new ThreadLocalOutputRowCollector();
        } else if (baseType == DatastoreCatalog.class) {
            return _configuration.getDatastoreCatalog();
        } else if (baseType == CollectionFactory.class) {
            return new CollectionFactoryImpl(_configuration.getStorageProvider());
        } else if (baseType == RendererFactory.class) {
            return new RendererFactory(_configuration);
        } else if (baseType == RowAnnotationFactory.class) {
            return _rowAnntationFactoryRef.get();
        } else if (baseType == AnalyzerBeansConfiguration.class) {
            return _configuration;
        } else if (baseType == TaskRunner.class) {
            return _configuration.getTaskRunner();
        } else if (baseType == AnalysisJob.class) {
            return _job;
        } else if (baseType == StringConverter.class) {
            // create a child injection manager (instead of using 'this') to
            // ensure that any wrapping/decoration is preserved
            final InjectionManager childInjectionManager = (_configuration == null ? this : _configuration
                    .getInjectionManager(_job));
            return new StringConverter(childInjectionManager);
        } else if (baseType == RowAnnotation.class) {
            return _rowAnntationFactoryRef.get().createAnnotation();
        } else if (baseType == Datastore.class && _job != null) {
            return _job.getDatastore();
        } else if (baseType == DatastoreConnection.class && _job != null) {
            throw new UnsupportedOperationException(
                    "DatastoreConnections cannot be injected as of AnalyzerBeans 0.16. Inject a Datastore and manage a connection instead.");
        } else if (baseType == DataContext.class && _job != null) {
            throw new UnsupportedOperationException(
                    "DataContext cannot be injected as of AnalyzerBeans 0.16. Inject a Datastore and manage a connection instead.");
        } else if (baseType == SchemaNavigator.class && _job != null) {
            throw new UnsupportedOperationException(
                    "SchemaNavigator cannot be injected as of AnalyzerBeans 0.16. Inject a Datastore and manage a connection instead.");
        } else {
            // only inject persistent lists, sets, maps into @Provided fields.
            if (injectionPoint.getAnnotation(Provided.class) != null && injectionPoint.isGenericType()) {
                final Class<?> clazz1 = injectionPoint.getGenericTypeArgument(0);
                if (baseType == List.class) {
                    List<?> list = _configuration.getStorageProvider().createList(clazz1);
                    return list;
                } else if (baseType == Set.class) {
                    Set<?> set = _configuration.getStorageProvider().createSet(clazz1);
                    return set;
                } else if (baseType == Map.class) {
                    Class<?> clazz2 = (Class<?>) injectionPoint.getGenericTypeArgument(1);
                    Map<?, ?> map = _configuration.getStorageProvider().createMap(clazz1, clazz2);
                    return map;
                }
            }
        }

        // unsupported injection type
        return null;
    }

}
