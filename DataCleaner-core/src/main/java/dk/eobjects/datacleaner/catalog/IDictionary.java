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

import java.io.Serializable;

/**
 * A dictionary is an object that can tell you if a word or sentence is valid or
 * not. Dictionaries are serializable so that they can be saved as part of an
 * application configuration for use at a later time.
 */
public interface IDictionary extends Serializable {

	/**
	 * @return a name/label for the dictionary. Can be null.
	 */
	public String getName();

	/**
	 * Evaluates a list of words
	 * 
	 * @param values
	 *            the words
	 * @return an array of boolean, representing whether or not the words where
	 *         validated
	 */
	public boolean[] isValid(String... values);
}