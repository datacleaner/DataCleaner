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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class keeps track of all profiles and their descriptors. As such the
 * ProfilerManager serves a central entry point to the profiler framework.
 */
public final class ProfilerManager {

	private static List<IProfileDescriptor> _profileDescriptors = new ArrayList<IProfileDescriptor>();
	private static Log _log = LogFactory.getLog(ProfilerManager.class);

	/**
	 * Prevent instantiation
	 */
	private ProfilerManager() {
	}

	public static IProfileDescriptor[] getProfileDescriptors() {
		return _profileDescriptors
				.toArray(new IProfileDescriptor[_profileDescriptors.size()]);
	}

	public static IProfileDescriptor getProfileDescriptorByProfileClass(
			Class<? extends IProfile> profileClass) {
		if (profileClass != null) {
			for (IProfileDescriptor profileDescriptor : _profileDescriptors) {
				if (profileClass == profileDescriptor.getProfileClass()) {
					return profileDescriptor;
				}
			}
		}
		return null;
	}

	public static IProfileDescriptor getProfileDescriptorByProfileClassName(
			String className) {
		if (className != null) {
			for (IProfileDescriptor profileDescriptor : _profileDescriptors) {
				if (className.equals(profileDescriptor.getProfileClass()
						.getName())) {
					return profileDescriptor;
				}
			}
		}
		return null;
	}

	public static void setProfileDescriptors(
			List<IProfileDescriptor> profileDescriptors) {
		if (_log.isInfoEnabled()) {
			_log.info("Setting profile descriptors: "
					+ ArrayUtils.toString(profileDescriptors));
		}
		_profileDescriptors = profileDescriptors;
	}

	public static void addProfileDescriptor(IProfileDescriptor profileDescriptor) {
		if (_log.isInfoEnabled()) {
			_log.info("Adding profile descriptor: " + profileDescriptor);
		}
		_profileDescriptors.add(profileDescriptor);
	}
}