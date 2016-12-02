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
package org.datacleaner.beans.numbers;

import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Named;

import org.apache.metamodel.util.HasName;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.NumbersCategory;

@Named("Generate ID")
@Description("Generates a unique and sequential record ID")
@Categorized(NumbersCategory.class)
public class GenerateIdTransformer implements Transformer {

    public enum IdType implements HasName {
        SEQUENCE("Sequence"), ROW_NUMBER("Row number");

        private final String name;

        IdType(final String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    private final AtomicLong _counter;
    @Configured
    @Description(
            "A type of ID which will be generated for each record in scope. Current options: sequential numbers or row number.")
    IdType idType = IdType.SEQUENCE;
    @Configured
    @Description("A column which represent the scope for which the ID will be generated. "
            + "If eg. a source column is selected, an ID will be generated for each source record. "
            + "If a transformed column is selected, an ID will be generated for each record generated that has this column.")
    InputColumn<?> columnInScope;
    @Configured
    @Description("The row number offset. This is often used to insert into a database with existing sequential IDs. "
            + "Since the transformer increments before inserting, an offset of e.g. 100 would make the first newly "
            + "inserted ID 101.")
    long offset = 0;

    public GenerateIdTransformer() {
        _counter = new AtomicLong();
    }

    @Initialize
    public void init() {
        _counter.set(offset);
    }

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(Number.class, "Generated ID");
    }

    @Override
    public Number[] transform(final InputRow inputRow) {
        final long id;
        if (IdType.ROW_NUMBER == idType) {
            id = inputRow.getId();
        } else {
            id = _counter.incrementAndGet();
        }
        return new Number[] { id };
    }

}
