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
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eobjects.datacleaner.util.ResourceManager;
import org.eobjects.metamodel.util.FileHelper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/{tenant}/launch-resources")
public class LaunchResourcesController {

    @RequestMapping("images/app-icon.png")
    public void fetchAppIcon(HttpServletResponse response) throws IOException {
        fetchImage(response, "images/window/app-icon.png");
    }

    private void fetchImage(HttpServletResponse response, String path) throws IOException {
        response.setContentType("image/png");

        URL resource = ResourceManager.getInstance().getUrl(path);
        InputStream in = resource.openStream();
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

        final ServletContext context = request.getSession().getServletContext();
        final File libFolder = getLibFolder(context);
        final String realPath = libFolder.getPath() + '/' + filename + ".jar";
        final FileInputStream in = new FileInputStream(realPath);
        try {
            FileHelper.copy(in, out);
        } finally {
            FileHelper.safeClose(in);
        }
    }

    public static File getLibFolder(ServletContext context) {
        final String libPath = context.getRealPath("WEB-INF/lib");
        final File libFolder = new File(libPath);
        if (!libFolder.exists()) {
            // in dev mode the folder does not exist, try with maven built lib
            // folder
            final File alternativeFolder = new File("target/DataCleaner-monitor/WEB-INF/lib");
            if (alternativeFolder.exists()) {
                return alternativeFolder;
            }
        }
        return libFolder;
    }
}
