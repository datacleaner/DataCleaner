/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.util.batch;

import java.util.Collections;
import java.util.List;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.Func;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TransformerBean("Mock batcher")
public class MockBatchTransformer extends BatchTransformer {

    private static final Logger logger = LoggerFactory.getLogger(MockBatchTransformer.class);

    @Configured
    InputColumn<String> input;

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns("BAZ");
    }

    @Override
    public void map(BatchSource<InputRow> source, BatchSink<Object[]> sink) {
        logger.info("map({} records)", source.size());
        ;

        List<InputRow> list = source.toList();
        List<String> values = CollectionUtils.map(list, new Func<InputRow, String>() {
            @Override
            public String eval(InputRow row) {
                return row.getValue(input);
            }
        });

        // sort the values
        Collections.sort(values);

        for (int i = 0; i < source.size(); i++) {
            String value = values.get(i);
            sink.setOutput(i, new Object[] { value });
            logger.info("setOutput({}, {})", i, value);
        }
    }

}
