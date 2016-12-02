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
package org.datacleaner.sample;

import java.util.Random;

import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Concurrent;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.TextCategory;

/**
 * A sample transformer that appends a greeting to a name column's values.
 *
 * After reading the sample code, notice these characteristics of the
 * transformer implementation.
 *
 * REQUIRED: The class must be annotated with @TransformerBean with a name
 *
 * REQUIRED: The class must implement the {@link Transformer} interface. The
 * generic parameter to this interface specifies the transformed output
 * column(s) type.
 *
 * OPTIONAL: @Categorized for categorization in menus etc.
 *
 * OPTIONAL: @Description for tooltips etc.
 *
 * OPTIONAL: @Concurrent for specification of multithreaded behaviour (default
 * true = multithreading allowed)
 *
 * REQUIRED: One or more @Configured InputColumn (or InputColumn[]) fields.
 *
 * OPTIONAL: Additional @Configured fields.
 *
 * OPTIONAL: Any amount of methods with the @Initialize or @Close methods.
 *
 * OPTIONAL: A .png file with the fully classified class name as it's path (see
 * src/main/resources).
 */
@Named("Hello world transformer")
@Categorized(TextCategory.class)
@Description("Put your description of your transformer here")
@Concurrent(true)
public class HelloWorldTransformer implements Transformer {

    // REQUIRED: One or more InputColumn based
    @Configured
    InputColumn<String> nameColumn;

    @Configured
    @Description("A set of randomized greetings")
    String[] greetings = { "Hello", "Howdy", "Hi", "Yo" };

    private Random random = new Random();

    @Override
    public OutputColumns getOutputColumns() {
        final String[] columnNames = { nameColumn.getName() + " (greeting)" };
        return new OutputColumns(String.class, columnNames);
    }

    @Override
    public String[] transform(final InputRow inputRow) {
        final String name = inputRow.getValue(nameColumn);

        final int randomIndex = random.nextInt(greetings.length);
        final String greeting = greetings[randomIndex];

        final String greetingLine = greeting + " " + name;

        return new String[] { greetingLine };
    }

}
