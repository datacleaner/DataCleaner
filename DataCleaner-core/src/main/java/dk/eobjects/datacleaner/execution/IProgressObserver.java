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
 * A progress observer is an object who recieves notifications about an ongoing
 * progress. A progress observer is initialized with the init method and
 * subsequently notified using the notifyBegin, notifyProgress and
 * notifySuccess/notifyFailure methods
 */
public interface IProgressObserver {

	/**
	 * @param tablesToProcess
	 *            the tables that are going to be processed
	 */
	public void init(Table[] tablesToProcess);

	/**
	 * @param tableToProcess
	 *            the table for which process is beginning
	 * @param numRows
	 *            the number of rows from the table that is going to be
	 *            processed (before processing)
	 */
	public void notifyBeginning(Table tableToProcess, long numRows);

	/**
	 * @param numRowsProcessed
	 *            how many rows that are currently processed
	 */
	public void notifyProgress(long numRowsProcessed);

	/**
	 * @param processedTable
	 *            the table for which processing was a success
	 * @param numRowsProcessed
	 *            the total number of processed rows (this CAN differ from the
	 *            numRows variable in notifyBeginning, if the table content have
	 *            changed while processing)
	 */
	public void notifySuccess(Table processedTable, long numRowsProcessed);

	/**
	 * @param processedTable
	 *            the table for which processing failed
	 * @param lastRow
	 *            the last processed row number or -1 if processing never began
	 */
	public void notifyFailure(Table processedTable, Throwable throwable,
			long lastRow);
}