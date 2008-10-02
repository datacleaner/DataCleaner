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
package dk.eobjects.datacleaner.webmonitor.view;

import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.view.AbstractView;

/**
 * A view capable of rendering RESTful service responses according to the
 * types.xsd schema
 */
public class RestfulServiceView extends AbstractView {

	public static final String TYPES_NAMESPACE = "http://www.eobjects.dk/datacleaner/2.0";
	public static final String TYPES_LOCATION_HINT = "datacleaner.xsd";

	private RestfulNode _rootNode;

	public RestfulNode getRootNode() {
		return _rootNode;
	}

	public void setRootNode(RestfulNode rootNode) {
		_rootNode = rootNode;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void renderMergedOutputModel(Map model, HttpServletRequest req,
			HttpServletResponse res) throws Exception {
		if (_rootNode == null) {
			throw new IllegalStateException("Cannot render without a root node");
		}
		res.setContentType("application/xml");

		PrintWriter out = res.getWriter();
		out.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

		_rootNode.addAttribute("xmlns", TYPES_NAMESPACE);
		_rootNode.addAttribute("xmlns:xsi",
				"http://www.w3.org/2001/XMLSchema-instance");
		_rootNode.addAttribute("xsi:schemaLocation", TYPES_NAMESPACE + " "
				+ TYPES_LOCATION_HINT + " ");
		out.println(_rootNode);
	}
}