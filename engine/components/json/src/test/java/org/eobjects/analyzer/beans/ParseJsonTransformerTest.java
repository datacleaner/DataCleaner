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
package org.eobjects.analyzer.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;
import org.junit.Test;

public class ParseJsonTransformerTest {

	@Test
	public void testExtractJsonValuesTransformerWithoutAnyValidation() {
		InputColumn<String> col1 = new MockInputColumn<String>("jsonDocument",
				String.class);
		ParseJsonTransformer transformer = new ParseJsonTransformer(col1);
		transformer.init();
		assertEquals(1, transformer.getOutputColumns().getColumnCount());

		String json = "{\"name\":\"shekhar\",\"country\":\"india\"}";

		Object[] values = transformer.transform(new MockInputRow().put(col1,
				json));
		assertEquals(1, values.length);
		assertEquals(2, ((Map<?, ?>) values[0]).size());
		assertEquals("{name=shekhar, country=india}", values[0].toString());
	}

	@Test
	public void testExtractJsonNumbersAndBooleans() {
		InputColumn<String> col1 = new MockInputColumn<String>("jsonDocument",
				String.class);
		ParseJsonTransformer transformer = new ParseJsonTransformer(col1);
		transformer.init();
		assertEquals(1, transformer.getOutputColumns().getColumnCount());

		String json = "{\"name\":\"kasper\",\"age\":29,\"developer\":true,\"manager\":false,\"balance\":400.17}";

		Object[] values = transformer.transform(new MockInputRow().put(col1,
				json));

		assertEquals(1, values.length);
		assertEquals(5, ((Map<?, ?>) values[0]).size());
		assertEquals(
				"{name=kasper, age=29, developer=true, manager=false, balance=400.17}",
				values[0].toString());
	}

	@Test
	public void shouldReturnEmptyMapWhenNoJsonDocumentExistForColumn()
			throws Exception {
		InputColumn<String> col1 = new MockInputColumn<String>("jsonDocument",
				String.class);
		ParseJsonTransformer transformer = new ParseJsonTransformer(col1);
		transformer.init();
		assertEquals(1, transformer.getOutputColumns().getColumnCount());
		Object[] values = transformer.transform(new MockInputRow());
		assertTrue(values.length == 1);
		assertNull(values[0]);
	}

	@Test
	public void shouldExtractNestedDocumentsAsCollections() throws Exception {
		InputColumn<String> col1 = new MockInputColumn<String>("jsonDocument",
				String.class);
		ParseJsonTransformer transformer = new ParseJsonTransformer(col1);
		transformer.init();
		assertEquals(1, transformer.getOutputColumns().getColumnCount());

		String json = "{\"name\":\"shekhar\",\"addresses\":[{\"city\":\"Delhi\",\"country:\":\"India\"},{\"city\":\"Delhi\",\"country:\":\"India\"}],\"emails\":[\"email1\",\"email2\"]}";

		Object[] values = transformer.transform(new MockInputRow().put(col1,
				json));
		assertEquals(1, values.length);
		Map<?, ?> map = (Map<?, ?>) values[0];
		assertEquals(
				"{name=shekhar, addresses=[{city=Delhi, country:=India}, {city=Delhi, country:=India}], emails=[email1, email2]}",
				map.toString());

		assertTrue(map.get("addresses") instanceof List);

		List<?> addresses = (List<?>) map.get("addresses");

		assertTrue(addresses.get(0) instanceof Map);
		assertTrue(map.get("emails") instanceof List);
	}
}
