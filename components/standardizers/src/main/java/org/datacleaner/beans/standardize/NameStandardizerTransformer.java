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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.MatchingAndStandardizationCategory;
import org.datacleaner.util.HasGroupLiteral;
import org.datacleaner.util.NamedPattern;
import org.datacleaner.util.NamedPatternMatch;

/**
 * Tokenizes/standardizes four components of a full name: Firstname, Lastname,
 * Middlename and Titulation.
 */
@Named("Name standardizer")
@Description("Identify the various parts of a full name column and turn it into separate, standardized tokens.")
@Categorized({ MatchingAndStandardizationCategory.class })
@Deprecated
public class NameStandardizerTransformer implements Transformer {

    public enum NamePart implements HasGroupLiteral {
        FIRSTNAME, LASTNAME, MIDDLENAME, TITULATION;

        @Override
        public String getGroupLiteral() {
            if (this == TITULATION) {
                return "(Mr|Ms|Mrs|Hr|Fru|Frk|Miss|Mister)";
            }
            return null;
        }
    }

    public static final String[] DEFAULT_PATTERNS =
            { "FIRSTNAME LASTNAME", "TITULATION. FIRSTNAME LASTNAME", "TITULATION FIRSTNAME LASTNAME",
                    "FIRSTNAME MIDDLENAME LASTNAME", "TITULATION. FIRSTNAME MIDDLENAME LASTNAME", "LASTNAME, FIRSTNAME",
                    "LASTNAME, FIRSTNAME MIDDLENAME" };
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

        namedPatterns = new ArrayList<>();

        for (final String stringPattern : stringPatterns) {
            namedPatterns.add(new NamedPattern<>(stringPattern, NamePart.class));
        }
    }

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(String.class, "Firstname", "Lastname", "Middlename", "Titulation");
    }

    @Override
    public String[] transform(final InputRow inputRow) {
        final String value = inputRow.getValue(inputColumn);
        return transform(value);
    }

    public String[] transform(final String value) {
        String firstName = null;
        String lastName = null;
        String middleName = null;
        String titulation = null;

        if (value != null) {
            for (final NamedPattern<NamePart> namedPattern : namedPatterns) {
                final NamedPatternMatch<NamePart> match = namedPattern.match(value);
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
    public void setInputColumn(final InputColumn<?> inputColumn) {
        this.inputColumn = (InputColumn<String>) inputColumn;
    }

    public void setStringPatterns(final String... stringPatterns) {
        this.stringPatterns = stringPatterns;
    }
}
