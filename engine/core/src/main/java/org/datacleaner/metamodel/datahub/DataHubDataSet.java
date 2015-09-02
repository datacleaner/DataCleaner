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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.AccessControlException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Datahub dataset
 * 
 * @author hetty
 *
 */
public class DataHubDataSet extends AbstractDataSet {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataHubDataSet.class);

    private static final int PAGE_SIZE = 10000;

    private final DataHubConnection _connection;
    private final Query _query;
    private String _queryString;
    private String _uri;
    private boolean _paging;
    private Integer _nextPageFirstRow;
    private Integer _nextPageMaxRows;
    private Iterator<Object[]> _resultSetIterator;
    private Row _row;

    /**
     * Constructor
     * 
     * @param query
     * @param connection
     */
    public DataHubDataSet(Query query, DataHubConnection connection) {
        super(getSelectItems(query));
        Table table = query.getFromClause().getItem(0).getTable();
        _queryString = getQueryString(query, table);
        _query = query;
        _connection = connection;
        _uri = createEncodedUri(connection, table);
        _paging = query.getMaxRows() == null;
        _nextPageFirstRow = 1;
        _nextPageMaxRows = PAGE_SIZE;
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
            if (_paging) {
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

        String uri = _uri + createParams(firstRow, maxRows);

        HttpGet request = new HttpGet(uri);
        request.addHeader("Accept", "application/json");

        HttpResponse response = executeRequest(request);

        return getResultSet(response.getEntity());
    }

    private Iterator<Object[]> getResultSet(HttpEntity entity) {
        JsonQueryDatasetResponseParser parser = new JsonQueryDatasetResponseParser();
        try {
            List<Object[]> resultSet = parser.parseQueryResult(entity.getContent());
            return resultSet.iterator();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String createParams(final Integer firstRow, final Integer maxRows) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("q", _queryString));
        params.add(new BasicNameValuePair("f", firstRow.toString()));
        params.add(new BasicNameValuePair("m", maxRows.toString()));
        return URLEncodedUtils.format(params, "utf-8");
    }

    private static String encodeUrl(String url) {
        try {
            return URLEncoder.encode(url, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static List<SelectItem> getSelectItems(Query query) {
        return query.getSelectClause().getItems();
    }

    private String createEncodedUri(DataHubConnection connection, Table table) {
        return connection.getRepositoryUrl() + "/datastores/"
                + encodeUrl(((DataHubSchema) table.getSchema()).getDatastoreName()) + ".query?";
    }

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

        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 403) {
            throw new AccessControlException("You are not authorized to access the service");
        }
        if (statusCode == 404) {
            throw new AccessControlException("Could not connect to Datahub: not found");
        }
        if (statusCode != 200) {
            throw new IllegalStateException("Unexpected response status code: " + statusCode);
        }
        return response;
    }

}
