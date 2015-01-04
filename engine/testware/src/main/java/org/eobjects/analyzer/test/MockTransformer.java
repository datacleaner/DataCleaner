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
package org.eobjects.analyzer.test;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.ComponentContext;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@TransformerBean("Mock transformer")
public class MockTransformer implements Transformer<String> {

    @Configured
    InputColumn<?> input;

    @Inject
    ComponentContext componentContext;

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns("mock output");
    }

    @Override
    public String[] transform(InputRow inputRow) {
        Object value = inputRow.getValue(input);

        componentContext.publishMessage(new MockTransformerMessage("Mocking: " + value, input));

        return new String[] { "mocked: " + value };
    }

    public void setInput(InputColumn<?> input) {
        this.input = input;
    }

    public InputColumn<?> getInput() {
        return input;
    }
    
}
