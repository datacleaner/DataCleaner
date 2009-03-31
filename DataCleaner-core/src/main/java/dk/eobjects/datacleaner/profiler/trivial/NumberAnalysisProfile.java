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

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.NumberFormatter;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.apache.commons.math.stat.descriptive.SummaryStatisticsImpl;

import dk.eobjects.datacleaner.profiler.AbstractProfile;
import dk.eobjects.datacleaner.profiler.IMatrix;
import dk.eobjects.datacleaner.profiler.MatrixBuilder;
import dk.eobjects.datacleaner.profiler.MatrixValue;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.FilterItem;
import dk.eobjects.metamodel.query.OperatorType;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.util.FormatHelper;

/**
 * Number analysis profile, which provides statistical information for number
 * columns:
 * 
 * <ul>
 * <li>Highest value</li>
 * <li>Lowest value</li>
 * <li>Sum</li>
 * <li>Mean</li>
 * <li>Geometric mean</li>
 * <li>Standard deviation</li>
 * <li>Variance</li>
 * </ul>
 */
public class NumberAnalysisProfile extends AbstractProfile {

	private Map<Column, SummaryStatistics> _statistics = new HashMap<Column, SummaryStatistics>();
	private NumberFormat _numberFormat = FormatHelper.getUiNumberFormat();

	@Override
	protected void processValue(Column column, Object value, long valueCount, Row row) {
		SummaryStatistics statistics = _statistics.get(column);
		if (statistics == null) {
			statistics = new SummaryStatisticsImpl();
			_statistics.put(column, statistics);
		}
		if (value != null) {
			Double doubleValue = Double.parseDouble(value.toString());
			for (int i = 0; i < valueCount; i++) {
				statistics.addValue(doubleValue);
			}
		}
	}

	@Override
	protected List<IMatrix> getResultMatrices() {
		MatrixBuilder mb = new MatrixBuilder();
		mb.addRow("Highest value");
		mb.addRow("Lowest value");
		mb.addRow("Sum");
		mb.addRow("Mean");
		mb.addRow("Geometric mean");
		mb.addRow("Standard deviation");
		mb.addRow("Variance");

		NumberFormatter nf = new NumberFormatter(_numberFormat);
		for (int i = 0; i < _columns.length; i++) {
			try {
				SummaryStatistics s = _statistics.get(_columns[i]);
				if (s.getN() > 0) {
					double lowestValue = s.getMin();
					double highestValue = s.getMax();
					double geometricMean = s.getGeometricMean();
					double standardDeviation = s.getStandardDeviation();
					double mean = s.getMean();
					double variance = s.getVariance();
					double sum = s.getSum();

					MatrixValue[] matrixValues = mb.addColumn(_columns[i].getName(), nf.valueToString(highestValue), nf
							.valueToString(lowestValue), nf.valueToString(sum), nf.valueToString(mean), nf
							.valueToString(geometricMean), nf.valueToString(standardDeviation), nf
							.valueToString(variance));

					matrixValues[0].setDetailSource(getBaseQuery().where(
							new FilterItem(new SelectItem(_columns[i]), OperatorType.EQUALS_TO, highestValue)));

					matrixValues[1].setDetailSource(getBaseQuery().where(
							new FilterItem(new SelectItem(_columns[i]), OperatorType.EQUALS_TO, lowestValue)));
				} else {
					mb.addColumn(_columns[i].getName(), null, null, null, null, null, null, null);
				}
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}

		List<IMatrix> result = new ArrayList<IMatrix>();
		if (!mb.isEmpty()) {
			result.add(mb.getMatrix());
		}
		return result;
	}
}