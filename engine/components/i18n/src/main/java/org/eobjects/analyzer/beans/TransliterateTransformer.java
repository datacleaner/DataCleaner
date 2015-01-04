/**
 * AnalyzerBeans
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

import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.beans.categories.StringManipulationCategory;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

import com.ibm.icu.text.Transliterator;

@TransformerBean("Transliterate")
@Description("Converts non-latin characters to latin (or even ASCII) characters.")
@Categorized(StringManipulationCategory.class)
public class TransliterateTransformer implements Transformer<String> {

	@Configured
	InputColumn<String> column;

	@Configured(required = false)
	@Description("Should latin characters and diacritics be converted to plain ASCII?")
	boolean latinToAscii = true;

	private final Transliterator latinTransliterator = Transliterator.getInstance("Any-Latin");
	private final Transliterator asciiTransformer = Transliterator.getInstance("Latin-ASCII");

	@Override
	public OutputColumns getOutputColumns() {
		return new OutputColumns(column.getName() + " (transliterated)");
	}

	@Override
	public String[] transform(InputRow inputRow) {
		String value = inputRow.getValue(column);
		if (value == null) {
			return new String[1];
		}
		value = latinTransliterator.transform(value);
		if (latinToAscii) {
			value = asciiTransformer.transform(value);
		}
		return new String[] { value };
	}

}
