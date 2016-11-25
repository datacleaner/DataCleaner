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
package org.datacleaner.test.mock;

import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Named;

import org.datacleaner.api.Configured;
import org.datacleaner.api.HasAnalyzerResult;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.result.NumberResult;

/**
 * A mock transformer which also implements {@link HasAnalyzerResult}
 */
@Named("MockTransformerWithAnalyzerResult")
public class MockTransformerWithAnalyzerResult implements Transformer, HasAnalyzerResult<NumberResult> {

    private final AtomicInteger _counter;
    @Configured
    InputColumn<?> column;

    public MockTransformerWithAnalyzerResult() {
        _counter = new AtomicInteger();
    }

    @Initialize
    public void init() {
        _counter.set(0);
    }

    @Override
    public NumberResult getResult() {
        return new NumberResult(_counter.get());
    }

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(String.class, "row id");
    }

    @Override
    public Object[] transform(final InputRow inputRow) {
        _counter.incrementAndGet();
        return new Object[] { inputRow.getId() };
    }

}
