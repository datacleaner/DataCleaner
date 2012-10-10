/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.datacleaner.monitor.server.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.FileDatastore;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.server.ConfigurationInterceptor;
import org.eobjects.datacleaner.monitor.server.LaunchArtifactProvider;
import org.eobjects.datacleaner.monitor.shared.model.SecurityRoles;
import org.eobjects.datacleaner.repository.Repository;
import org.eobjects.datacleaner.util.ResourceManager;
import org.eobjects.metamodel.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class FileBasedRepositoryDownloadServlet {

	private static final long serialVersionUID = -4156454487759119275L;
	private static final int BATCH_SIZE = 1024;

	private static final Logger logger = LoggerFactory
			.getLogger(FileBasedRepositoryDownloadServlet.class);

	@Autowired
	LaunchArtifactProvider _launchArtifactProvider;

	@Autowired
	Repository _repository;

	@Autowired
	ConfigurationInterceptor _configurationInterceptor;

	@Autowired
	TenantContextFactory _tenantContextFactory;

	@RequestMapping("/images/save.png")
	public void fetchAppIcon(HttpServletResponse response) throws IOException {
		fetchImage(response, "images/window/save.png");
	}

	private void fetchImage(HttpServletResponse response, String path)
			throws IOException {
		response.setContentType("image/png");

		final URL resource = ResourceManager.getInstance().getUrl(path);
		final InputStream in = resource.openStream();
		try {
			FileHelper.copy(in, response.getOutputStream());
		} finally {
			FileHelper.safeClose(in);
		}
	}

	@RolesAllowed(SecurityRoles.ADMIN)
	@RequestMapping(value = "/{tenant}/datastores/{datastore}.download", method = RequestMethod.GET)
	protected void downloadFileRepo(HttpServletRequest request,
			HttpServletResponse response,
			@PathVariable("tenant") final String tenant,
			@PathVariable("datastore") String datastorename)
			throws ServletException, IOException {
		AnalyzerBeansConfiguration configuration = _tenantContextFactory
				.getContext(tenant).getConfiguration();

		datastorename = datastorename.replaceAll("\\+", " ");
		Datastore ds = configuration.getDatastoreCatalog().getDatastore(
				datastorename);
		if (ds instanceof FileDatastore) {
			String filename = ((FileDatastore) ds).getFilename();
			File file = new File(filename);
			FileInputStream is = new FileInputStream(file);
			// String contentType = "text/csv";
			// response.setContentType(contentType);
			response.setHeader("Content-Disposition", "attachment; filename="
					+ datastorename);
			OutputStream os = response.getOutputStream();

			try {
				FileHelper.copy(is, os);
			} catch (Exception e) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			} finally {
				os.flush();
				os.close();
				is.close();
			}

		}
	}

}
