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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;

import org.apache.metamodel.pojo.ArrayTableDataProvider;
import org.apache.metamodel.pojo.TableDataProvider;
import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.Func;
import org.apache.metamodel.util.HasNameMapper;
import org.apache.metamodel.util.SimpleTableDef;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.connection.PojoDatastore;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.concurrent.SingleThreadedTaskRunner;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunner;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.monitor.configuration.PlaceholderAnalysisJob;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.job.JobContext;
import org.datacleaner.monitor.server.job.DataCleanerJobContext;
import org.datacleaner.monitor.shared.model.SecurityRoles;
import org.datacleaner.util.PreviewTransformedDataAnalyzer;
import org.datacleaner.util.StringUtils;
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
    @RequestMapping(value = "/{tenant}/jobs/{job:.+}.invoke", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    @ResponseBody
    @RolesAllowed(SecurityRoles.TASK_ATOMIC_EXECUTOR)
    public JobInvocationPayload invokeJob(@PathVariable("tenant") final String tenant,
            @PathVariable("job") String jobName, @RequestBody final JobInvocationPayload input) throws Throwable {
        logger.info("Request payload: {}", input);


        final TenantContext tenantContext = _contextFactory.getContext(tenant);
        final DataCleanerJobContext analysisJobContext = (DataCleanerJobContext) getJob(jobName, tenantContext);
        final String tablePath = getTablePath(analysisJobContext.getSourceColumnPaths());
        String schemaName = getSchemaName(tablePath);
        String tableName = getTableName(tablePath);

        final List<TableDataProvider<?>> tableDataProviders = new ArrayList<TableDataProvider<?>>(1);
        final List<String> columnNames = getColumnNames(analysisJobContext.getSourceColumnPaths(), tablePath);

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

        final String datastoreName = analysisJobContext.getSourceDatastoreName();
        final PojoDatastore placeholderDatastore = new PojoDatastore(datastoreName, schemaName, tableDataProviders);

        final AnalysisJob originalJob = analysisJobContext.getAnalysisJob();

        final PlaceholderAnalysisJob placeholderAnalysisJob = new PlaceholderAnalysisJob(placeholderDatastore,
                originalJob);

        final DataCleanerConfiguration configuration = getRunnerConfiguration(tenantContext);

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

    /**
     * Takes a JSON request body containing an array of key value pairs (the example below has 2 rows with 1 int and 2 strings
     * each):
     *
     * <pre>
     * {"rows":[
     *   {"id":1, "name":"John", "message": "hello"},
     *   {"id":2, "name":"Jane", "message": "howdy"}
     * ]}
     * </pre>
     * The column names as known in the datastore are passed as key for the values.
     * These values will be passed as source records for a job, and the
     * transformed records will be returned.
     *
     * @param tenant
     * @param jobName
     * @param input
     * @return - returns the output in row/ column format and adds a list with the output/ value map e.g. {"outputColumn1": "Output Value 1", "outputColumn2": "Output Value 2"} per row
     * @throws Throwable
     */
    @RequestMapping(value = "/{tenant}/jobs/{job:.+}.invoke/mapped", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    @ResponseBody
    @RolesAllowed(SecurityRoles.TASK_ATOMIC_EXECUTOR)
    public JobInvocationPayload invokeJobMapped(@PathVariable("tenant") final String tenant,
            @PathVariable("job") String jobName, @RequestBody final JobInvocationPayload input) throws Throwable {

        final TenantContext tenantContext = _contextFactory.getContext(tenant);
        final DataCleanerJobContext analysisJobContext = (DataCleanerJobContext) getJob(jobName, tenantContext);
        final List<String> columnPaths = analysisJobContext.getSourceColumnPaths();
        final List<String> columnNames = getColumnNames(columnPaths,  getTablePath(columnPaths));

        JobInvocationPayload convertedInput = new JobInvocationPayload();
        convertedInput.setRows(toRows(columnNames, input.getColumnValueMap()));

        JobInvocationPayload output = invokeJob(tenant, jobName, convertedInput);
        output.setColumnValueMap(toColumnValueMap(output.getColumns(), output.getRows()));
        return output;
    }

    private String getTableName(String tablePath) {
        String tableName = null;
        if (tablePath.indexOf('.') == -1) {
            tableName = tablePath;
        } else {
            tableName = tablePath.substring(tablePath.indexOf('.') + 1);
        }
        if (Strings.isNullOrEmpty(tableName)) {
            tableName = "table";
        }
        return tableName;
    }

    private String getSchemaName(String tablePath) {
        String schemaName = null;
        if (tablePath.indexOf('.') > -1) {
            schemaName = tablePath.substring(0, tablePath.indexOf('.'));
        }
        if (Strings.isNullOrEmpty(schemaName)) {
            schemaName = "schema";
        }
        return schemaName;
    }

    private List<String> getColumnNames(List<String> columnPaths, String tablePath){
        return CollectionUtils.map(columnPaths, new Func<String, String>() {
            @Override
            public String eval(String columnPath) {
                if (!tablePath.isEmpty()) {
                    return columnPath.substring(tablePath.length() + 1);
                }
                return columnPath;
            }
        });
    }

    private JobContext getJob(String jobName, TenantContext tenantContext){
        jobName = jobName.replaceAll("\\+", " ");
        final JobContext job = tenantContext.getJob(jobName);
        if (!(job instanceof DataCleanerJobContext)) {
            throw new UnsupportedOperationException("Job not compatible with operation: " + jobName);
        }
        return job;
    }

    private DataCleanerConfiguration getRunnerConfiguration(TenantContext tenantContext) {
        final DataCleanerConfiguration configuration = tenantContext.getConfiguration();
        final DataCleanerEnvironment environment = configuration.getEnvironment();

        // replace task runner with single threaded taskrunner to ensure order
        // of output records.
        final DataCleanerEnvironmentImpl replacementEnvironment = new DataCleanerEnvironmentImpl(environment)
                .withTaskRunner(new SingleThreadedTaskRunner());

        return new DataCleanerConfigurationImpl(configuration).withEnvironment(replacementEnvironment);
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

    private List<JobInvocationRowData> toRows(List<String> columnNames, List<Map<String, Object>> columnValueMapRows){
        List<JobInvocationRowData> result = new ArrayList<>();
        for(Map<String, Object> columnValueMap: columnValueMapRows){
            List<Object> values = new ArrayList<>();
            for(String columnName: columnNames){
                if(columnValueMap.containsKey(columnName)){
                    values.add(columnValueMap.get(columnName));
                }else {
                    values.add(null);
                }
            }
            result.add(new JobInvocationRowData(values.toArray(new Object[values.size()])));
        }

        return result;
    }

    private List<Map<String, Object>> toColumnValueMap(List<String> columnNames, List<JobInvocationRowData> rows){
        List<Map<String, Object>> result = new ArrayList<>();
        for(JobInvocationRowData row: rows){
            Object[] values = row.getValues();
            Map<String, Object> columnValueMap = new HashMap<>();
            int index = 0;
            for(String column: columnNames){
                columnValueMap.put(column, values[index]);
                index++;
            }
            result.add(columnValueMap);
        }
        return result;
    }
}
