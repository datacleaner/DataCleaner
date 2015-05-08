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
package org.datacleaner.data;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.MetaModelException;
import org.apache.metamodel.QueryPostprocessDataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.EmptyDataSet;
import org.apache.metamodel.schema.AbstractSchema;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.datacleaner.api.OutputDataStream;

/**
 * A virtual {@link DataContext} that represents/wraps a
 * {@link OutputDataStream}.
 */
public class OutputDataStreamDataContext extends QueryPostprocessDataContext {

    private final OutputDataStream _outputDataStream;

    public OutputDataStreamDataContext(OutputDataStream outputDataStream) {
        _outputDataStream = outputDataStream;
    }

    @Override
    protected Schema getMainSchema() throws MetaModelException {
        return new AbstractSchema() {
            private static final long serialVersionUID = 1L;

            @Override
            public String getQuote() {
                return null;
            }

            @Override
            public Table[] getTables() {
                return new Table[] { _outputDataStream.getTable() };
            }

            @Override
            public String getName() {
                return null;
            }
        };
    }

    @Override
    protected String getMainSchemaName() throws MetaModelException {
        return _outputDataStream.getName();
    }

    @Override
    protected DataSet materializeMainSchemaTable(Table table, Column[] columns, int maxRows) {
        return new EmptyDataSet(columns);
    }

}
