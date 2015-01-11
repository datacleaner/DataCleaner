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
package org.datacleaner.job.runner;

import java.util.concurrent.atomic.AtomicInteger;

import org.datacleaner.beans.api.Alias;
import org.datacleaner.beans.api.Configured;
import org.datacleaner.beans.api.Initialize;
import org.datacleaner.beans.api.OutputColumns;
import org.datacleaner.beans.api.Transformer;
import javax.inject.Named;
import org.datacleaner.data.InputColumn;
import org.datacleaner.data.InputRow;

@Named("Transformer mock")
@Alias("Mock transformer")
public class TransformerMock implements Transformer {

    @Configured
    InputColumn<?>[] input;

    private AtomicInteger i;

    @Initialize
    public void init() {
        i = new AtomicInteger(0);
    }

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(input.length, Integer.class);
    }

    @Override
    public Integer[] transform(InputRow inputRow) {
        Integer[] res = new Integer[input.length];
        res[0] = i.incrementAndGet();
        return res;
    }
}
