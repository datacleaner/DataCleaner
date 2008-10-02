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
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

public class ProfileHelperTest extends TestCase {

	public void testGetProfileResultsByProfileDisplayName() throws Exception {
		List<IProfileResult> profileResults = new ArrayList<IProfileResult>();
		profileResults.add(new ProfileResult(
				ProfileManagerTest.DESCRIPTOR_PATTERN_FINDER));
		profileResults.add(new ProfileResult(
				ProfileManagerTest.DESCRIPTOR_PATTERN_FINDER));
		profileResults.add(new ProfileResult(
				ProfileManagerTest.DESCRIPTOR_STANDARD_MEASURES));

		Map<IProfileDescriptor, List<IProfileResult>> result = ProfilerHelper
				.getProfileResultsByProfileDescriptor(profileResults);
		Set<IProfileDescriptor> keySet = result.keySet();
		assertEquals(2, keySet.size());

		List<IProfileResult> patternFinderList = result
				.get(ProfileManagerTest.DESCRIPTOR_PATTERN_FINDER);
		assertEquals(2, patternFinderList.size());
		List<IProfileResult> nullCounterList = result
				.get(ProfileManagerTest.DESCRIPTOR_STANDARD_MEASURES);
		assertEquals(1, nullCounterList.size());
	}
}