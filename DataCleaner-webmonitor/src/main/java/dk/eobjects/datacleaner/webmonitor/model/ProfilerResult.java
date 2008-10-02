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

public class ProfilerResult implements Serializable {

	private static final long serialVersionUID = -3556948260486002658L;

	private long _id;
	private ProfilerJob _profilerJob;
	private String _tableName;
	private String _profileName;
	private Date _date;
	private Set<ProfilerResultCell> _cells = new HashSet<ProfilerResultCell>();
	private String[] _columnNames = new String[0];
	private String[] _rowNames = new String[0];

	public long getId() {
		return _id;
	}

	public void setId(long id) {
		_id = id;
	}

	public void setTableName(String tableName) {
		_tableName = tableName;
	}

	public String getTableName() {
		return _tableName;
	}

	public ProfilerJob getProfilerJob() {
		return _profilerJob;
	}

	public void setProfilerJob(ProfilerJob profilerJob) {
		_profilerJob = profilerJob;
	}

	public String getProfileName() {
		return _profileName;
	}

	public void setProfileName(String profileName) {
		_profileName = profileName;
	}

	public Date getDate() {
		return _date;
	}

	public void setDate(Date date) {
		_date = date;
	}

	public Set<ProfilerResultCell> getCells() {
		return _cells;
	}

	public void setCells(Set<ProfilerResultCell> cells) {
		_cells = cells;
	}

	public String[] getColumnNames() {
		return _columnNames;
	}

	public void setColumnNames(String[] columnNames) {
		_columnNames = columnNames;
	}

	public String[] getRowNames() {
		return _rowNames;
	}

	public void setRowNames(String[] rowNames) {
		_rowNames = rowNames;
	}
}