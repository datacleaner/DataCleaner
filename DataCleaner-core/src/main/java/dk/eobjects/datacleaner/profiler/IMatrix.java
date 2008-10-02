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

/**
 * A matrix is a table with numbers where all rows and all columns are named.
 * Matrices are used primarily for displaying parts of profile results, where
 * for example the columns of the matrix represent the profiled columns, the
 * rows of the matrix represent the categories of the profile result and content
 * (numbers) of the matrix represent the actual count/value of the profile
 * result.
 */
public interface IMatrix {

	public String[] getRowNames();

	public String[] getColumnNames();

	public MatrixValue[][] getValues();

	public MatrixValue getValue(int rowNumber, int columnNumber);

	public MatrixValue getValue(String rowName, String columnName);
}