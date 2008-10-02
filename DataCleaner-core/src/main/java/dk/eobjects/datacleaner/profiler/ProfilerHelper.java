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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfilerHelper {

	public static Number[][] toSingleColumnMatrixValues(List<Number> numbers) {
		Number[][] result = new Number[numbers.size()][1];
		for (int i = 0; i < numbers.size(); i++) {
			result[i][0] = numbers.get(i);
		}
		return result;
	}

	public static Number[][] toSingleColumnMatrixValues(Number[] numbers) {
		Number[][] result = new Number[numbers.length][1];
		for (int i = 0; i < numbers.length; i++) {
			result[i][0] = numbers[i];
		}
		return result;
	}

	/**
	 * Organizes profile results according to their profile descriptors
	 */
	public static Map<IProfileDescriptor, List<IProfileResult>> getProfileResultsByProfileDescriptor(
			List<IProfileResult> results) {
		Map<IProfileDescriptor, List<IProfileResult>> result = new HashMap<IProfileDescriptor, List<IProfileResult>>();

		for (int i = 0; i < results.size(); i++) {
			IProfileResult profileResult = results.get(i);
			IProfileDescriptor profileDescriptor = profileResult
					.getDescriptor();
			List<IProfileResult> profileResultsByProfileDescriptor = result
					.get(profileDescriptor);
			if (profileResultsByProfileDescriptor == null) {
				profileResultsByProfileDescriptor = new ArrayList<IProfileResult>();
				result
						.put(profileDescriptor,
								profileResultsByProfileDescriptor);
			}
			profileResultsByProfileDescriptor.add(profileResult);
		}

		return result;
	}
}