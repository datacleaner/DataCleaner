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

import java.util.ArrayList;
import java.util.List;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.PojoDatastore;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.concurrent.SingleThreadedTaskRunner;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunner;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.monitor.configuration.JobContext;
import org.eobjects.datacleaner.monitor.configuration.PlaceholderAnalysisJob;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.util.PreviewTransformedDataAnalyzer;
import org.eobjects.metamodel.pojo.ArrayTableDataProvider;
import org.eobjects.metamodel.pojo.TableDataProvider;
import org.eobjects.metamodel.util.CollectionUtils;
import org.eobjects.metamodel.util.Func;
import org.eobjects.metamodel.util.SimpleTableDef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/{tenant}/jobs/{job}.invoke")
public class JobInvocationController {

    @Autowired
    TenantContextFactory _contextFactory;

    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public List<Object[]> invokeJob(@PathVariable("tenant") final String tenant, @PathVariable("job") String jobName,
            final List<Object[]> sourceRecords) throws Throwable {
        
        jobName = jobName.replaceAll("\\+", " ");

        final TenantContext tenantContext = _contextFactory.getContext(tenant);
        final JobContext job = tenantContext.getJob(jobName);

        final String datastoreName = job.getSourceDatastoreName();
        final List<String> columnPaths = job.getSourceColumnPaths();

        final String tablePath = getTablePath(columnPaths);

        final String schemaName;
        final String tableName;
        if (tablePath.indexOf('.') == -1) {
            tableName = tablePath;
            schemaName = null;
        } else {
            schemaName = tablePath.substring(0, tablePath.indexOf('.'));
            tableName = tablePath.substring(tablePath.indexOf('.') + 1);

        }

        final List<TableDataProvider<?>> tableDataProviders = new ArrayList<TableDataProvider<?>>(1);
        final List<String> columnNames = CollectionUtils.map(columnPaths, new Func<String, String>() {
            @Override
            public String eval(String columnPath) {
                return columnPath.substring(tablePath.length() + 1);
            }
        });

        // TODO: No column types added
        final SimpleTableDef tableDef = new SimpleTableDef(tableName, columnNames.toArray(new String[0]));
        tableDataProviders.add(new ArrayTableDataProvider(tableDef, sourceRecords));

        final PojoDatastore placeholderDatastore = new PojoDatastore(datastoreName, schemaName, tableDataProviders);

        final AnalysisJob originalJob = job.getAnalysisJob();

        final PlaceholderAnalysisJob placeholderAnalysisJob = new PlaceholderAnalysisJob(placeholderDatastore,
                originalJob);

        final AnalyzerBeansConfiguration configuration = getRunnerConfiguration(tenantContext);

        final AnalysisRunner runner = new AnalysisRunnerImpl(configuration);

        final AnalysisResultFuture resultFuture = runner.run(placeholderAnalysisJob);

        if (resultFuture.isErrornous()) {
            throw resultFuture.getErrors().get(0);
        }

        final PreviewTransformedDataAnalyzer result = (PreviewTransformedDataAnalyzer) resultFuture.getResults().get(0);

        final List<Object[]> list = result.getList();

        return list;
    }

    private AnalyzerBeansConfiguration getRunnerConfiguration(TenantContext tenantContext) {
        AnalyzerBeansConfiguration configuration = tenantContext.getConfiguration();

        // replace task runner with single threaded taskrunner to ensure order
        // of output records.
        AnalyzerBeansConfigurationImpl conf = (AnalyzerBeansConfigurationImpl) configuration;
        conf = conf.replace(new SingleThreadedTaskRunner());
        return conf;
    }

    private String getTablePath(List<String> columnPaths) {
        String tablePath = StringUtils.getLongestCommonToken(columnPaths, '.');
        final int firstDotIndex = tablePath.indexOf('.');
        final int lastDotIndex = tablePath.lastIndexOf('.');
        if (lastDotIndex != firstDotIndex) {
            tablePath = tablePath.substring(0, lastDotIndex);
        }
        return tablePath;
    }
}
