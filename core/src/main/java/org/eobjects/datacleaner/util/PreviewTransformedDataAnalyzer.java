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
package org.eobjects.datacleaner.util;

import java.util.ArrayList;
import java.util.List;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.result.AnalyzerResult;

/**
 * A dummy analyzer used by the Preview Data button (
 * {@link PreviewTransformedDataActionListener}) to collect values from the
 * previewed records.
 * 
 * @author Kasper Sørensen
 */
@AnalyzerBean("Preview transformed data collector")
public class PreviewTransformedDataAnalyzer implements Analyzer<PreviewTransformedDataAnalyzer>, AnalyzerResult {

    private static final long serialVersionUID = 1L;

    @Configured
    InputColumn<?>[] columns;

    private final List<Object[]> list = new ArrayList<Object[]>();

    @Override
    public void run(InputRow row, int distinctCount) {
        Object[] result = new Object[columns.length];
        for (int i = 0; i < columns.length; i++) {
            result[i] = row.getValue(columns[i]);
        }
        list.add(result);
    }

    public List<Object[]> getList() {
        return list;
    }

    public InputColumn<?>[] getColumns() {
        return columns;
    }

    @Override
    public PreviewTransformedDataAnalyzer getResult() {
        return this;
    }
}
