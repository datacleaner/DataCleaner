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
public class DatahubDataSet extends AbstractDataSet {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(DatahubDataSet.class);

    private final Query _query;
    private final DatahubConnection _connection;
    private Iterator<Object[]> _resultSetIterator;

    private Row _row;
    private boolean _closed;

    private String _uri;
    private Integer _pagedFirstRow;
    private Integer _pagedMaxRows;

    private String _queryString;

    
    /**
     * Constructor used for regular query execution.
     * 
     * @param query
     * @param jdbcDataContext
     * @param connection
     * @param statement
     * @param resultSet
     */
    public DatahubDataSet(String uri, Query query, String queryString, DatahubConnection connection) {
        super(query.getSelectClause().getItems());
        // if (query == null) {
        // throw new IllegalArgumentException("Arguments cannot be null");
        // }
        _uri = uri;
        _queryString = queryString;
        _query = query;
        _connection = connection;
        _pagedFirstRow = 1;
        _pagedMaxRows = 10000;
        _resultSetIterator = getBatch();
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
        Object[] values = null;
        if (!_resultSetIterator.hasNext()) {
            _resultSetIterator = getBatch();
            if (!_resultSetIterator.hasNext()) {
                _row = null;
                return false;
            }
        }
        values = _resultSetIterator.next();            
//        if (values == null) {
//            values = _resultSetIterator.next();
//        }
//        if (values == null) {
//            _row = null;
//            return false;
//        }
        _row = new DefaultRow(getHeader(), values);
        return true;
    }

    private Iterator<Object[]> getBatch() {
      final Integer firstRow = (_query.getFirstRow() == null ? _pagedFirstRow : _query.getFirstRow());
      final Integer maxRows = (_query.getMaxRows() == null ? _pagedMaxRows : _query.getMaxRows());
      
      _pagedFirstRow = _pagedFirstRow + _pagedMaxRows;

      List<NameValuePair> params = new ArrayList<>();
      params.add(new BasicNameValuePair("q", _queryString));
      params.add(new BasicNameValuePair("f", firstRow.toString()));
      params.add(new BasicNameValuePair("m", maxRows.toString()));
      String paramString = URLEncodedUtils.format(params, "utf-8");

      String uri = _uri + paramString;
      
      HttpGet request = new HttpGet(uri);
      request.addHeader("Accept", "application/json");
      HttpResponse response = executeRequest(request);
      HttpEntity entity = response.getEntity();
      JsonQueryDatasetResponseParser parser = new JsonQueryDatasetResponseParser();
      try {
          return parser.parseQueryResult(entity.getContent()).iterator();
      } catch (Exception e) {
          throw new IllegalStateException(e);
      }
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
            throw new AccessControlException(
                    "You are not authorized to access the service");
        }
        if (statusCode == 404) {
            throw new AccessControlException(
                    "Could not connect to Datahub: not found");
        }
        if (statusCode != 200) {
            throw new IllegalStateException("Unexpected response status code: "
                    + statusCode);
        }
        return response;
    }

}
