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
import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
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
import org.datacleaner.metamodel.datahub.utils.JsonParserHelper;
import org.datacleaner.util.http.MonitorHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatahubDataContext extends QueryPostprocessDataContext implements
        UpdateableDataContext {
    private static final Logger logger = LoggerFactory
            .getLogger(DatahubDataContext.class);

    private DatahubConnection _connection;
    
    private DatahubSchema _schema;

    public DatahubDataContext(String host, Integer port, String username,
            String password, String tenantId, boolean https) {
        _connection = new DatahubConnection(host, port, username, password,
                tenantId, https);
        _schema = getDatahubSchema();

    }

    public static boolean checkForExternal(String str) {
        int length = str.length();
        for (int i = 0; i < length; i++) {
            if (str.charAt(i) > 0x7F) {
                return true;
            }
        }
        return false;
    }

    private static final Pattern COLON = Pattern.compile("%3A", Pattern.LITERAL);
    private static final Pattern SLASH = Pattern.compile("%2F", Pattern.LITERAL);
    private static final Pattern QUEST_MARK = Pattern.compile("%3F", Pattern.LITERAL);
    private static final Pattern EQUAL = Pattern.compile("%3D", Pattern.LITERAL);
    private static final Pattern AMP = Pattern.compile("%26", Pattern.LITERAL);

    public static String encodeUrl(String url) {
//        if (checkForExternal(url)) {
                String value;
                try {
                    value = URLEncoder.encode(url, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new IllegalStateException(e);
                }
                value = COLON.matcher(value).replaceAll(":");
                value = SLASH.matcher(value).replaceAll("/");
                value = QUEST_MARK.matcher(value).replaceAll("?");
                value = EQUAL.matcher(value).replaceAll("=");
                return AMP.matcher(value).replaceAll("&");
//        } else {
//            return url;
//        }
    }
    
    private DatahubSchema getDatahubSchema() {
        List<String> datastoreNames = getDataStoreNames();
        _schema = new DatahubSchema();
        for (String datastoreName : datastoreNames) {
            // String schemaName = getMainSchemaName();
            String uri = _connection.getRepositoryUrl() + "/datastores" + "/"
                    + datastoreName + ".schemas";
            logger.debug("request {}", uri);
            HttpGet request = new HttpGet(encodeUrl(uri));
            try {
                HttpResponse response = executeRequest(request);
                String result = EntityUtils.toString(response.getEntity());
                JsonParserHelper parser = new JsonParserHelper();
                DatahubSchema schema = parser.parseJsonSchema(result);
                _schema.addTables(schema.getTables());

            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return _schema;
    }

    @Override
    public void executeUpdate(UpdateScript arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public DataSet executeQuery(final Query query) {
        Table table = query.getFromClause().getItem(0).getTable();
        // TODO dummy implementation
        return new DatahubDataSet(table.getColumns());

    }

    @Override
    protected Number executeCountQuery(Table table,
            List<FilterItem> whereItems, boolean functionApproximationAllowed) {
        // TODO dummy implementation
        return 3;
    }

    @Override
    protected Schema getMainSchema() throws MetaModelException {
        return _schema;
    }

    private List<String> getDataStoreNames() {
        String uri = _connection.getRepositoryUrl() + "/datastores";
        logger.debug("request {}", uri);
        HttpGet request = new HttpGet(uri);
        try {
            HttpResponse response = executeRequest(request);
            String result = EntityUtils.toString(response.getEntity());
            JsonParserHelper parser = new JsonParserHelper();
            List<String> datastoreNames = parser.parseDataStoreArray(result);
            return datastoreNames;

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public Schema testGetMainSchema() {
        return getMainSchema();
    }

    private HttpResponse executeRequest(HttpGet request) throws Exception {

        MonitorHttpClient httpClient = _connection.getHttpClient();
        HttpResponse response = httpClient.execute(request/*,
                _connection.getContext()*/);

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

    @Override
    protected String getMainSchemaName() throws MetaModelException {
        return "MDM";
    }

    @Override
    protected DataSet materializeMainSchemaTable(Table table, Column[] columns,
            int maxRows) {
            
//            String query = createQuery(table, columns, maxRows);
//            String datastoreName = _schema.getDataStoreName(table.getName());
//            if (datastoreName == null) {
//                //throw
//            }
//            //orderdb.query?q=
//            String uri = _connection.getRepositoryUrl() + "/datastores" + "/"
//                    + datastoreName + ".query?q=" + query;
//            logger.debug("request {}", uri);
//            HttpGet request = new HttpGet(uri);
//            request.addHeader("Accept", "application/json");
//
//            try {
//                HttpResponse response = executeRequest(request);
//                String result = EntityUtils.toString(response.getEntity());
//                System.out.println(result);
//                //JsonParserHelper parser = new JsonParserHelper();
//
//            } catch (Exception e) {
//                throw new IllegalStateException(e);
//            }
            return new DatahubDataSet(columns);
            

    }
    
    public DataSet testMaterializeMainSchemaTable(Table table, Column[] columns,
            int maxRows) {
        return materializeMainSchemaTable(table, columns, maxRows);
    }
    private String createQuery(Table table, Column[] columns, int maxRows) {
        final StringBuilder sb = new StringBuilder();
        sb.append("SELECT+*+");
//        for (int i = 0; i < columns.length; i++) {
//            if (i != 0) {
//                sb.append(',');
//            }
//            sb.append(columns[i].getName());
//        }
        sb.append("+FROM+");
        sb.append(table.getName());

//        if (maxRows > 0) {
//            sb.append(" LIMIT " + maxRows);
//        }
        return sb.toString();
    }

}