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
package org.datacleaner.job.runner;

import org.datacleaner.components.convert.ConvertToNumberTransformer;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.job.AnalyzerJob;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.LazyRef;
import org.apache.metamodel.util.Ref;

final class RowProcessingMetricsImpl implements RowProcessingMetrics {

    private final RowProcessingPublishers _publishers;
    private final RowProcessingPublisher _publisher;
    private final Ref<Integer> _expectedRows;

    public RowProcessingMetricsImpl(RowProcessingPublishers publishers, RowProcessingPublisher publisher) {
        _publishers = publishers;
        _publisher = publisher;
        _expectedRows = createExpectedRowsRef();
    }

    @Override
    public AnalysisJobMetrics getAnalysisJobMetrics() {
        return _publishers.getAnalysisJobMetrics();
    }

    @Override
    public Query getQuery() {
        return _publisher.getQuery();
    }

    @Override
    public Table getTable() {
        return _publisher.getTable();
    }

    @Override
    public int getExpectedRows() {
        final Integer expectedRows = _expectedRows.get();
        return expectedRows.intValue();
    }

    @Override
    public AnalyzerJob[] getAnalyzerJobs() {
        return _publisher.getAnalyzerJobs();
    }

    private Ref<Integer> createExpectedRowsRef() {
        return new LazyRef<Integer>() {

            @Override
            protected Integer fetch() {
                int expectedRows = -1;
                {
                    final Query originalQuery = getQuery();
                    final Query countQuery = originalQuery.clone();
                    countQuery.setMaxRows(null);
                    countQuery.setFirstRow(null);
                    countQuery.getSelectClause().removeItems();
                    countQuery.getOrderByClause().removeItems();
                    countQuery.selectCount();
                    countQuery.getSelectClause().getItem(0).setFunctionApproximationAllowed(true);

                    final Datastore datastore = _publisher.getDatastore();
                    try (final DatastoreConnection connection = datastore.openConnection()) {
                        try (final DataSet countDataSet = connection.getDataContext().executeQuery(countQuery)) {
                            if (countDataSet.next()) {
                                final Number count = ConvertToNumberTransformer.transformValue(countDataSet.getRow()
                                        .getValue(0));
                                if (count != null) {
                                    expectedRows = count.intValue();
                                }
                            }
                        }
                    }

                    final Integer maxRows = originalQuery.getMaxRows();
                    if (maxRows != null) {
                        expectedRows = Math.min(expectedRows, maxRows.intValue());
                    }
                }

                return expectedRows;
            }
        };
    }

}
