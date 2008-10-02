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
package dk.eobjects.datacleaner.execution;

/**
 * A progress observer is an object who recieves notifications about an ongoing
 * progress. A progress observer is initialized with the init method and
 * subsequently notifyExecutionBegin and notifyExecutionEnd is called for each
 * unit of progress.
 */
public interface IProgressObserver {

	public void init(Object[] executingObjects);

	public void notifyExecutionBegin(Object executingObject);

	public void notifyExecutionSuccess(Object executingObject);

	public void notifyExecutionFailed(Object executingObject,
			Throwable throwable);
}