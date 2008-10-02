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
package dk.eobjects.datacleaner.webmonitor;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import dk.eobjects.datacleaner.profiler.IMatrix;
import dk.eobjects.datacleaner.profiler.IProfileDescriptor;
import dk.eobjects.datacleaner.profiler.IProfileResult;
import dk.eobjects.datacleaner.webmonitor.model.ProfilerJob;
import dk.eobjects.datacleaner.webmonitor.model.ProfilerResult;
import dk.eobjects.datacleaner.webmonitor.model.ProfilerResultCell;
import dk.eobjects.metamodel.schema.Table;

public class PersistenceHelper extends HibernateTemplate {
	
	public void saveResults(ProfilerJob job, Table table,
			List<IProfileResult> results) {
		for (IProfileResult profileResult : results) {
			IProfileDescriptor descriptor = profileResult.getDescriptor();
			IMatrix[] matrices = profileResult.getMatrices();
			for (IMatrix matrix : matrices) {
				final ProfilerResult result = new ProfilerResult();
				result.setTableName(table.getName());
				result.setDate(new Date());
				String[] columnNames = matrix.getColumnNames();
				result.setColumnNames(columnNames);
				String[] rowNames = matrix.getRowNames();
				result.setRowNames(rowNames);
				result.setProfileName(descriptor.getDisplayName());
				result.setProfilerJob(job);
				Set<ProfilerResultCell> cells = new HashSet<ProfilerResultCell>();

				for (int i = 0; i < rowNames.length; i++) {
					for (int j = 0; j < columnNames.length; j++) {
						Object value = matrix.getValue(i, j).getValue();
						if (value != null) {
							ProfilerResultCell cell = new ProfilerResultCell();
							cell.setRowIndex(i);
							cell.setColumnIndex(j);
							cell.setProfilerResult(result);
							cell.setValue(value.toString());
							cells.add(cell);
						}
					}
				}
				result.setCells(cells);

				execute(new HibernateCallback() {
					public Object doInHibernate(Session session)
							throws HibernateException, SQLException {
						session.save(result);
						return null;
					}
				});
			}
		}
	}
}