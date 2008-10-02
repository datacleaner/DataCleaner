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
 * The profile result is the a profile. The result contains of one or more
 * matrices containing numbers with named rows and named columns.
 */
public interface IProfileResult {

	/**
	 * @return the descriptor of the profile that has been executed
	 */
	public IProfileDescriptor getDescriptor();

	/**
	 * @return an array of matrices representing the measures that have been
	 *         computed during the profiling.
	 */
	public IMatrix[] getMatrices();

	/**
	 * @return the error (if any) that occurred during execution
	 */
	public Exception getError();
}