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

public class ProfilerResultCell implements Serializable {

	private static final long serialVersionUID = -1593846572709555088L;
	private long _id;
	private ProfilerResult _profilerResult;
	private int _columnIndex;
	private int _rowIndex;
	private String _value;

	public long getId() {
		return _id;
	}

	public void setId(long id) {
		_id = id;
	}

	public ProfilerResult getProfilerResult() {
		return _profilerResult;
	}

	public void setProfilerResult(ProfilerResult profilerResult) {
		_profilerResult = profilerResult;
	}

	public int getColumnIndex() {
		return _columnIndex;
	}

	public void setColumnIndex(int columnIndex) {
		_columnIndex = columnIndex;
	}

	public int getRowIndex() {
		return _rowIndex;
	}

	public void setRowIndex(int rowIndex) {
		_rowIndex = rowIndex;
	}

	public String getValue() {
		return _value;
	}

	public void setValue(String value) {
		_value = value;
	}
}