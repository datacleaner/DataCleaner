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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.RolesAllowed;

import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.configuration.AnalyzerBeansConfigurationImpl;
import org.datacleaner.connection.PojoDatastore;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.concurrent.SingleThreadedTaskRunner;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunner;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.util.StringUtils;
import org.datacleaner.monitor.configuration.PlaceholderAnalysisJob;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.job.JobContext;
import org.datacleaner.monitor.server.job.DataCleanerJobContext;
import org.datacleaner.monitor.shared.model.SecurityRoles;
import org.datacleaner.util.PreviewTransformedDataAnalyzer;
import org.apache.metamodel.pojo.ArrayTableDataProvider;
import org.apache.metamodel.pojo.TableDataProvider;
import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.Func;
import org.apache.metamodel.util.HasNameMapper;
import org.apache.metamodel.util.SimpleTableDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Strings;

@Controller
@RequestMapping(value = "/{tenant}/jobs/{job}.invoke")
public class JobInvocationController {

    private static final Logger logger = LoggerFactory.getLogger(JobInvocationController.class);

    @Autowired
    TenantContextFactory _contextFactory;

    /**
     * Takes a JSON request body on this form (2 rows with 1 int and 2 strings
     * each):
     * 
     * <pre>
     * {"rows":[
     *   {"values":[1,"hello","John"]},
     *   {"values":[1,"howdy","Jane"]}
     * ]}
     * </pre>
     * 
     * These values will be passed as source records for a job, and the
     * transformed records will be returned.
     * 
     * @param tenant
     * @param jobName
     * @param input
     * @return
     * @throws Throwable
     */
    @RequestMapping(method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    @ResponseBody
    @RolesAllowed(SecurityRoles.TASK_ATOMIC_EXECUTOR)
    public JobInvocationPayload invokeJob(@PathVariable("tenant") final String tenant,
            @PathVariable("job") String jobName, @RequestBody final JobInvocationPayload input) throws Throwable {
        logger.info("Request payload: {}", input);

        jobName = jobName.replaceAll("\\+", " ");

        final TenantContext tenantContext = _contextFactory.getContext(tenant);
        final JobContext job = tenantContext.getJob(jobName);

        if (!(job instanceof DataCleanerJobContext)) {
            throw new UnsupportedOperationException("Job not compatible with operation: " + jobName);
        }

        final DataCleanerJobContext analysisJobContext = (DataCleanerJobContext) job;
        final String datastoreName = analysisJobContext.getSourceDatastoreName();
        final List<String> columnPaths = analysisJobContext.getSourceColumnPaths();

        final String tablePath = getTablePath(columnPaths);

        String schemaName;
        String tableName;
        if (tablePath.indexOf('.') == -1) {
            tableName = tablePath;
            schemaName = null;
        } else {
            schemaName = tablePath.substring(0, tablePath.indexOf('.'));
            tableName = tablePath.substring(tablePath.indexOf('.') + 1);
        }

        if (Strings.isNullOrEmpty(schemaName)) {
            schemaName = "schema";
        }
        if (Strings.isNullOrEmpty(tableName)) {
            tableName = "table";
        }

        final List<TableDataProvider<?>> tableDataProviders = new ArrayList<TableDataProvider<?>>(1);
        final List<String> columnNames = CollectionUtils.map(columnPaths, new Func<String, String>() {
            @Override
            public String eval(String columnPath) {
                if (!tablePath.isEmpty()) {
                    return columnPath.substring(tablePath.length() + 1);
                }
                return columnPath;
            }
        });

        final List<JobInvocationRowData> inputRows = input.getRows();
        final List<Object[]> inputRowData = CollectionUtils.map(inputRows, new Func<JobInvocationRowData, Object[]>() {
            @Override
            public Object[] eval(JobInvocationRowData rowData) {
                return rowData.getValues();
            }
        });

        // TODO: No column types added
        final SimpleTableDef tableDef = new SimpleTableDef(tableName, columnNames.toArray(new String[0]));
        tableDataProviders.add(new ArrayTableDataProvider(tableDef, inputRowData));

        final PojoDatastore placeholderDatastore = new PojoDatastore(datastoreName, schemaName, tableDataProviders);

        final AnalysisJob originalJob = analysisJobContext.getAnalysisJob();

        final PlaceholderAnalysisJob placeholderAnalysisJob = new PlaceholderAnalysisJob(placeholderDatastore,
                originalJob);

        final AnalyzerBeansConfiguration configuration = getRunnerConfiguration(tenantContext);

        final AnalysisRunner runner = new AnalysisRunnerImpl(configuration);

        final AnalysisResultFuture resultFuture = runner.run(placeholderAnalysisJob);

        if (resultFuture.isErrornous()) {
            throw resultFuture.getErrors().get(0);
        }

        final PreviewTransformedDataAnalyzer result = (PreviewTransformedDataAnalyzer) resultFuture.getResults().get(0);

        final JobInvocationPayload output = new JobInvocationPayload();
        final List<String> outputColumnNames = CollectionUtils.map(result.getColumns(), new HasNameMapper());
        output.setColumns(outputColumnNames);

        final List<Object[]> collectedRowData = result.getList();
        for (Object[] outputRow : collectedRowData) {
            output.addRow(outputRow);
        }

        logger.info("Response payload: {}", output);

        return output;
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
