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
package org.datacleaner.beans;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.metamodel.util.LazyRef;
import org.datacleaner.api.Analyzer;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.ColumnProperty;
import org.datacleaner.api.Concurrent;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.result.AnalyzerResultFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("MockAnalyzer")
@Description("For testing purposes. Sleeps for 5 seconds.")
@Concurrent(true)
public class MockAnalyzer implements Analyzer<AnalyzerResultFuture<AnalyzerResult>> {

    private static final Logger logger = LoggerFactory.getLogger(MockAnalyzer.class);
    
    @Inject
    @Configured(value = "Column", order = 1)
    @ColumnProperty(escalateToMultipleJobs = true)
    InputColumn<?> _column;

    /**
     * Main constructor
     */
    public MockAnalyzer() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void run(InputRow row, int distinctCount) {
        // Do nothing
    }

    @Override
    public AnalyzerResultFuture<AnalyzerResult> getResult() {
        return new AnalyzerResultFuture<AnalyzerResult>("MockAnalyzerResultFuture", new LazyRef<AnalyzerResult>() {

            @Override
            protected AnalyzerResult fetch() throws Throwable {
                try {
                    logger.info("Sleeping for 5 seconds...");
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    logger.warn("MockAnalyzerResultFuture sleep period interrupted!");
                }
                return new MockAnalyzerResult();
            }
        });
    }

}
