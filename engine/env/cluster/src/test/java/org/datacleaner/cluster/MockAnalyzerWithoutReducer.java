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
package org.datacleaner.cluster;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import org.datacleaner.beans.api.Analyzer;
import org.datacleaner.beans.api.Configured;
import org.datacleaner.data.InputColumn;
import org.datacleaner.data.InputRow;
import org.datacleaner.result.ListResult;

@Named("Analyzer without reducer")
public class MockAnalyzerWithoutReducer implements Analyzer<ListResult<String>> {

    @Configured
    InputColumn<?>[] input;

    @Override
    public ListResult<String> getResult() {
        List<String> list = new ArrayList<String>();
        list.add("foobar");
        return new ListResult<String>(list);
    }

    @Override
    public void run(InputRow row, int arg1) {
        // do nothing
    }

}
