/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.regexswap;

import java.io.Serializable;
import java.util.List;

import org.eobjects.metamodel.util.BaseObject;

public final class Regex extends BaseObject implements Serializable {

	private static final long serialVersionUID = 1L;
	private final String _name;
	private final String _description;
	private final String _expression;
	private final String _author;
	private final long _timestamp;
	private final int _positiveVotes;
	private final int _negativeVotes;
	private final String _detailsUrl;
	private final List<Category> _categories;

	@Override
	protected void decorateIdentity(List<Object> identifiers) {
		identifiers.add(_name);
		identifiers.add(_description);
		identifiers.add(_expression);
		identifiers.add(_author);
		identifiers.add(_timestamp);
		identifiers.add(_positiveVotes);
		identifiers.add(_negativeVotes);
		identifiers.add(_detailsUrl);
		identifiers.add(_categories);
	}

	public Regex(String name, String description, String expression, String author, long timestamp, int positiveVotes,
			int negativeVotes, String detailsUrl, List<Category> categories) {
		_name = name;
		_description = description;
		_expression = expression;
		_author = author;
		_timestamp = timestamp;
		_positiveVotes = positiveVotes;
		_negativeVotes = negativeVotes;
		_detailsUrl = detailsUrl;
		_categories = categories;
	}

	public String getName() {
		return _name;
	}

	public String getDescription() {
		return _description;
	}

	public String getExpression() {
		return _expression;
	}

	public String getAuthor() {
		return _author;
	}

	public long getTimestamp() {
		return _timestamp;
	}

	public int getPositiveVotes() {
		return _positiveVotes;
	}

	public int getNegativeVotes() {
		return _negativeVotes;
	}

	public String getDetailsUrl() {
		return _detailsUrl;
	}

	public List<Category> getCategories() {
		return _categories;
	}

	public boolean containsCategory(Category category) {
		return _categories.contains(category);
	}

	public String createWebsiteUrl() {
		String url = "http://datacleaner.org/regex/" + getName().replaceAll(" ", "%20");
		return url;
	}

	@Override
	public String toString() {
		return "Regex[name=" + _name + ",expression=" + _expression + "]";
	}
}
