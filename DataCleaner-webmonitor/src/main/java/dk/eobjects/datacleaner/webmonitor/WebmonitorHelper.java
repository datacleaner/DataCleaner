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

import java.io.File;

import javax.servlet.ServletContext;

import org.springframework.web.context.ServletContextAware;

public class WebmonitorHelper implements ServletContextAware {

	private ServletContext _servletContext;

	public void setServletContext(ServletContext servletContext) {
		_servletContext = servletContext;
	}

	public File getFile(String path) {
		File file = new File(path);
		if (!file.exists()) {
			file = new File(_servletContext.getRealPath(path));
		}
		return file;
	}
}