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
 * A profile is an abstract mechanism/engine for calculating a collection of
 * (preferebly related) measures.
 * 
 * The profile lifecycle is: <li>set the properties of the profile with
 * <code>setProperties</code></li> <li>initialize the profile with
 * <code>initialize</code></li> <li>run the <code>process</code> for all rows of
 * data</li> <li>end the profiling and recieve the result by calling the
 * <code>getResult</code> method</li>
 */
public interface IProfile {

	/**
	 * Sets configuration properties for the profile
	 * 
	 * @param properties
	 */
	public void setProperties(Map<String, String> properties);

	/**
	 * Initializes the profile
	 * 
	 * @param columns
	 *            the columns to be profiled
	 */
	public void initialize(Column... columns);

	/**
	 * Processes a row of data
	 * 
	 * @param row
	 *            the row to be processed
	 * @param distinctRowCount
	 *            the distinct count of the values in the row
	 */
	public void process(Row row, long distinctRowCount);

	/**
	 * @return the result of the profile, when there are no more rows to process
	 */
	public IProfileResult getResult();

	/**
	 * Closes any system resources associated to this profile (optional).
	 */
	public void close();
}