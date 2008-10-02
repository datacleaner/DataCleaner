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
package dk.eobjects.datacleaner.profiler.trivial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import dk.eobjects.datacleaner.profiler.AbstractProfile;
import dk.eobjects.datacleaner.profiler.IMatrix;
import dk.eobjects.datacleaner.profiler.MatrixBuilder;
import dk.eobjects.datacleaner.profiler.MatrixValue;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.FilterItem;
import dk.eobjects.metamodel.query.OperatorType;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;

/**
 * Searches the values of a column for repeated values. If the number of
 * repeated values surpass the "significance rate", then the value will appear
 * in the profile result.
 * 
 * @deprecated Use ValueDistributionProfile instead, this class will be deleted as
 *             of DataCleaner 2.0
 *             
 * @see ValueDistributionProfile
 */
@Deprecated
public class RepeatedValuesProfile extends AbstractProfile {

	public static final String PROPERTY_SIGNIFICANCE_RATE = "Significance rate (%)";
	private Map<Column, Map<String, Long>> _repeatedValues = new HashMap<Column, Map<String, Long>>();
	private int _significanceRate = 5;

	@Override
	public void setProperties(Map<String, String> properties) {
		super.setProperties(properties);
		String significanceRateString = _properties
				.get(PROPERTY_SIGNIFICANCE_RATE);
		if (significanceRateString != null) {
			try {
				_significanceRate = Integer.parseInt(significanceRateString);
			} catch (NumberFormatException e) {
				_log.error(e);
			}
		}
	}

	@Override
	protected void processValue(Column column, Object value, long valueCount,
			Row row) {
		String repeatedValue = null;
		if (value != null) {
			repeatedValue = value.toString();
		}

		Map<String, Long> valueMap = _repeatedValues.get(column);
		if (valueMap == null) {
			valueMap = new HashMap<String, Long>();
			_repeatedValues.put(column, valueMap);
		}
		Long repeatCount = valueMap.get(repeatedValue);
		if (repeatCount == null) {
			repeatCount = 0l;
		}
		repeatCount += valueCount;
		if (value != null) {
			valueMap.put(repeatedValue, repeatCount);
		} else {
			valueMap.put(null, repeatCount);
		}
	}

	@Override
	protected List<IMatrix> getResultMatrices() {
		ArrayList<IMatrix> result = new ArrayList<IMatrix>();
		for (int i = 0; i < _columns.length; i++) {
			Column column = _columns[i];
			Map<String, Long> valueMap = _repeatedValues.get(column);
			MatrixBuilder mb = new MatrixBuilder();
			mb.addColumn(column.getName() + " count");
			mb.addColumn(column.getName() + " %");

			for (Entry<String, Long> entry : valueMap.entrySet()) {
				Long repeatCount = entry.getValue();
				String value = entry.getKey();
				int repeatPercentage = (int) (repeatCount * 100 / _totalCount);
				if (repeatPercentage >= _significanceRate) {
					MatrixValue[] matrixValues = mb.addRow(value, repeatCount,
							repeatPercentage + "%");
					matrixValues[0].setDetailSource(new Query().from(
							column.getTable()).select(_columns).where(
							new FilterItem(new SelectItem(column),
									OperatorType.EQUALS_TO, value)));
				}
			}

			if (!mb.isEmpty()) {
				mb.sortColumn(0, MatrixBuilder.DESCENDING);
				result.add(mb.getMatrix());
			}
		}

		return result;
	}

	public void setSignificanceRate(int i) {
		_significanceRate = i;
	}
}