/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.monitor.shared.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eobjects.datacleaner.monitor.shared.DescriptorService;
import org.eobjects.datacleaner.monitor.shared.DescriptorServiceAsync;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.SuggestOracle;

public class StringParameterizedMetricSuggestOracle extends SuggestOracle {

    private static class Suggestion implements SuggestOracle.Suggestion {

        private final String _string;

        public Suggestion(String string) {
            _string = string;
        }

        @Override
        public String getDisplayString() {
            return _string;
        }

        @Override
        public String getReplacementString() {
            return _string;
        }

        @Override
        public String toString() {
            return _string;
        }
    }

    private Collection<String> _suggestions;

    public StringParameterizedMetricSuggestOracle(Collection<String> suggestions) {
        _suggestions = suggestions;
    }

    public StringParameterizedMetricSuggestOracle(TenantIdentifier tenant, JobIdentifier job, MetricIdentifier metric) {
        _suggestions = new ArrayList<String>();
        setMetric(tenant, job, metric);
    }
    

    public void setMetric(TenantIdentifier tenant, JobIdentifier job, MetricIdentifier metric) {
        DescriptorServiceAsync descriptorService = GWT.create(DescriptorService.class);
        descriptorService.getMetricParameterSuggestions(tenant, job, metric, new DCAsyncCallback<Collection<String>>() {
            @Override
            public void onSuccess(Collection<String> result) {
                _suggestions.clear();
                if (result == null) {
                    return;
                }
                _suggestions.addAll(result);
            }
        });
    };

    @Override
    public void requestDefaultSuggestions(Request request, Callback callback) {
        requestSuggestions("", request, callback);
    }

    @Override
    public void requestSuggestions(Request request, Callback callback) {
        String query = request.getQuery();
        requestSuggestions(query, request, callback);
    }

    private void requestSuggestions(String query, Request request, Callback callback) {
        final List<Suggestion> suggestions = new ArrayList<Suggestion>();

        // TODO: Activate when "supportsInClause" is working properly

        // if ("".equals(query) && !_suggestions.isEmpty()) {
        // suggestions.add(new Suggestion("IN " + _suggestions.toString()));
        // suggestions.add(new Suggestion("NOT IN " + _suggestions.toString()));
        // }

        for (String suggestionWord : _suggestions) {
            if (suggestionWord.toLowerCase().startsWith(query.toLowerCase())) {
                suggestions.add(new Suggestion(suggestionWord));
            }
        }

        final Response response = new Response(suggestions);
        callback.onSuggestionsReady(request, response);
    }
}
