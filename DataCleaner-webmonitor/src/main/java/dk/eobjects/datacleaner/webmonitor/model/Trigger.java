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
package dk.eobjects.datacleaner.webmonitor.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.quartz.CronTrigger;
import org.quartz.SimpleTrigger;

public class Trigger implements Serializable {

	private static final long serialVersionUID = -9217750960418980438L;
	private long _id;
	private String _name;
	private String _cronExpression;
	private Long _repeatInterval;
	private Set<ProfilerJob> _profilerJobs = new HashSet<ProfilerJob>();

	public long getId() {
		return _id;
	}

	public void setId(long id) {
		_id = id;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	public String getCronExpression() {
		return _cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		_cronExpression = cronExpression;
	}

	public Long getRepeatInterval() {
		return _repeatInterval;
	}

	public void setRepeatInterval(Long repeatInterval) {
		_repeatInterval = repeatInterval;
	}

	public Set<ProfilerJob> getProfilerJobs() {
		return _profilerJobs;
	}

	public void setProfilerJobs(Set<ProfilerJob> profilerJobs) {
		_profilerJobs = profilerJobs;
	}

	public org.quartz.Trigger toQuartzTrigger() throws Exception {
		org.quartz.Trigger result;
		if (_cronExpression != null) {
			result = new CronTrigger(_name, null, _cronExpression);
		} else {
			SimpleTrigger trigger = new SimpleTrigger(_name, null);
			trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
			trigger.setRepeatInterval(_repeatInterval);
			trigger.setStartTime(new Date());
			result = trigger;
		}
		return result;
	}

	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder(this,
				ToStringStyle.SHORT_PREFIX_STYLE).append("id", _id).append(
				"name", _name).append("cronExpression", _cronExpression)
				.append("repeatInterval", _repeatInterval);
		return tsb.toString();
	}
}