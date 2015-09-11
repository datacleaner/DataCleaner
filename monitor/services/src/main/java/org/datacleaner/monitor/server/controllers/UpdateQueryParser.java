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
package org.datacleaner.monitor.server.controllers;

import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.query.parser.QueryParserException;
import org.apache.metamodel.query.parser.QueryPartParser;

/**
 * Very simple parser for update statements.
 * 
 * Restrictions:
 * <ul>
 * <li>The clauses in SET must be simple column=constant-value statements. No
 * functions. No assignments of one column to another.
 * <li>The WHERE clause must be a single column=constand-value statement
 * (id=53536).
 * </ul>
 */
public class UpdateQueryParser {

    private final UpdateableDataContext _dataContext;
    private final String _queryString;
    private final String _queryStringUpperCase;

    public UpdateQueryParser(UpdateableDataContext dataContext, String queryString) {
        if (dataContext == null) {
            throw new IllegalArgumentException("DataContext cannot be null");
        }
        if (queryString == null) {
            throw new IllegalArgumentException("Query string cannot be null");
        }
        _dataContext = dataContext;
        _queryString = prepareQuery(queryString);
        _queryStringUpperCase = _queryString.toUpperCase();
    }

    /**
     * Performs any preparations (not changing any semantics) to the query
     * string
     * 
     * @param queryString
     * @return
     */
    private String prepareQuery(String queryString) {
        queryString = queryString.replaceAll("[\n\r\t]", " ");
        queryString = queryString.replaceAll("  ", " ");
        queryString = queryString.trim();
        return queryString;
    }

    public UpdateQuery parseUpdate() {
        final UpdateQuery query = new UpdateQuery();

        int[] updateIndices = indexesOf("UPDATE ", null);
        int[] columnAssignmentsIndices = indexesOf("SET ", updateIndices);
        int[] whereIndices = indexesOf("WHERE ", updateIndices);

        // a few validations, minimum requirements
        if (updateIndices == null) {
            throw new QueryParserException("UPDATE not found in update query: " + _queryString);
        }
        if (columnAssignmentsIndices == null) {
            throw new QueryParserException("SET not found in query: " + _queryString);
        }

        // parse UPDATE
        {
            final String updateClause = getSubstring(getLastEndIndex(updateIndices),
                    getNextStartIndex(columnAssignmentsIndices, whereIndices));
            parseUpdateClause(query, updateClause);
        }

        {
            String columnAssignmentsClause = getSubstring(getLastEndIndex(columnAssignmentsIndices), whereIndices[0]);
            parseColumnAssignmentsClause(query, columnAssignmentsClause);
        }

        if (whereIndices != null) {
            final String whereClause = getSubstring(getLastEndIndex(whereIndices), getNextStartIndex());
            if (whereClause != null) {
                parseWhereClause(query, whereClause);
            }
        }
        return query;
    }

    private void parseUpdateClause(UpdateQuery query, String updateClause) {
        updateClause = updateClause.trim();
        if (!updateClause.isEmpty()) {
            throw new QueryParserException("The UPDATE clause must not be empty.");
        } else {
            query.setUpdatedTable(updateClause);
        }
    }

    private void parseColumnAssignmentsClause(UpdateQuery query, String columnAssignmentsClause) {
        QueryPartParser clauseParser = new QueryPartParser(new UpdateItemParser(query), columnAssignmentsClause, ",");
        clauseParser.parse();
    }

    private void parseWhereClause(UpdateQuery query, String whereClause) {
        whereClause = whereClause.trim();
        final String[] parts = whereClause.split("=");
        if (parts.length != 2) {
            throw new QueryParserException(
                    "WHERE clause is restricted to a single field = <value> term: " + whereClause);
        }
        query.setWhere(parts[0].trim(), parts[1].trim());
    }

    private String getSubstring(Integer from, int to) {
        if (from == null) {
            return null;
        }
        if (from.intValue() == to) {
            return null;
        }
        return _queryString.substring(from, to);
    }

    private int getNextStartIndex(int[]... indicesArray) {
        for (int[] indices : indicesArray) {
            if (indices != null) {
                return indices[0];
            }
        }
        return _queryString.length();
    }

    private Integer getLastEndIndex(int[]... indicesArray) {
        for (int[] indices : indicesArray) {
            if (indices != null) {
                return indices[1];
            }
        }
        return null;
    }

    /**
     * Finds the start and end indexes of a string in the query. The string
     * parameter of this method is expected to be in upper case, while the query
     * itself is tolerant of case differences.
     * 
     * @param string
     * @param previousIndices
     * @return
     */
    protected int[] indexesOf(String string, int[] previousIndices) {
        final int startIndex;
        if (previousIndices == null) {
            startIndex = _queryStringUpperCase.indexOf(string);
        } else {
            startIndex = _queryStringUpperCase.indexOf(string, previousIndices[1]);
        }
        if (startIndex == -1) {
            return null;
        }
        int endIndex = startIndex + string.length();
        return new int[] { startIndex, endIndex };
    }
}
