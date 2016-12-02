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
package org.datacleaner.reference.regexswap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.HttpClient;
import org.datacleaner.util.http.HttpXmlUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Client class for the RegexSwap, which allows for easy retrieval of shared
 * regular expressions.
 *
 * @author Kasper Sørensen
 */
public final class RegexSwapClient {

    public static final String CATEGORIES_URL = "https://datacleaner.org/ws/categories";
    public static final String REGEXES_URL = "https://datacleaner.org/ws/regexes";

    private final Map<String, Category> _categories = new HashMap<>();
    private final Map<String, Regex> _regexes = new HashMap<>();
    private final HttpClient _httpClient;

    public RegexSwapClient(final HttpClient httpClient) {
        _httpClient = httpClient;
    }

    public Category getCategoryByName(final String name) {
        Category category = _categories.get(name);
        if (category == null) {
            refreshCategories();
            category = _categories.get(name);
        }
        return category;
    }

    public Regex getRegexByName(final String name) {
        Regex regex = _regexes.get(name);
        if (regex == null) {
            refreshRegexes();
            regex = _regexes.get(name);
        }
        return regex;
    }

    public void refreshRegexes() {
        final Element rootNode = HttpXmlUtils.getRootNode(_httpClient, REGEXES_URL);
        final List<Node> regexNodes = HttpXmlUtils.getChildNodesByName(rootNode, "regex");
        for (final Node node : regexNodes) {
            createRegex((Element) node);
        }
    }

    public Collection<Category> getCategories() {
        if (_categories.isEmpty()) {
            refreshCategories();
        }
        return _categories.values();
    }

    public void refreshCategories() {
        final Element rootNode = HttpXmlUtils.getRootNode(_httpClient, CATEGORIES_URL);
        final List<Node> categoryNodes = HttpXmlUtils.getChildNodesByName(rootNode, "category");
        for (final Node categoryNode : categoryNodes) {
            final String name = HttpXmlUtils.getChildNodeText(categoryNode, "name");
            final String description = HttpXmlUtils.getChildNodeText(categoryNode, "description");
            final String detailsUrl = HttpXmlUtils.getChildNodeText(categoryNode, "detailsUrl");

            final Category category = new Category(name, description, detailsUrl);
            _categories.put(name, category);
        }
    }

    private Regex createRegex(final Element regexNode) {
        final String name = HttpXmlUtils.getChildNodeText(regexNode, "name");
        final String description = HttpXmlUtils.getChildNodeText(regexNode, "description");
        final String expression = HttpXmlUtils.getChildNodeText(regexNode, "expression");
        final String author = HttpXmlUtils.getChildNodeText(regexNode, "author");
        final long timestamp = Long.parseLong(HttpXmlUtils.getChildNodeText(regexNode, "timestamp"));
        final int positiveVotes = Integer.parseInt(HttpXmlUtils.getChildNodeText(regexNode, "positiveVotes"));
        final int negativeVotes = Integer.parseInt(HttpXmlUtils.getChildNodeText(regexNode, "negativeVotes"));
        final String detailsUrl = HttpXmlUtils.getChildNodeText(regexNode, "detailsUrl");
        final List<Category> categories = new ArrayList<>();
        final List<Node> categoriesNodes = HttpXmlUtils.getChildNodesByName(regexNode, "categories");
        if (!categoriesNodes.isEmpty()) {
            final Node categoriesNode = categoriesNodes.get(0);
            final List<Node> categoryNodes = HttpXmlUtils.getChildNodesByName(categoriesNode, "category");
            for (final Node categoryNode : categoryNodes) {
                final String categoryName = HttpXmlUtils.getText(categoryNode);
                final Category category = getCategoryByName(categoryName);
                if (category != null) {
                    categories.add(category);
                }
            }
        }
        final Regex regex =
                new Regex(name, description, expression, author, timestamp, positiveVotes, negativeVotes, detailsUrl,
                        categories);
        _regexes.put(name, regex);
        return regex;
    }

    public Regex refreshRegex(Regex regex) {
        final String detailsUrl = regex.getDetailsUrl();
        final Element regexNode = HttpXmlUtils.getRootNode(_httpClient, detailsUrl);
        regex = createRegex(regexNode);
        return regex;
    }

    public List<Regex> getRegexes(final Category category) {
        final List<Regex> regexes = new ArrayList<>();
        final Node rootNode = HttpXmlUtils.getRootNode(_httpClient, category.getDetailsUrl());
        final List<Node> regexNodes = HttpXmlUtils.getChildNodesByName(rootNode, "regex");
        for (final Node regexNode : regexNodes) {

            final String name = HttpXmlUtils.getChildNodeText(regexNode, "name");
            final String description = HttpXmlUtils.getChildNodeText(regexNode, "description");
            final String expression = HttpXmlUtils.getChildNodeText(regexNode, "expression");
            final String author = HttpXmlUtils.getChildNodeText(regexNode, "author");
            final long timestamp = Long.parseLong(HttpXmlUtils.getChildNodeText(regexNode, "timestamp"));
            final int positiveVotes = Integer.parseInt(HttpXmlUtils.getChildNodeText(regexNode, "positiveVotes"));
            final int negativeVotes = Integer.parseInt(HttpXmlUtils.getChildNodeText(regexNode, "negativeVotes"));
            final String detailsUrl = HttpXmlUtils.getChildNodeText(regexNode, "detailsUrl");

            final List<Category> categories;
            Regex regex = _regexes.get(name);
            if (regex == null) {
                categories = new ArrayList<>();
                regex = new Regex(name, description, expression, author, timestamp, positiveVotes, negativeVotes,
                        detailsUrl, categories);
            } else {
                categories = regex.getCategories();
                if (!categories.contains(category)) {
                    categories.add(category);
                }
            }

            _regexes.put(name, regex);

            regexes.add(regex);
        }
        return regexes;
    }
}
