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
package org.datacleaner.metamodel.datahub;

import static org.apache.http.HttpHeaders.ACCEPT;
import static org.datacleaner.metamodel.datahub.DataHubConnectionHelper.validateReponseStatusCode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.metamodel.data.AbstractDataSet;
import org.apache.metamodel.data.DefaultRow;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.query.SelectItem;
import org.apache.metamodel.schema.Table;
import org.datacleaner.metamodel.datahub.utils.JsonQueryDatasetResponseParser;
import org.datacleaner.util.http.MonitorHttpClient;

/**
 * Datahub dataset
 */
public class DataHubDataSet extends AbstractDataSet {

    private static final int PAGE_SIZE = 10000;
    
    private static final String JSON_CONTENT_TYPE = "application/json";
    
    private static final String QUERY_PARAM = "q";
    private static final String FIRST_ROW_PARAM = "f";
    private static final String MAX_ROW_PARAM = "m";

    private final DataHubRepoConnection _connection;
    private final Query _query;
    private String _queryString;
    private String _uri;
    private boolean _paging;
    private Integer _nextPageFirstRow;
    private Integer _nextPageMaxRows;
    private Iterator<Object[]> _resultSetIterator;
    private Row _row;

    private boolean _endReached;

    /**
     * Constructor
     * 
     * @param query
     * @param connection
     */
    public DataHubDataSet(String tenantName, Query query, DataHubRepoConnection connection) {
        super(getSelectItems(query));
        Table table = query.getFromClause().getItem(0).getTable();
        _queryString = getQueryString(query, table);
        _query = query;
        _connection = connection;
        _uri = createEncodedUri(tenantName, table);
        _paging = query.getMaxRows() == null;
        _nextPageFirstRow = 1;
        _nextPageMaxRows = PAGE_SIZE;
        _endReached = false;
        _resultSetIterator = getNextPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row getRow() {
        return _row;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean next() {
        if (!_resultSetIterator.hasNext()) {
            if (_paging && !_endReached) {
                _resultSetIterator = getNextPage();
                if (!_resultSetIterator.hasNext()) {
                    _row = null;
                    return false;
                }
            } else {
                _row = null;
                return false;
            }
        }
        _row = new DefaultRow(getHeader(), _resultSetIterator.next());
        return true;
    }

    private Iterator<Object[]> getNextPage() {
        final Integer firstRow = (_query.getFirstRow() == null ? _nextPageFirstRow : _query.getFirstRow());
        final Integer maxRows = (_query.getMaxRows() == null ? _nextPageMaxRows : _query.getMaxRows());

        _nextPageFirstRow = _nextPageFirstRow + _nextPageMaxRows;

        final String uri = _uri + createParams(firstRow, maxRows);

        final HttpGet request = new HttpGet(uri);
        request.addHeader(ACCEPT, JSON_CONTENT_TYPE);
        final HttpResponse response = executeRequest(request);
        final List<Object[]> resultSet = getResultSet(response.getEntity());
        final int resultSetSize = resultSet.size();
        _endReached = (resultSetSize < maxRows);
        return resultSet.iterator();
    }

    private List<Object[]> getResultSet(HttpEntity entity) {
        JsonQueryDatasetResponseParser parser = new JsonQueryDatasetResponseParser();
        try {
            List<Object[]> resultSet = parser.parseQueryResult(entity.getContent());
            return resultSet;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String createParams(final Integer firstRow, final Integer maxRows) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(QUERY_PARAM, _queryString));
        params.add(new BasicNameValuePair(FIRST_ROW_PARAM, firstRow.toString()));
        params.add(new BasicNameValuePair(MAX_ROW_PARAM, maxRows.toString()));
        return URLEncodedUtils.format(params, "utf-8");
    }

    private static List<SelectItem> getSelectItems(Query query) {
        return query.getSelectClause().getItems();
    }

    private String createEncodedUri(String tenantName, Table table) {
        String datastoreName = ((DataHubSchema) table.getSchema()).getDatastoreName();
        
        return _connection.getQueryUrl(tenantName, datastoreName);
    }

    /**
     * Changes all occurences of <table-name>.<column-name> with just <column-name>
     * 
     * @param query The original query
     * @param table The table from the query.
     * @return The Query string with the qualified table names removed.
     */
    private String getQueryString(Query query, Table table) {
        String queryString = query.toSql();
        return queryString.replace(table.getName() + ".", "");
    }


    private HttpResponse executeRequest(HttpGet request) {
        MonitorHttpClient httpClient = _connection.getHttpClient();
        HttpResponse response;
        try {
            response = httpClient.execute(request);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        validateReponseStatusCode(response);
        
        return response;
    }

}
