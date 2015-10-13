package org.datacleaner.metamodel.datahub;

import static com.google.common.net.UrlEscapers.urlPathSegmentEscaper;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.isEmpty;

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
import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;
import org.datacleaner.util.http.MonitorHttpClient;

public class DataHubUpdateConnection {
    public final static String CONTEXT_PATH = "/service/cdi/v1";
    public final static String GOLDEN_RECORDS_PATH = "/goldenrecords";
    public final static String SOURCE_RECORDS_PATH = "/sources";
    public final static String UPDATE_PATH = GOLDEN_RECORDS_PATH + "/batch";
    public final static String DELETE_GR_PATH = GOLDEN_RECORDS_PATH + "/delete";
    public final static String DELETE_SR_PATH = SOURCE_RECORDS_PATH + "/delete";

    private final DataHubConnection _connection;

    public DataHubUpdateConnection(DataHubConnection connection) {
        _connection = connection;
    }

    public String getUpdateUrl() {
        return getContextUrl() + UPDATE_PATH + (isEmpty(_connection.getTenantId()) ? EMPTY
                : "/" + urlPathSegmentEscaper().escape(_connection.getTenantId()));
    }

    public String getDeleteGoldenRecordUrl() {
        return getContextUrl() + DELETE_GR_PATH;
    }

    public String getDeleteSourceRecordUrl() {
        return getContextUrl() + DELETE_SR_PATH;
    }

    public MonitorHttpClient getHttpClient() {
        return _connection.getHttpClient(getContextUrl());
    }

    private String getContextUrl() {
        URIBuilder uriBuilder = _connection.getBaseUrlBuilder();
        appendToPath(uriBuilder, CONTEXT_PATH);

        try {
            return uriBuilder.build().toString();
        } catch (URISyntaxException uriSyntaxException) {
            throw new IllegalStateException(uriSyntaxException);
        }
    }

    private URIBuilder appendToPath(URIBuilder uriBuilder, String pathSegment) {
        if (uriBuilder.getPath() != null) {
            uriBuilder.setPath(uriBuilder.getPath() + pathSegment);
        }

        return uriBuilder.setPath(pathSegment);
    }


}
