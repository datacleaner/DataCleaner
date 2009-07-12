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
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.datacleaner.execution.DataCleanerExecutor;
import dk.eobjects.datacleaner.execution.ExecutionConfiguration;
import dk.eobjects.datacleaner.execution.IExecutorCallback;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.schema.Column;

public class ProfilerExecutorCallback implements
		IExecutorCallback<ProfilerJobConfiguration, IProfileResult, IProfile> {

	protected Log _log = LogFactory.getLog(getClass());

	public static DataCleanerExecutor<ProfilerJobConfiguration, IProfileResult, IProfile> createExecutor() {
		return new DataCleanerExecutor<ProfilerJobConfiguration, IProfileResult, IProfile>(
				new ProfilerExecutorCallback());
	}

	public List<IProfile> initProcessors(
			Map<ProfilerJobConfiguration, Column[]> jobConfigurations,
			ExecutionConfiguration executionConfiguration) {
		ArrayList<IProfile> result = new ArrayList<IProfile>();
		for (Entry<ProfilerJobConfiguration, Column[]> entry : jobConfigurations
				.entrySet()) {
			ProfilerJobConfiguration configuration = entry.getKey();
			Column[] columns = entry.getValue();
			IProfile profile = initProfile(configuration, columns,
					executionConfiguration);
			result.add(profile);
		}
		return result;
	}

	private IProfile initProfile(ProfilerJobConfiguration configuration,
			Column[] columns, ExecutionConfiguration executionConfiguration) {
		Class<? extends IProfile> profileClass = configuration
				.getProfileDescriptor().getProfileClass();
		try {
			IProfile profile = profileClass.newInstance();
			profile.setProperties(configuration.getProfileProperties());
			profile.initialize(columns);
			if (!executionConfiguration.isDrillToDetailEnabled()
					&& profile instanceof AbstractProfile) {
				AbstractProfile ap = (AbstractProfile) profile;
				ap.setDetailsEnabled(executionConfiguration
						.isDrillToDetailEnabled());
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

	public void processRow(Row row, long count, IProfile processor) {
		processor.process(row, count);
	}

	public IProfileResult getResult(IProfile processor) {
		IProfileResult result = processor.getResult();
		processor.close();
		return result;
	}
}