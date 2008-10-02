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

import java.io.File;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;

import dk.eobjects.datacleaner.webmonitor.WebmonitorHelper;

public class DataCleanerDataSource implements FactoryBean {

	private static final Log _log = LogFactory
			.getLog(DataCleanerDataSource.class);
	private String _relativePath;
	private BasicDataSource _dataSource;
	private WebmonitorHelper _webmonitorHelper;

	public void setDatabaseFolder(String path) {
		_relativePath = path;
	}

	public void setWebmonitorHelper(WebmonitorHelper webmonitorHelper) {
		_webmonitorHelper = webmonitorHelper;
	}

	public DataSource getObject() {
		if (_dataSource == null) {
			if (_relativePath == null) {
				throw new IllegalStateException("Non-valid database folder: "
						+ _relativePath);
			}
			String absolutePath = _webmonitorHelper.getFile(_relativePath)
					.getAbsolutePath();
			_dataSource = new BasicDataSource();
			_dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
			_dataSource.setUsername("sa");
			_dataSource.setPassword("");
			String url = "jdbc:hsqldb:file:" + absolutePath + File.separator
					+ "webmonitor_database";
			_log.info("Using database URL: " + url);
			_dataSource.setUrl(url);
		}
		return _dataSource;
	}

	public void destroy() throws SQLException {
		_log.info("Destroying DataSource");
		_dataSource.close();
		_dataSource = null;
	}

	public Class<?> getObjectType() {
		return DataSource.class;
	}

	public boolean isSingleton() {
		return true;
	}
}