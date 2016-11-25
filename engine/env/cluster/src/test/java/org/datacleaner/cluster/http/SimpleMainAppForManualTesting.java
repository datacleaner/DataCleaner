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

import java.util.ArrayList;
import java.util.List;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.beans.CompletenessAnalyzer;
import org.datacleaner.beans.CompletenessAnalyzerResult;
import org.datacleaner.cluster.ClusterTestHelper;
import org.datacleaner.cluster.DistributedAnalysisRunner;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.runner.AnalysisResultFuture;

public class SimpleMainAppForManualTesting {

    public static void main(final String[] args) throws Throwable {

        // create a HTTP BASIC enabled HTTP client
        final CloseableHttpClient httpClient = HttpClients.createSystem();

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("admin", "admin");
        credentialsProvider.setCredentials(new AuthScope("localhost", 8080), credentials);
        credentialsProvider.setCredentials(new AuthScope("localhost", 9090), credentials);

        final HttpClientContext httpClientContext = HttpClientContext.create();
        httpClientContext.setCredentialsProvider(credentialsProvider);

        // register endpoints
        final List<String> slaveEndpoints = new ArrayList<>();
        slaveEndpoints.add("http://localhost:8080/DataCleaner-monitor/repository/demo/cluster_slave_endpoint");
        slaveEndpoints.add("http://localhost:9090/DataCleaner-monitor/repository/demo/cluster_slave_endpoint");

        final HttpClusterManager clusterManager = new HttpClusterManager(httpClient, httpClientContext, slaveEndpoints);

        final DataCleanerConfiguration configuration = ClusterTestHelper.createConfiguration("manual_test", false);

        // build a job that concats names and inserts the concatenated names
        // into a file
        final AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(configuration);
        jobBuilder.setDatastore("orderdb");
        jobBuilder.addSourceColumns("CUSTOMERS.CUSTOMERNUMBER", "CUSTOMERS.CUSTOMERNAME", "CUSTOMERS.CONTACTFIRSTNAME",
                "CUSTOMERS.CONTACTLASTNAME");

        final AnalyzerComponentBuilder<CompletenessAnalyzer> completeness =
                jobBuilder.addAnalyzer(CompletenessAnalyzer.class);
        completeness.addInputColumns(jobBuilder.getSourceColumns());
        completeness.setConfiguredProperty("Conditions",
                new CompletenessAnalyzer.Condition[] { CompletenessAnalyzer.Condition.NOT_BLANK_OR_NULL,
                        CompletenessAnalyzer.Condition.NOT_BLANK_OR_NULL,
                        CompletenessAnalyzer.Condition.NOT_BLANK_OR_NULL,
                        CompletenessAnalyzer.Condition.NOT_BLANK_OR_NULL });

        final AnalysisJob job = jobBuilder.toAnalysisJob();
        jobBuilder.close();

        final AnalysisResultFuture result = new DistributedAnalysisRunner(configuration, clusterManager).run(job);

        if (result.isErrornous()) {
            throw result.getErrors().get(0);
        }

        final List<AnalyzerResult> results = result.getResults();
        for (final AnalyzerResult analyzerResult : results) {
            System.out.println("result:" + analyzerResult);
            if (analyzerResult instanceof CompletenessAnalyzerResult) {
                final int invalidRowCount = ((CompletenessAnalyzerResult) analyzerResult).getInvalidRowCount();
                System.out.println("invalid records found: " + invalidRowCount);
            } else {
                System.out.println("class: " + analyzerResult.getClass().getName());
            }
        }
    }
}
