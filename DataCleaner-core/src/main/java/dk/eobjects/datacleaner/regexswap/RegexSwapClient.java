/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.datacleaner.regexswap;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import dk.eobjects.datacleaner.util.DomHelper;

public class RegexSwapClient {

	public static final String CATEGORIES_URL = "http://datacleaner.eobjects.org/ws/categories";
	public static final String REGEXES_URL = "http://datacleaner.eobjects.org/ws/regexes";

	private Map<String, Category> _categories = new HashMap<String, Category>();
	private Map<String, Regex> _regexes = new HashMap<String, Regex>();
	private HttpClient _httpClient;

	public RegexSwapClient() {
		this(new HttpClient());
	}

	public RegexSwapClient(HttpClient httpClient) {
		_httpClient = httpClient;
	}

	public Map<String, Category> getCategories() {
		return _categories;
	}

	public Map<String, Regex> getRegexes() {
		return _regexes;
	}

	public void updateCategories() throws IOException {
		Element rootNode = getRootNode(CATEGORIES_URL);
		List<Node> categoryNodes = DomHelper.getChildNodesByName(rootNode,
				"category");
		for (Node categoryNode : categoryNodes) {
			String name = DomHelper.getChildNodeText(categoryNode, "name");
			String description = DomHelper.getChildNodeText(categoryNode,
					"description");
			String detailsUrl = DomHelper.getChildNodeText(categoryNode,
					"detailsUrl");
			Category category = _categories.get(name);
			if (category == null) {
				category = new Category(name);
				_categories.put(name, category);
			}
			category.setDetailsUrl(detailsUrl);
			category.setDescription(description);
		}
	}

	public void updateRegex(Regex regex) throws IOException {
		Element regexNode = getRootNode(regex.getDetailsUrl());
		String description = DomHelper.getChildNodeText(regexNode,
				"description");
		String expression = DomHelper.getChildNodeText(regexNode, "expression");
		String author = DomHelper.getChildNodeText(regexNode, "author");
		String timestamp = DomHelper.getChildNodeText(regexNode, "timestamp");
		String positiveVotes = DomHelper.getChildNodeText(regexNode,
				"positiveVotes");
		String negativeVotes = DomHelper.getChildNodeText(regexNode,
				"negativeVotes");
		String detailsUrl = DomHelper.getChildNodeText(regexNode, "detailsUrl");
		regex.setDescription(description);
		regex.setExpression(expression);
		regex.setAuthor(author);
		regex.setTimestamp(Long.parseLong(timestamp));
		regex.setPositiveVotes(Integer.parseInt(positiveVotes));
		regex.setNegativeVotes(Integer.parseInt(negativeVotes));
		regex.setDetailsUrl(detailsUrl);

		List<Category> categories = new ArrayList<Category>();
		List<Node> categoriesNodes = DomHelper.getChildNodesByName(regexNode,
				"categories");
		if (!categoriesNodes.isEmpty()) {
			Node categoriesNode = categoriesNodes.get(0);
			List<Node> categoryNodes = DomHelper.getChildNodesByName(
					categoriesNode, "category");
			for (Node categoryNode : categoryNodes) {
				String categoryName = DomHelper.getText(categoryNode);
				Category category = _categories.get(categoryName);
				if (!category.containsRegex(regex)) {
					category.addRegex(regex);
				}
				categories.add(category);
			}
		}
		regex.setCategories(categories);
	}

	public void updateRegexes(Category category) throws IOException {
		List<Regex> regexes = new ArrayList<Regex>();
		Node rootNode = getRootNode(category.getDetailsUrl());
		List<Node> regexNodes = DomHelper
				.getChildNodesByName(rootNode, "regex");
		for (Node regexNode : regexNodes) {

			String name = DomHelper.getChildNodeText(regexNode, "name");
			Regex regex = _regexes.get(name);
			if (regex == null) {
				regex = new Regex(name);
				_regexes.put(name, regex);
			}
			String description = DomHelper.getChildNodeText(regexNode,
					"description");
			String expression = DomHelper.getChildNodeText(regexNode,
					"expression");
			String author = DomHelper.getChildNodeText(regexNode, "author");
			String timestamp = DomHelper.getChildNodeText(regexNode,
					"timestamp");
			String positiveVotes = DomHelper.getChildNodeText(regexNode,
					"positiveVotes");
			String negativeVotes = DomHelper.getChildNodeText(regexNode,
					"negativeVotes");
			String detailsUrl = DomHelper.getChildNodeText(regexNode,
					"detailsUrl");
			regex.setDescription(description);
			regex.setExpression(expression);
			regex.setAuthor(author);
			regex.setTimestamp(Long.parseLong(timestamp));
			regex.setPositiveVotes(Integer.parseInt(positiveVotes));
			regex.setNegativeVotes(Integer.parseInt(negativeVotes));
			regex.setDetailsUrl(detailsUrl);

			if (!regex.containsCategory(category)) {
				regex.addCategory(category);
			}

			regexes.add(regex);
		}
		category.setRegexes(regexes);
	}

	private Element getRootNode(String url) throws IOException {
		GetMethod method = new GetMethod(url);
		try {
			_httpClient.executeMethod(method);
			final InputStream stream = method.getResponseBodyAsStream();
			InputSource inputSource = new InputSource() {
				@Override
				public InputStream getByteStream() {
					return stream;
				}
			};

			Document document = DomHelper.getDocumentBuilder().parse(
					inputSource);
			return (Element) document.getFirstChild();
		} catch (SAXException e) {
			throw new IllegalStateException(e);
		}
	}
}
