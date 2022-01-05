/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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
package org.datacleaner.reference.regexswap;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.util.EntityUtils;
import org.datacleaner.util.SystemProperties;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Client class for the RegexSwap, which allows for easy retrieval of shared regular expressions.
 */
public final class RegexSwapClient {

    private static final URI REGEXES_URI = URI.create(SystemProperties.getString("org.datacleaner.regexswap.url",
            "https://datacleaner.github.io/content/regexes.json"));

    private final Map<String, Category> _categories = new HashMap<>();
    private final Map<String, Regex> _regexes = new HashMap<>();
    private final HttpClient _httpClient;

    public RegexSwapClient(final HttpClient httpClient) {
        _httpClient = httpClient;
    }

    public Category getCategoryByName(final String name) {
        if (_regexes.isEmpty()) {
            refreshRegexes();
        }
        return _categories.get(name);
    }

    public Regex getRegexByName(final String name) {
        if (_regexes.isEmpty()) {
            refreshRegexes();
        }
        return _regexes.get(name);
    }

    @SuppressWarnings("unchecked")
    public void refreshRegexes() {
        final HttpUriRequest request = RequestBuilder.get(REGEXES_URI).build();
        final Map<String, ?> root;
        try {
            final HttpResponse response = _httpClient.execute(request);
            final String json = EntityUtils.toString(response.getEntity());
            final ObjectMapper objectMapper = new ObjectMapper();
            root = objectMapper.readValue(json, Map.class);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to fetch regular expressions from " + REGEXES_URI, e);
        }
        final List<?> regexes = (List<?>) root.get("regexes");
        for (Object node : regexes) {
            createRegex((Map<String, ?>) node);
        }
    }

    public Collection<Category> getCategories() {
        if (_regexes.isEmpty()) {
            refreshRegexes();
        }
        return _categories.values();
    }

    private Regex createRegex(final Map<String, ?> node) {
        final String name = (String) node.get("name");
        final String description = (String) node.get("description");
        final String expression = (String) node.get("expression");
        @SuppressWarnings("unchecked") final List<String> tags = (List<String>) node.get("tags");
        final List<Category> categories = new ArrayList<>();
        for (final String tag : tags) {
            Category category = _categories.get(tag);
            if (category == null) {
                category = new Category(tag);
                _categories.put(tag, category);
            }
            categories.add(category);
        }
        final Regex regex = new Regex(name, description, expression, categories);
        _regexes.put(name, regex);
        return regex;
    }

    public List<Regex> getRegexes(final Category category) {
        return _regexes.values().stream().filter(r -> r.getCategories().contains(category))
                .collect(Collectors.toList());
    }
}
