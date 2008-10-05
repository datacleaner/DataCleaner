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

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import dk.eobjects.datacleaner.profiler.AbstractProfile;
import dk.eobjects.datacleaner.profiler.IProfile;
import dk.eobjects.datacleaner.profiler.IProfileResult;
import dk.eobjects.datacleaner.profiler.ProfileConfiguration;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.schema.Column;

public class ProfileRunner extends
		AbstractRunner<ProfileConfiguration, IProfileResult, IProfile> {

	private boolean _detailsEnabled = true;

	@Override
	protected IProfile[] initConfigurations(
			Map<ProfileConfiguration, Column[]> configurations) {
		ArrayList<IProfile> result = new ArrayList<IProfile>();
		for (Entry<ProfileConfiguration, Column[]> entry : configurations
				.entrySet()) {
			ProfileConfiguration configuration = entry.getKey();
			Column[] columns = entry.getValue();
			IProfile profile = initProfile(configuration, columns);
			result.add(profile);
		}
		return result.toArray(new IProfile[result.size()]);
	}

	private IProfile initProfile(ProfileConfiguration configuration,
			Column[] columns) {
		Class<? extends IProfile> profileClass = configuration
				.getProfileDescriptor().getProfileClass();
		try {
			IProfile profile = profileClass.newInstance();
			profile.setProperties(configuration.getProfileProperties());
			profile.initialize(columns);
			if (!_detailsEnabled && profile instanceof AbstractProfile) {
				AbstractProfile ap = (AbstractProfile) profile;
				ap.setDetailsEnabled(_detailsEnabled);
			}
			return profile;
		} catch (InstantiationException e) {
			_log.fatal(e);
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			_log.fatal(e);
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void processRow(Row row, long count, IProfile processor) {
		processor.process(row, count);
	}

	@Override
	protected IProfileResult getResult(IProfile processor) {
		return processor.getResult();
	}

	public void setDetailsEnabled(boolean detailsEnabled) {
		_detailsEnabled  = detailsEnabled;
	}
}