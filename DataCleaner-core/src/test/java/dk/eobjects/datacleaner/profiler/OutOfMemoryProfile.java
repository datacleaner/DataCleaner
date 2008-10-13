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
package dk.eobjects.datacleaner.profiler;

import java.util.Map;

import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.schema.Column;

/**
 * A profile that yielding an out of memory error after 10 rows have been
 * processed
 */
public class OutOfMemoryProfile implements IProfile {

	private int _counter = 0;

	public IProfileResult getResult() {
		throw new UnsupportedOperationException(
				"Result cannot be generated and this method should not be called");
	}

	public void initialize(Column... columns) {
	}

	public void process(Row row, long distinctRowCount) {
		_counter++;
		if (_counter > 10) {
			// Imitates an out of memory error
			throw new OutOfMemoryError("Java heap space");
		}
	}

	public void setProperties(Map<String, String> properties) {
	}
}
