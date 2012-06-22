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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eobjects.datacleaner.monitor.server.LaunchArtifactProvider;
import org.eobjects.datacleaner.repository.Repository;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.util.ResourceManager;
import org.eobjects.metamodel.util.FileHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/{tenant}/launch-resources")
public class LaunchResourcesController {

    @Autowired
    LaunchArtifactProvider _launchArtifactProvider;

    @Autowired
    Repository _repository;

    @RequestMapping("/images/app-icon.png")
    public void fetchAppIcon(HttpServletResponse response) throws IOException {
        fetchImage(response, "images/window/app-icon.png");
    }

    private void fetchImage(HttpServletResponse response, String path) throws IOException {
        response.setContentType("image/png");

        final URL resource = ResourceManager.getInstance().getUrl(path);
        final InputStream in = resource.openStream();
        try {
            FileHelper.copy(in, response.getOutputStream());
        } finally {
            FileHelper.safeClose(in);
        }
    }

    @RequestMapping("/conf.xml")
    public void fetchConfigurationFile(@PathVariable("tenant") final String tenant, final HttpServletResponse response)
            throws IOException {
        final RepositoryFolder tenantFolder = _repository.getFolder(tenant);
        if (tenantFolder == null) {
            throw new IllegalArgumentException("No such tenant: " + tenant);
        }

        final RepositoryFile confFile = tenantFolder.getFile("conf.xml");

        response.setContentType("application/xml");

        final InputStream in = confFile.readFile();

        try {
            FileHelper.copy(in, response.getOutputStream());
        } finally {
            FileHelper.safeClose(in);
        }
    }

    @RequestMapping(value = "/{filename:.+}.jar")
    public void fetchJarFile(HttpServletRequest request, HttpServletResponse response,
            @PathVariable("tenant") final String tenant, @PathVariable("filename") String filename) throws IOException {

        response.setContentType("application/x-java-archive");

        final ServletOutputStream out = response.getOutputStream();

        final InputStream in = _launchArtifactProvider.readJarFile(filename + ".jar");
        try {
            FileHelper.copy(in, out);
        } finally {
            FileHelper.safeClose(in);
        }
    }
}
