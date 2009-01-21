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
package dk.eobjects.datacleaner.export;

import java.io.PrintWriter;

import dk.eobjects.datacleaner.profiler.IProfileResult;
import dk.eobjects.datacleaner.validator.IValidationRuleResult;
import dk.eobjects.metamodel.schema.Table;

public interface IResultExporter {

	/**
	 * Indicates whether this result exporter can collect multiple results in
	 * the same writer or if results should be exported to separate writers.
	 * 
	 * @return true if multiple results can be written to the same writer, false
	 *         if not
	 */
	public boolean isCollectiveResultsCapable();

	public void writeProfileResultHeader(PrintWriter writer);

	public void writeValidationRuleResultHeader(PrintWriter writer);

	public void writeProfileResult(Table table, IProfileResult result, PrintWriter writer);

	public void writeValidationRuleResult(Table table, IValidationRuleResult result, PrintWriter writer);

	public void writeProfileResultFooter(PrintWriter writer);

	public void writeValidationRuleResultFooter(PrintWriter writer);
}