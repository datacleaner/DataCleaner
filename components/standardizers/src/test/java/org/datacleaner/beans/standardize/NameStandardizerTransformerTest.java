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
package org.datacleaner.beans.standardize;

import java.util.Arrays;

import junit.framework.TestCase;

import org.datacleaner.beans.standardize.NameStandardizerTransformer;
import org.datacleaner.data.MetaModelInputColumn;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.MutableColumn;

public class NameStandardizerTransformerTest extends TestCase {

	public void testSimpleScenario() throws Exception {
		NameStandardizerTransformer transformer = new NameStandardizerTransformer();
		Column column = new MutableColumn("name", ColumnType.VARCHAR);

		MetaModelInputColumn inputColumn = new MetaModelInputColumn(column);
		transformer.setInputColumn(inputColumn);

		transformer.init();

		assertEquals("[John, Doh, Doe, null]",
				Arrays.toString(transformer.transform("John Doe Doh")));

		assertEquals("[John, Doh, Doe, null]",
				Arrays.toString(transformer.transform("Doh, John Doe")));

		assertEquals("[Kasper, Sørensen, null, null]",
				Arrays.toString(transformer.transform("Kasper Sørensen")));

		assertEquals("[Kasper, Sørensen, null, null]",
				Arrays.toString(transformer.transform("Sørensen, Kasper")));
		
		assertEquals("[Kasper, Sørensen, null, Mr]",
				Arrays.toString(transformer.transform("Mr. Kasper Sørensen")));
		
		assertEquals("[Kasper, Sørensen, null, Mister]",
				Arrays.toString(transformer.transform("Mister Kasper Sørensen")));
		
		assertEquals("[Jane, Foobar, Doe, Mrs]",
				Arrays.toString(transformer.transform("Mrs. Jane Doe Foobar")));
	}
}
