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
package org.eobjects.datacleaner.sample;

import java.util.Random;

import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Concurrent;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.beans.categories.StringManipulationCategory;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

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
@TransformerBean("Hello world transformer")
@Categorized(StringManipulationCategory.class)
@Description("Put your description of your transformer here")
@Concurrent(true)
public class HelloWorldTransformer implements Transformer<String> {

	// REQUIRED: One or more InputColumn based
	@Configured
	InputColumn<String> nameColumn;

	@Configured
	@Description("A set of randomized greetings")
	String[] greetings = { "Hello", "Howdy", "Hi", "Yo" };

	private Random random = new Random();

	@Override
	public OutputColumns getOutputColumns() {
		String[] columnNames = { nameColumn.getName() + " (greeting)" };
		return new OutputColumns(columnNames);
	}

	@Override
	public String[] transform(InputRow inputRow) {
		String name = inputRow.getValue(nameColumn);

		int randomIndex = random.nextInt(greetings.length);
		String greeting = greetings[randomIndex];

		String greetingLine = greeting + " " + name;

		return new String[] { greetingLine };
	}

}
