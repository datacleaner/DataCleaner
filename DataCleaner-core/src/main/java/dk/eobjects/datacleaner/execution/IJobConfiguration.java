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

import java.io.Serializable;

import dk.eobjects.datacleaner.profiler.ProfilerExecutorCallback;
import dk.eobjects.datacleaner.validator.ValidatorExecutorCallback;
import dk.eobjects.metamodel.schema.Column;

/**
 * Represents a configured part of a composite job, to be run by an IRunner
 * instance.
 * 
 * @see ProfilerExecutorCallback
 * @see ValidatorExecutorCallback
 */
public interface IJobConfiguration extends Serializable {

	public Column[] getColumns();
}