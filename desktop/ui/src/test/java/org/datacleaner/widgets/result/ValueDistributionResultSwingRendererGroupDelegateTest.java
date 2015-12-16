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
package org.datacleaner.widgets.result;

import junit.framework.TestCase;

import org.datacleaner.api.InputColumn;
import org.datacleaner.beans.valuedist.SingleValueDistributionResult;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.result.SingleValueFrequency;
import org.datacleaner.result.ValueCountListImpl;
import org.datacleaner.storage.RowAnnotationImpl;

public class ValueDistributionResultSwingRendererGroupDelegateTest extends TestCase {

    private InputColumn<String> column = new MockInputColumn<String>("col", String.class);

    public void testVanilla() throws Exception {
        ValueCountListImpl topValueCount = ValueCountListImpl.createFullList();
        for (int i = 0; i < 40; i++) {
            // 40 values with unique counts
            topValueCount.register(new SingleValueFrequency("v" + i, i + 1));
        }

        ValueDistributionResultSwingRendererGroupDelegate r = new ValueDistributionResultSwingRendererGroupDelegate(
                "foo", 50, null, null);
        r.renderGroupResult(new SingleValueDistributionResult(column.getName(), topValueCount, 0, 0, 0, null,
                new RowAnnotationImpl(), null, null));

        assertEquals(40, r.getDataSetItemCount());
    }

}
