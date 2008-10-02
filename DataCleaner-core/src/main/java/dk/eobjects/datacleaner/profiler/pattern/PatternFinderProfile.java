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
package dk.eobjects.datacleaner.profiler.pattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import dk.eobjects.datacleaner.profiler.AbstractProfile;
import dk.eobjects.datacleaner.profiler.IMatrix;
import dk.eobjects.datacleaner.profiler.MatrixBuilder;
import dk.eobjects.datacleaner.profiler.MatrixValue;
import dk.eobjects.metamodel.data.IRowFilter;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.schema.Column;

public class PatternFinderProfile extends AbstractProfile {

	private Map<Column, PatternRecognizer> _patternRecognizers = new HashMap<Column, PatternRecognizer>();

	@Override
	protected void processValue(Column column, Object value, long valueCount,
			Row row) {
		PatternRecognizer patternRecognizer = _patternRecognizers.get(column);
		if (patternRecognizer == null) {
			patternRecognizer = new PatternRecognizer();
			_patternRecognizers.put(column, patternRecognizer);
		}
		if (value != null) {
			patternRecognizer.addInstance(value.toString(), valueCount);
		}
	}

	@Override
	protected List<IMatrix> getResultMatrices() {
		List<IMatrix> result = new ArrayList<IMatrix>();

		for (final Column column : _columns) {
			MatrixBuilder mb = new MatrixBuilder();
			mb.addColumn(column.getName());

			final PatternRecognizer patternRecognizer = _patternRecognizers
					.get(column);
			Map<String, Long> patterns = patternRecognizer.identifyPatterns();
			for (Entry<String, Long> entry : patterns.entrySet()) {
				Long patternCount = entry.getValue();
				final String patternName = entry.getKey();
				MatrixValue[] matrixValues = mb.addRow(patternName,
						patternCount);
				if (isDetailsEnabled()) {
					MatrixValue mv = matrixValues[0];
					mv.setDetailSource(getBaseQuery(column));
					mv.addDetailRowFilter(new IRowFilter() {

						public boolean accept(Row row) {
							Object value = row.getValue(column);
							if (value != null) {
								return patternRecognizer.patternEquals(
										patternName, value.toString());
							}
							return false;
						}

					});
				}
			}
			if (!mb.isEmpty()) {
				mb.sortColumn(0, MatrixBuilder.DESCENDING);
				result.add(mb.getMatrix());
			}
		}
		return result;
	}
}