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
package org.datacleaner.cluster.http;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.job.runner.AnalysisListener;
import org.datacleaner.job.runner.AnalysisResultFuture;

/**
 * A simple execution servlet to deploy on a slave node
 */
public class SlaveServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static final String SERVLET_CONTEXT_ATTRIBUTE_CONFIGURATION = "org.datacleaner.configuration";
    public static final String SERVLET_CONTEXT_ATTRIBUTE_ANALYSIS_LISTENER = "org.datacleaner.analysislistener";

    @Inject
    DataCleanerConfiguration _configuration;

    @Inject
    AnalysisListener _analysisListener;

    private final ConcurrentMap<String, AnalysisResultFuture> _runningJobs;

    public SlaveServlet() {
        this(null);
    }

    public SlaveServlet(DataCleanerConfiguration configuration) {
        this(configuration, null);
    }

    public SlaveServlet(DataCleanerConfiguration configuration, AnalysisListener analysisListener) {
        super();
        _configuration = configuration;
        _analysisListener = analysisListener;
        _runningJobs = new ConcurrentHashMap<String, AnalysisResultFuture>();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        final ServletContext servletContext = config.getServletContext();
        if (_configuration == null) {
            final Object configurationAttribute = servletContext.getAttribute(SERVLET_CONTEXT_ATTRIBUTE_CONFIGURATION);
            if (configurationAttribute != null && configurationAttribute instanceof DataCleanerConfiguration) {
                _configuration = (DataCleanerConfiguration) configurationAttribute;
            }
        }
        if (_analysisListener == null) {
            final Object analysisListenerAttribute = servletContext
                    .getAttribute(SERVLET_CONTEXT_ATTRIBUTE_ANALYSIS_LISTENER);
            if (analysisListenerAttribute != null && analysisListenerAttribute instanceof AnalysisListener) {
                _analysisListener = (AnalysisListener) analysisListenerAttribute;
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final SlaveServletHelper helper = new SlaveServletHelper(_configuration, _runningJobs);
        if (_analysisListener == null) {
            helper.handleRequest(req, resp);
        } else {
            helper.handleRequest(req, resp, _analysisListener);
        }
    }
}
