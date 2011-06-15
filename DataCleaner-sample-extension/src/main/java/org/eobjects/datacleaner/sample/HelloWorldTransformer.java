/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@TransformerBean("Hello world transformer")
@Description("Put your description of your transformer here")
public class HelloWorldTransformer implements Transformer<String> {

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
