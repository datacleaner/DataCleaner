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
package dk.eobjects.datacleaner.webmonitor.controller;

import java.io.InputStream;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.AbstractView;

/**
 * Controller for feeding the schema for validation
 */
@Controller
public class SchemaController {

	@RequestMapping(value = { "/rest/datacleaner.xsd", "/soap/datacleaner.xsd" })
	public View getSchema() {
		return new AbstractView() {

			@SuppressWarnings("unchecked")
			@Override
			protected void renderMergedOutputModel(Map model,
					HttpServletRequest req, HttpServletResponse res)
					throws Exception {
				res.setContentType("application/xml");
				ClassPathResource resource = new ClassPathResource(
						"schemas/datacleaner.xsd");
				InputStream inputStream = resource.getInputStream();
				ServletOutputStream outputStream = res.getOutputStream();
				for (int nextChar = inputStream.read(); nextChar != -1; nextChar = inputStream
						.read()) {
					outputStream.write(nextChar);
				}
			}

		};
	}
}