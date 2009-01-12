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

import dk.eobjects.metamodel.schema.Table;

/**
 * Stub implementation of the IProgressObserver interface
 */
public abstract class AbstractProgressObserver implements IProgressObserver {

	public void init(Table[] tablesToProcess) {
	}

	public void notifyBeginning(Table tableToProcess, long numRows) {
	}

	public void notifyFailure(Table processedTable, Throwable throwable,
			long lastRow) {
	}

	public void notifyProgress(long numRowsProcessed) {
	}

	public void notifySuccess(Table processedTable, long numRowsProcessed) {
	}
}
