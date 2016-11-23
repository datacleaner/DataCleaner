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
package org.datacleaner.monitor.shared.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.datacleaner.monitor.shared.DescriptorService;
import org.datacleaner.monitor.shared.DescriptorServiceAsync;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.MetricIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.SuggestOracle;

public class StringParameterizedMetricSuggestOracle extends SuggestOracle {

    private static class Suggestion implements SuggestOracle.Suggestion {

        private final String _string;

        public Suggestion(final String string) {
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

    public StringParameterizedMetricSuggestOracle(final Collection<String> suggestions) {
        _suggestions = suggestions;
    }

    public StringParameterizedMetricSuggestOracle(final TenantIdentifier tenant, final JobIdentifier job, final MetricIdentifier metric) {
        _suggestions = new ArrayList<>();
        setMetric(tenant, job, metric);
    }


    public void setMetric(final TenantIdentifier tenant, final JobIdentifier job, final MetricIdentifier metric) {
        final DescriptorServiceAsync descriptorService = GWT.create(DescriptorService.class);
        descriptorService.getMetricParameterSuggestions(tenant, job, metric, new DCAsyncCallback<Collection<String>>() {
            @Override
            public void onSuccess(final Collection<String> result) {
                _suggestions.clear();
                if (result == null) {
                    return;
                }
                _suggestions.addAll(result);
            }
        });
    }

    @Override
    public void requestDefaultSuggestions(final Request request, final Callback callback) {
        requestSuggestions("", request, callback);
    }

    @Override
    public void requestSuggestions(final Request request, final Callback callback) {
        final String query = request.getQuery();
        requestSuggestions(query, request, callback);
    }

    private void requestSuggestions(final String query, final Request request, final Callback callback) {
        final List<Suggestion> suggestions = new ArrayList<>();

        // TODO: Activate when "supportsInClause" is working properly

        // if ("".equals(query) && !_suggestions.isEmpty()) {
        // suggestions.add(new Suggestion("IN " + _suggestions.toString()));
        // suggestions.add(new Suggestion("NOT IN " + _suggestions.toString()));
        // }

        for (final String suggestionWord : _suggestions) {
            if (suggestionWord.toLowerCase().startsWith(query.toLowerCase())) {
                suggestions.add(new Suggestion(suggestionWord));
            }
        }

        final Response response = new Response(suggestions);
        callback.onSuggestionsReady(request, response);
    }
}
