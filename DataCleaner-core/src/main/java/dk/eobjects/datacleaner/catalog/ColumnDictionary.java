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
package dk.eobjects.datacleaner.catalog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.query.FilterItem;
import dk.eobjects.metamodel.query.GroupByItem;
import dk.eobjects.metamodel.query.OperatorType;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;

public class ColumnDictionary implements IDictionary {

	private static final long serialVersionUID = 5860761529554184426L;
	private String _name;
	private DataContext _dataContext;
	private Column _column;

	public ColumnDictionary(String name, DataContext dataContext, Column column) {
		_name = name;
		_dataContext = dataContext;
		_column = column;
	}

	public String getName() {
		return _name;
	}

	public boolean[] isValid(String... values) {
		SelectItem selectItem = new SelectItem(_column);
		boolean[] result = new boolean[values.length];
		List<FilterItem> items = new ArrayList<FilterItem>();
		for (int i = 0; i < values.length; i++) {
			String sentence = values[i];
			if (sentence != null) {
				if (sentence.indexOf('\'') != -1) {
					// If the value contains a single quote we will have to use
					// a workaround to ensure that the query can be executed
					String wildcardValue = sentence.replace('\'', '%');
					items.add(new FilterItem(selectItem, OperatorType.LIKE,
							wildcardValue));
				} else {
					items.add(new FilterItem(selectItem,
							OperatorType.EQUALS_TO, sentence));
				}
			}
		}
		Query q = new Query().select(selectItem).selectCount().from(
				_column.getTable()).where(
				new FilterItem(items.toArray(new FilterItem[items.size()])))
				.groupBy(new GroupByItem(selectItem));
		List<Object[]> queryResult = _dataContext.executeQuery(q)
				.toObjectArrays();

		for (int i = 0; i < values.length; i++) {
			String value = values[i];
			boolean found = false;
			if (value != null) {
				for (Iterator<Object[]> it = queryResult.iterator(); it
						.hasNext()
						&& !found;) {
					Object[] objects = it.next();
					if (value.equals(objects[0])) {
						found = true;
						if (((Number) objects[1]).intValue() > 0) {
							result[i] = true;
						} else {
							result[i] = false;
						}
					}
				}
			}
			if (found == false) {
				result[i] = false;
			}
		}
		return result;
	}

	@Override
	public int hashCode() {
		return _name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}
}
