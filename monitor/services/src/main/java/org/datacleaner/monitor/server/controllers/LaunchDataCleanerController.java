/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
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
package org.datacleaner.monitor.server.controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.metamodel.util.FileHelper;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.job.JobContext;
import org.datacleaner.monitor.server.LaunchArtifactProvider;
import org.datacleaner.monitor.server.SecurityConfiguration;
import org.datacleaner.monitor.server.job.DataCleanerJobContext;
import org.datacleaner.monitor.shared.model.SecurityRoles;
import org.datacleaner.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller responsible for generating and providing a JNLP file for launching
 * datacleaner as a WebStart application on the client.
 */
@Controller
public class LaunchDataCleanerController {

    private static final String RESOURCES_FOLDER = "launch-resources/";

    @Autowired
    LaunchArtifactProvider _launchArtifactProvider;

    @Autowired
    TenantContextFactory _contextFactory;

    @Autowired(required = false)
    SecurityConfiguration securityURLHolder;

    @RolesAllowed(SecurityRoles.JOB_EDITOR)
    @RequestMapping(value = "/{tenant}/datastores/{datastore}.analyze.jnlp", method = RequestMethod.GET)
    @ResponseBody
    public void launchDataCleanerForDatastore(final HttpServletRequest request, final HttpServletResponse response,
            @PathVariable("tenant") final String tenant, @PathVariable("datastore") String datastoreName)
            throws IOException {
        datastoreName = datastoreName.replaceAll("\\+", " ");

        final DataCleanerConfiguration configuration = _contextFactory.getContext(tenant).getConfiguration();
        final Datastore ds = configuration.getDatastoreCatalog().getDatastore(datastoreName);
        if (ds == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No such datastore: " + datastoreName);
            return;
        }
        final String scheme = request.getScheme();
        final String hostname = request.getServerName();
        final int port = getPort(request, scheme);
        final String contextPath = request.getContextPath();

        final String encodedDatastoreName = URLEncoder.encode(datastoreName, FileHelper.UTF_8_ENCODING);

        final String jnlpHref = "datastores/" + encodedDatastoreName + ".analyze.jnlp";
        final String confPath = '/' + RESOURCES_FOLDER + "conf.xml?datastore=" + encodedDatastoreName;

        writeJnlpResponse(request, tenant, response, scheme, hostname, port, contextPath, jnlpHref, null, datastoreName,
                confPath);
    }

    @RolesAllowed(SecurityRoles.JOB_EDITOR)
    @RequestMapping(value = "/{tenant}/jobs/{job:.+}.launch.jnlp", method = RequestMethod.GET)
    @ResponseBody
    public void launchDataCleanerForJob(final HttpServletRequest request, final HttpServletResponse response,
            @PathVariable("tenant") final String tenant, @PathVariable("job") String jobName) throws IOException {
        jobName = jobName.replaceAll("\\+", " ");

        final TenantContext context = _contextFactory.getContext(tenant);
        if (!context.containsJob(jobName)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No such job: " + jobName);
            return;
        }

        final JobContext job = context.getJob(jobName);

        if (!(job instanceof DataCleanerJobContext)) {
            throw new UnsupportedOperationException("Job not compatible with operation: " + job);
        }

        final String datastoreName = ((DataCleanerJobContext) job).getSourceDatastoreName();
        final String encodedJobName = URLEncoder.encode(jobName, FileHelper.UTF_8_ENCODING);

        final String scheme = request.getScheme();
        final String hostname = request.getServerName();
        final int port = getPort(request, scheme);

        final String contextPath = request.getContextPath();

        final String jnlpHref = "jobs/" + encodedJobName + ".launch.jnlp";
        final String jobPath = "/jobs/" + encodedJobName + ".analysis.xml";
        final String confPath = '/' + RESOURCES_FOLDER + "conf.xml?job=" + encodedJobName;

        writeJnlpResponse(request, tenant, response, scheme, hostname, port, contextPath, jnlpHref, jobPath,
                datastoreName, confPath);
    }

    private int getPort(final HttpServletRequest request, final String scheme) {
        final int port;
        if ("https".equals(scheme) && request.getServerPort() == 80) {
            port = 443;
        } else {
            port = request.getServerPort();
        }
        return port;
    }

    private void writeJnlpResponse(final HttpServletRequest request, final String tenant,
            final HttpServletResponse response, final String scheme, final String hostname, final int port,
            final String contextPath, final String jnlpHref, final String jobPath, final String datastoreName,
            final String confPath) throws UnsupportedEncodingException, IOException {
        response.setContentType("application/x-java-jnlp-file");

        final String baseUrl = createBaseUrl(scheme, hostname, port, contextPath, tenant);
        final String jobUrl = (jobPath == null ? null : baseUrl + jobPath);
        final String confUrl = baseUrl + confPath;

        final String username;
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            username = null;
        } else {
            username = authentication.getName();
        }

        final PrintWriter out = response.getWriter();

        final InputStream in = getClass().getResourceAsStream("launch-datacleaner-template.xml");
        try {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(in, FileHelper.UTF_8_ENCODING));
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                line = line.replaceAll("\\$BASE_URL", baseUrl);
                line = line.replaceAll("\\$JNLP_HREF", jnlpHref);
                if (jobUrl == null && (line.indexOf("<argument>-job</argument>") != -1
                        || line.indexOf("$JOB_URL") != -1)) {
                    // omit the JOB_URL argument lines
                    line = "";
                } else {
                    line = line.replaceAll("\\$JOB_URL", jobUrl);
                }
                line = line.replaceAll("\\$DATASTORE_NAME", datastoreName);
                line = line.replaceAll("\\$CONF_URL", confUrl);
                line = line.replaceAll("\\$MONITOR_HOSTNAME", hostname);
                line = line.replaceAll("\\$MONITOR_PORT", Integer.toString(port));
                line = line.replaceAll("\\$MONITOR_CONTEXT", contextPath);
                line = line.replaceAll("\\$MONITOR_TENANT", tenant);
                if (username == null && line.indexOf("$MONITOR_USERNAME") != -1) {
                    // omit the username line
                    line = "";
                } else {
                    line = line.replaceAll("\\$MONITOR_USERNAME", username);
                }

                if (securityURLHolder == null) {
                    if (line.indexOf("$MONITOR_SECURITY_MODE") != -1
                            || line.indexOf("$MONITOR_SECURITY_CASSERVERURL") != -1) {
                        line = "";
                    }
                } else {
                    line = line.replaceAll("\\$MONITOR_SECURITY_MODE", securityURLHolder.getSecurityMode());
                    line = line.replaceAll("\\$MONITOR_SECURITY_CASSERVERURL", securityURLHolder.getCasServerUrl());
                }

                line = line.replaceAll("\\$MONITOR_HTTPS", ("https".equals(scheme) ? "true" : "false"));
                if (line.indexOf("$JAR_HREF") == -1) {
                    out.write(line);
                    out.write('\n');
                } else {
                    insertJarFiles(request.getSession().getServletContext(), out, line);
                }
            }
        } finally {
            FileHelper.safeClose(in);
        }
    }

    private void insertJarFiles(final ServletContext context, final Writer out, final String templateLine)
            throws IOException {
        final List<String> jarFilenames = _launchArtifactProvider.getJarFilenames();
        for (final String filename : jarFilenames) {
            final String line;
            if (filename.startsWith("http://") || filename.startsWith("https://")) {
                line = templateLine.replaceAll("\\$JAR_HREF", filename);
            } else {
                line = templateLine.replaceAll("\\$JAR_HREF", RESOURCES_FOLDER + filename);
            }
            out.write(line);
            out.write('\n');
        }
    }

    private String createBaseUrl(final String scheme, final String hostname, final int port, final String contextPath,
            final String tenant) {
        final StringBuilder baseUrl = new StringBuilder();
        baseUrl.append(scheme);
        baseUrl.append("://");
        baseUrl.append(hostname);
        baseUrl.append(':');
        baseUrl.append(port);

        if (!contextPath.startsWith("/")) {
            baseUrl.append('/');
        }

        if (!StringUtils.isNullOrEmpty(contextPath) && !"/".equals(contextPath)) {
            baseUrl.append(contextPath);
            if (!contextPath.endsWith("/")) {
                baseUrl.append('/');
            }
        }

        baseUrl.append("repository/");
        baseUrl.append(tenant);

        return baseUrl.toString();
    }

    public void setSecurityURLHolder(final SecurityConfiguration securityURLHolder) {
        this.securityURLHolder = securityURLHolder;
    }

}
