/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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

import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.job.JobContext;
import org.eobjects.datacleaner.monitor.server.ConfigurationInterceptor;
import org.eobjects.datacleaner.monitor.server.LaunchArtifactProvider;
import org.eobjects.datacleaner.monitor.server.job.DataCleanerJobContext;
import org.eobjects.datacleaner.monitor.shared.model.SecurityRoles;
import org.eobjects.datacleaner.repository.Repository;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.apache.metamodel.util.Action;
import org.apache.metamodel.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(value = "/{tenant}/launch-resources")
public class LaunchResourcesController {

    private static final Logger logger = LoggerFactory.getLogger(LaunchResourcesController.class);

    @Autowired
    LaunchArtifactProvider _launchArtifactProvider;

    @Autowired
    Repository _repository;

    @Autowired
    ConfigurationInterceptor _configurationInterceptor;

    @Autowired
    TenantContextFactory _tenantContextFactory;

    @RequestMapping("/images/app-icon.png")
    public void fetchAppIcon(HttpServletResponse response) throws IOException {
        fetchImage(response, "launch-datacleaner-app-icon.png");
    }

    @RequestMapping("/images/splash.png")
    public void fetchSplashImage(HttpServletResponse response) throws IOException {
        fetchImage(response, "launch-datacleaner-splash.png");
    }

    private void fetchImage(HttpServletResponse response, String path) throws IOException {
        response.setContentType("image/png");

        final InputStream in = getClass().getResourceAsStream(path);
        if (in == null) {
            logger.warn("Could not resolve image: {}", path);
            return;
        }
        try {
            FileHelper.copy(in, response.getOutputStream());
        } catch (Exception e) {
            // errors here often happens when the client aborts because the
            // client Java already has a cached version of the file.
            if (logger.isInfoEnabled()) {
                logger.info("Failed to copy image file '" + path + "'", e);
            }
        } finally {
            FileHelper.safeClose(in);
        }
    }

    @RolesAllowed(SecurityRoles.JOB_EDITOR)
    @RequestMapping("/conf.xml")
    public void fetchConfigurationFile(@PathVariable("tenant") final String tenant,
            @RequestParam(value = "job", required = false) final String jobName,
            @RequestParam(value = "datastore", required = false) final String datastoreName,
            final HttpServletResponse response) throws Exception {

        final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        
        final DataCleanerJobContext jobContext;
        if (StringUtils.isNullOrEmpty(jobName)) { 
            jobContext = null;
        } else {
            final JobContext job = tenantContext.getJob(jobName);
            if (!(job instanceof DataCleanerJobContext)) {
                throw new UnsupportedOperationException("Job not compatible with operation: " + job);
            }
            jobContext = (DataCleanerJobContext) job;
        }

        final RepositoryFile confFile = tenantContext.getConfigurationFile();

        response.setContentType("application/xml");

        final ServletOutputStream out = response.getOutputStream();

        confFile.readFile(new Action<InputStream>() {
            @Override
            public void run(InputStream in) throws Exception {
                // intercept the input stream to decorate it with client-side
                // config elements.
                _configurationInterceptor.intercept(tenant, jobContext, datastoreName, in, out);
            }
        });
    }

    @RequestMapping(value = "/{filename:.+}.jar")
    public void fetchJarFile(HttpServletRequest request, HttpServletResponse response,
            @PathVariable("tenant") final String tenant, @PathVariable("filename") String filename) throws Exception {

        final InputStream in;
        try {
            in = _launchArtifactProvider.readJarFile(filename + ".jar");
        } catch (IllegalArgumentException e) {
            // file was not found
            response.sendError(404, e.getMessage());
            return;
        }

        if (in == null) {
            // file was not found
            response.sendError(404, "No such jar file: " + filename + ".jar");
        }

        response.setContentType("application/x-java-archive");

        final ServletOutputStream out = response.getOutputStream();

        try {
            FileHelper.copy(in, out);
        } catch (Exception e) {
            // errors here often happens when the client aborts because the
            // client Java already has a cached version of the file.
            if (logger.isInfoEnabled()) {
                logger.info("Failed to copy JAR file '" + filename + "'", e);
            }
        } finally {
            FileHelper.safeClose(in);
        }
    }
}
