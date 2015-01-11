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
package org.datacleaner.beans.mock;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import junit.framework.TestCase;

import org.datacleaner.api.Analyzer;
import org.datacleaner.api.Close;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.Provided;
import org.datacleaner.result.NumberResult;

@Named("Row-processing mock")
public class AnalyzerMock implements Analyzer<NumberResult> {

    private static List<AnalyzerMock> instances = new LinkedList<AnalyzerMock>();

    public static List<AnalyzerMock> getInstances() {
        return instances;
    }

    public static void clearInstances() {
        instances.clear();
    }

    public AnalyzerMock() {
        instances.add(this);
    }

    @Configured
    InputColumn<?>[] columns;

    @Configured
    String[] someStringProperty = new String[] { "foobar" };

    public InputColumn<?>[] getColumns() {
        return columns;
    }

    // A field-level @Configured property
    @Configured
    private String configured1;

    public String getConfigured1() {
        return configured1;
    }

    @Configured
    private Integer configured2;

    public Integer getConfigured2() {
        return configured2;
    }

    // A field-level @Provided property
    @Provided
    private Map<String, Long> providedMap;

    public Map<String, Long> getProvidedMap() {
        return providedMap;
    }

    @Provided
    private List<Boolean> providedList;

    public List<Boolean> getProvidedList() {
        return providedList;
    }

    private boolean init1 = false;
    private boolean init2 = false;

    @Initialize
    public void init1() {
        this.init1 = true;
    }

    public boolean isInit1() {
        return init1;
    }

    @Initialize
    public void init2() {
        this.init2 = true;
    }

    public boolean isInit2() {
        return init2;
    }

    private int runCount;
    private long rowCount;

    @Override
    public void run(InputRow row, int count) {
        TestCase.assertNotNull(row);
        TestCase.assertNotNull(count);
        this.runCount++;
        this.rowCount += count;
    }

    public long getRowCount() {
        return rowCount;
    }

    public int getRunCount() {
        return runCount;
    }

    private boolean close1 = false;
    private boolean close2 = false;

    @Close
    public void close1() {
        this.close1 = true;
    }

    public boolean isClose1() {
        return close1;
    }

    @Close
    public void close2() {
        this.close2 = true;
    }

    public boolean isClose2() {
        return close2;
    }

    public NumberResult getResult() {
        return new NumberResult(rowCount);
    }
}
