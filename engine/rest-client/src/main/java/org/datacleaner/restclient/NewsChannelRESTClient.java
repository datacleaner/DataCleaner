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
package org.datacleaner.restclient;

import java.util.List;

import org.datacleaner.api.RestrictedFunctionalityException;
import org.datacleaner.api.ShortNews;

public class NewsChannelRESTClient {

    private final RESTClient restClient;
    private final String url;

    public NewsChannelRESTClient(final String url, final String dataCleanerVersion) {
        this.url = url;
        restClient = new RESTClientImpl(null, null, dataCleanerVersion);
    }

    public List<ShortNews.Item> getNews(final int count) {
        final String response = call(count);
        final ShortNews news = Serializator.shortNewsList(response);
        return news.getNewsItems();
    }

    private String call(final int count) throws RestrictedFunctionalityException {
        final String response = restClient.getResponse(RESTClient.HttpMethod.GET, url + "?count=" + count, "");
        return response;
    }
}

