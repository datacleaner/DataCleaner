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

import java.util.List;
import java.util.Map;

import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.schema.Column;

/**
 * Provides an interface for callback methods to do the actual processing of a
 * DataCleanerExecutor instance
 * 
 * @param <C extends IJobConfiguration>
 *            the job configuration object type
 * @param <R>
 *            the result object type
 * @param <P>
 *            the processor object type
 */
public interface IExecutorCallback<C extends IJobConfiguration, R, P> {

	public List<P> initProcessors(Map<C, Column[]> jobConfigurations,
			ExecutionConfiguration executionConfiguration);

	public void processRow(Row row, long count, P processor);

	public R getResult(P processor);
}
