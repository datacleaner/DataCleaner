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

import java.util.concurrent.atomic.AtomicInteger;

import org.datacleaner.beans.api.Categorized;
import org.datacleaner.beans.api.Configured;
import org.datacleaner.beans.api.Description;
import org.datacleaner.beans.api.Initialize;
import org.datacleaner.beans.api.OutputColumns;
import org.datacleaner.beans.api.Transformer;
import org.datacleaner.beans.api.TransformerBean;
import org.datacleaner.beans.categories.NumbersCategory;
import org.datacleaner.data.InputColumn;
import org.datacleaner.data.InputRow;

@TransformerBean("Generate ID")
@Description("Generates a unique and sequential record ID")
@Categorized(NumbersCategory.class)
public class GenerateIdTransformer implements Transformer<Integer> {

	@Configured
	@Description("A column which represent the scope for which the ID will be generated. "
			+ "If eg. a source column is selected, an ID will be generated for each source record. "
			+ "If a transformed column is selected, an ID will be generated for each record generated that has this column.")
	InputColumn<?> columnInScope;

	@Configured
	int offset = 0;

	private final AtomicInteger _counter;

	public GenerateIdTransformer() {
		_counter = new AtomicInteger();
	}

	@Initialize
	public void init() {
		_counter.set(offset);
	}

	@Override
	public OutputColumns getOutputColumns() {
		return new OutputColumns("Generated ID");
	}

	@Override
	public Integer[] transform(InputRow inputRow) {
		int id = _counter.incrementAndGet();
		return new Integer[] { id };
	}

}
