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

import java.util.ArrayList;
import java.util.List;

public class Regex {

	private String _name;
	private String _description;
	private String _expression;
	private String _author;
	private long _timestamp;
	private int _positiveVotes;
	private int _negativeVotes;
	private String _detailsUrl;
	private List<Category> _categories = new ArrayList<Category>();
	
	public Regex(String name) {
		_name = name;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	public String getDescription() {
		return _description;
	}

	public void setDescription(String description) {
		_description = description;
	}

	public String getExpression() {
		return _expression;
	}

	public void setExpression(String expression) {
		_expression = expression;
	}

	public String getAuthor() {
		return _author;
	}

	public void setAuthor(String author) {
		_author = author;
	}

	public long getTimestamp() {
		return _timestamp;
	}

	public void setTimestamp(long timestamp) {
		_timestamp = timestamp;
	}

	public int getPositiveVotes() {
		return _positiveVotes;
	}

	public void setPositiveVotes(int positiveVotes) {
		_positiveVotes = positiveVotes;
	}

	public int getNegativeVotes() {
		return _negativeVotes;
	}

	public void setNegativeVotes(int negativeVotes) {
		_negativeVotes = negativeVotes;
	}
	
	public String getDetailsUrl() {
		return _detailsUrl;
	}
	
	public void setDetailsUrl(String detailsUrl) {
		_detailsUrl = detailsUrl;
	}
	
	public List<Category> getCategories() {
		return _categories;
	}
	
	public void setCategories(List<Category> categories) {
		_categories = categories;
	}
	
	public boolean containsCategory(Category category) {
		return _categories.contains(category);
	}
	
	public void addCategory(Category category) {
		_categories.add(category);
	}
}
