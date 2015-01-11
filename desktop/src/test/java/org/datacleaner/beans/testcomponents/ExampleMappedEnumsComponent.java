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
package org.datacleaner.beans.testcomponents;

import javax.inject.Named;

import org.datacleaner.api.Configured;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.MappedProperty;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;

@Named("Example mapped enums transformer")
public class ExampleMappedEnumsComponent implements Transformer {

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
        return new OutputColumns(columns.length, String.class);
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
