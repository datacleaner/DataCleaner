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
package org.datacleaner.connection;

import org.apache.metamodel.DataContext;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.data.OutputDataStreamDataContext;

/**
 * A virtual {@link Datastore} that represents/wraps a {@link OutputDataStream}.
 */
public class OutputDataStreamDatastore extends UsageAwareDatastore<DataContext> {

    private final OutputDataStream _outputDataStream;

    public OutputDataStreamDatastore(OutputDataStream outputDataStream) {
        super(outputDataStream.getName());
        _outputDataStream = outputDataStream;
    }

    private static final long serialVersionUID = 1L;

    @Override
    public PerformanceCharacteristics getPerformanceCharacteristics() {
        return _outputDataStream.getPerformanceCharacteristics();
    }

    @Override
    protected UsageAwareDatastoreConnection<DataContext> createDatastoreConnection() {
        final DataContext dataContext = new OutputDataStreamDataContext(_outputDataStream);
        return new DatastoreConnectionImpl<DataContext>(dataContext, this);
    }

}
