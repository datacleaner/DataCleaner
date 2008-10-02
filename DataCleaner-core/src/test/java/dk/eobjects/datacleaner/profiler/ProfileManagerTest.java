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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import dk.eobjects.datacleaner.profiler.pattern.PatternFinderProfile;
import dk.eobjects.datacleaner.profiler.trivial.ValueDistributionProfile;
import dk.eobjects.datacleaner.profiler.trivial.NumberAnalysisProfile;
import dk.eobjects.datacleaner.profiler.trivial.StandardMeasuresProfile;
import dk.eobjects.datacleaner.profiler.trivial.StringAnalysisProfile;
import dk.eobjects.datacleaner.profiler.trivial.TimeAnalysisProfile;
import dk.eobjects.datacleaner.util.ReflectionHelper;

public class ProfileManagerTest extends TestCase {

	public static final BasicProfileDescriptor DESCRIPTOR_STANDARD_MEASURES = new BasicProfileDescriptor(
			"Standard measures", StandardMeasuresProfile.class);
	public static final BasicProfileDescriptor DESCRIPTOR_PATTERN_FINDER = new BasicProfileDescriptor(
			"Pattern finder", PatternFinderProfile.class);
	public static final BasicProfileDescriptor DESCRIPTOR_STRING_ANALYSIS = new BasicProfileDescriptor(
			"String analysis", StringAnalysisProfile.class);
	public static final BasicProfileDescriptor DESCRIPTOR_NUMBER_ANALYSIS = new BasicProfileDescriptor(
			"Number analysis", NumberAnalysisProfile.class);
	public static final BasicProfileDescriptor DESCRIPTOR_TIME_ANALYSIS = new BasicProfileDescriptor(
			"Time analysis", TimeAnalysisProfile.class);
	public static final BasicProfileDescriptor DESCRIPTOR_FREQUENCY_TABLE = new BasicProfileDescriptor(
			"Frequency table", ValueDistributionProfile.class);

	public void testGetDescriptorByProfileClass() throws Exception {
		initProfileManager();
		IProfileDescriptor profileDescriptor = ProfilerManager
				.getProfileDescriptorByProfileClass(PatternFinderProfile.class);
		assertSame(DESCRIPTOR_PATTERN_FINDER, profileDescriptor);

		profileDescriptor = ProfilerManager
				.getProfileDescriptorByProfileClass(StandardMeasuresProfile.class);
		assertSame(DESCRIPTOR_STANDARD_MEASURES, profileDescriptor);

		profileDescriptor = ProfilerManager
				.getProfileDescriptorByProfileClass(null);
		assertNull(profileDescriptor);
	}

	public static void initProfileManager() {
		List<IProfileDescriptor> profileDescriptors = new ArrayList<IProfileDescriptor>();
		Field[] constants = ReflectionHelper
				.getConstants(ProfileManagerTest.class);
		for (int i = 0; i < constants.length; i++) {
			Field constant = constants[i];
			if (constant.getName().startsWith("DESCRIPTOR_")) {
				try {
					profileDescriptors.add((IProfileDescriptor) constant
							.get(null));
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}
		ProfilerManager.setProfileDescriptors(profileDescriptors);
	}
}