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
package org.eobjects.analyzer.beans.standardize;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.beans.categories.MatchingAndStandardizationCategory;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.util.HasGroupLiteral;
import org.eobjects.analyzer.util.NamedPattern;
import org.eobjects.analyzer.util.NamedPatternMatch;

/**
 * Tokenizes/standardizes four components of a full name: Firstname, Lastname,
 * Middlename and Titulation.
 * 
 * 
 */
@TransformerBean("Name standardizer")
@Description("Identify the various parts of a full name column and turn it into separate, standardized tokens.")
@Categorized({ MatchingAndStandardizationCategory.class })
public class NameStandardizerTransformer implements Transformer<String> {

	public static final String[] DEFAULT_PATTERNS = { "FIRSTNAME LASTNAME", "TITULATION. FIRSTNAME LASTNAME",
			"TITULATION FIRSTNAME LASTNAME", "FIRSTNAME MIDDLENAME LASTNAME", "TITULATION. FIRSTNAME MIDDLENAME LASTNAME",
			"LASTNAME, FIRSTNAME", "LASTNAME, FIRSTNAME MIDDLENAME" };

	public static enum NamePart implements HasGroupLiteral {
		FIRSTNAME, LASTNAME, MIDDLENAME, TITULATION;

		@Override
		public String getGroupLiteral() {
			if (this == TITULATION) {
				return "(Mr|Ms|Mrs|Hr|Fru|Frk|Miss|Mister)";
			}
			return null;
		}
	}

	@Inject
	@Configured
	InputColumn<String> inputColumn;

	@Inject
	@Configured("Name patterns")
	String[] stringPatterns = DEFAULT_PATTERNS;

	private List<NamedPattern<NamePart>> namedPatterns;

	@Initialize
	public void init() {
		if (stringPatterns == null) {
			stringPatterns = new String[0];
		}

		namedPatterns = new ArrayList<NamedPattern<NamePart>>();

		for (String stringPattern : stringPatterns) {
			namedPatterns.add(new NamedPattern<NamePart>(stringPattern, NamePart.class));
		}
	}

	@Override
	public OutputColumns getOutputColumns() {
		return new OutputColumns("Firstname", "Lastname", "Middlename", "Titulation");
	}

	@Override
	public String[] transform(InputRow inputRow) {
		String value = inputRow.getValue(inputColumn);
		return transform(value);
	}

	public String[] transform(String value) {
		String firstName = null;
		String lastName = null;
		String middleName = null;
		String titulation = null;

		if (value != null) {
			for (NamedPattern<NamePart> namedPattern : namedPatterns) {
				NamedPatternMatch<NamePart> match = namedPattern.match(value);
				if (match != null) {
					firstName = match.get(NamePart.FIRSTNAME);
					lastName = match.get(NamePart.LASTNAME);
					middleName = match.get(NamePart.MIDDLENAME);
					titulation = match.get(NamePart.TITULATION);
					break;
				}
			}
		}
		return new String[] { firstName, lastName, middleName, titulation };
	}

	@SuppressWarnings("unchecked")
	public void setInputColumn(InputColumn<?> inputColumn) {
		this.inputColumn = (InputColumn<String>) inputColumn;
	}

	public void setStringPatterns(String... stringPatterns) {
		this.stringPatterns = stringPatterns;
	}
}
