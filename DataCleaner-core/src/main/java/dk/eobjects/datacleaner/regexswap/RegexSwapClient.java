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
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.xml.sax.InputSource;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.DataContextFactory;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.schema.Table;

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
		DataContext dc = getDataContext(CATEGORIES_URL);
		Table table = dc.getDefaultSchema().getTableByName("category");
		DataSet dataSet = dc.executeQuery(new Query()
				.select(table.getColumns()).from(table));
		while (dataSet.next()) {
			Row row = dataSet.getRow();
			String name = (String) row.getValue(table.getColumnByName("name"));
			String description = (String) row.getValue(table
					.getColumnByName("description"));
			String detailsUrl = (String) row.getValue(table
					.getColumnByName("detailsUrl"));
			Category category = _categories.get(name);
			if (category == null) {
				category = new Category(name);
				_categories.put(name, category);
			}
			category.setDetailsUrl(detailsUrl);
			category.setDescription(description);
		}
		dataSet.close();
	}

	public void updateRegex(Regex regex) throws IOException {
		DataContext dc = getDataContext(regex.getDetailsUrl());
		Table table = dc.getDefaultSchema().getTableByName("regex");
		if (table != null) {
			DataSet dataSet = dc.executeQuery(new Query().select(
					table.getColumns()).from(table));
			dataSet.next();
			Row row = dataSet.getRow();
			String description = (String) row.getValue(table
					.getColumnByName("description"));
			String expression = (String) row.getValue(table
					.getColumnByName("expression"));
			String author = (String) row.getValue(table
					.getColumnByName("author"));
			String timestamp = (String) row.getValue(table
					.getColumnByName("timestamp"));
			String positiveVotes = (String) row.getValue(table
					.getColumnByName("positiveVotes"));
			String negativeVotes = (String) row.getValue(table
					.getColumnByName("negativeVotes"));
			String detailsUrl = (String) row.getValue(table
					.getColumnByName("detailsUrl"));
			regex.setDescription(description);
			regex.setExpression(expression);
			regex.setAuthor(author);
			regex.setTimestamp(Long.parseLong(timestamp));
			regex.setPositiveVotes(Integer.parseInt(positiveVotes));
			regex.setNegativeVotes(Integer.parseInt(negativeVotes));
			regex.setDetailsUrl(detailsUrl);
			dataSet.close();

			List<Category> categories = new ArrayList<Category>();
			table = dc.getDefaultSchema().getTableByName("category");
			if (table != null) {
				dataSet = dc.executeQuery(new Query()
						.select(table.getColumns()).from(table));
				while (dataSet.next()) {
					row = dataSet.getRow();
					String categoryName = (String) row.getValue(table
							.getColumnByName("category"));
					Category category = _categories.get(categoryName);
					if (!category.containsRegex(regex)) {
						category.addRegex(regex);
					}
					categories.add(category);
				}
				dataSet.close();
			}
			regex.setCategories(categories);
		}
	}

	public void updateRegexes(Category category) throws IOException {
		List<Regex> regexes = new ArrayList<Regex>();
		DataContext dc = getDataContext(category.getDetailsUrl());
		Table table = dc.getDefaultSchema().getTableByName("regex");
		if (table != null) {
			DataSet dataSet = dc.executeQuery(new Query().select(
					table.getColumns()).from(table));
			while (dataSet.next()) {
				Row row = dataSet.getRow();
				String name = (String) row.getValue(table
						.getColumnByName("name"));
				Regex regex = _regexes.get(name);
				if (regex == null) {
					regex = new Regex(name);
					_regexes.put(name, regex);
				}
				String description = (String) row.getValue(table
						.getColumnByName("description"));
				String expression = (String) row.getValue(table
						.getColumnByName("expression"));
				String author = (String) row.getValue(table
						.getColumnByName("author"));
				String timestamp = (String) row.getValue(table
						.getColumnByName("timestamp"));
				String positiveVotes = (String) row.getValue(table
						.getColumnByName("positiveVotes"));
				String negativeVotes = (String) row.getValue(table
						.getColumnByName("negativeVotes"));
				String detailsUrl = (String) row.getValue(table
						.getColumnByName("detailsUrl"));
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
			dataSet.close();
		}
		category.setRegexes(regexes);
	}

	private DataContext getDataContext(String url) throws IOException {
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
			DataContext dataContext = DataContextFactory.createXmlDataContext(
					inputSource, "public", true, false);
			return dataContext;
		} catch (HttpException e) {
			throw new IllegalStateException(e);
		}
	}
}
