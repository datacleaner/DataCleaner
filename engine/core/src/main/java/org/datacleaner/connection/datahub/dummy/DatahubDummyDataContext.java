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
package org.datacleaner.connection.datahub.dummy;

import java.util.List;

import org.apache.metamodel.MetaModelException;
import org.apache.metamodel.QueryPostprocessDataContext;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.query.FilterItem;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;

public class DatahubDummyDataContext extends QueryPostprocessDataContext implements UpdateableDataContext{

    
    @Override
    public void executeUpdate(UpdateScript arg0) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public DataSet executeQuery(final Query query) {
        Table table = query.getFromClause().getItem(0).getTable();
        return new DatahubDummyDataSet(table.getColumns());
        
    }

    @Override
    protected Number executeCountQuery(Table table, List<FilterItem> whereItems, boolean functionApproximationAllowed) {
        return 3;
    }

    @Override
    protected Schema getMainSchema() throws MetaModelException {
        return new DatahubDummySchema(getMainSchemaName());
    }

    @Override
    protected String getMainSchemaName() throws MetaModelException {
        return "Datahub";
    }

    @Override
    protected DataSet materializeMainSchemaTable(Table table, Column[] columns,
            int maxRows) {
        //executes a simple query and returns the result
//        final StringBuilder sb = new StringBuilder();
//        sb.append("SELECT ");
//        for (int i = 0; i < columns.length; i++) {
//            if (i != 0) {
//                sb.append(',');
//            }
//            sb.append(columns[i].getName());
//        }
//        sb.append(" FROM ");
//        sb.append(table.getName());
//
//        if (maxRows > 0) {
//            sb.append(" LIMIT " + maxRows);
//        }
//
//        final QueryResult queryResult = executeSoqlQuery(sb.toString());
        return new DatahubDummyDataSet(columns);
    }

}
