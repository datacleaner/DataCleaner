/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.analyzer.beans.testcomponents;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.MappedProperty;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@TransformerBean("Example mapped enums transformer")
public class ExampleMappedEnumsComponent implements Transformer<String> {

    public static enum SomeEnum {
        ONE, TWO, THREE, FOUR, FIVE, SIX
    }
    
    @Configured
    InputColumn<?>[] columns;
    
    @Configured
    @MappedProperty("Columns")
    SomeEnum[] enums;
    
    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(columns.length);
    }

    @Override
    public String[] transform(InputRow arg0) {
        String[] strings = new String[enums.length];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = enums[i].name();
        }
        return strings;
    }

}
