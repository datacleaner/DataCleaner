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
package org.eobjects.datacleaner.monitor.timeline.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    }

    private Collection<String> _suggestions;

    public StringParameterizedMetricSuggestOracle(Collection<String> suggestions) {
        _suggestions = suggestions;
    }

    @Override
    public void requestSuggestions(Request request, Callback callback) {
        List<Suggestion> suggestions = new ArrayList<Suggestion>();

        String query = request.getQuery();
        if ("".equals(query)) {
            suggestions.add(new Suggestion("IN " + suggestions.toString()));
            suggestions.add(new Suggestion("NOT IN " + suggestions.toString()));
        }

        for (String suggestionWord : _suggestions) {
            if (suggestionWord.toLowerCase().startsWith(query.toLowerCase())) {
                suggestions.add(new Suggestion(suggestionWord));
            }
        }

        Response response = new Response(suggestions);
        callback.onSuggestionsReady(request, response);
    }
}
