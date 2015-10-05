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

import javax.inject.Inject;
import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.MatchingAndStandardizationCategory;
import org.datacleaner.util.HasGroupLiteral;
import org.datacleaner.util.NamedPattern;
import org.datacleaner.util.NamedPatternMatch;

/**
 * Tokenizes/standardizes the components of an email: Username and Domain
 */
@Named("Email standardizer")
@Description("Retrieve the username or domain from an email address.")
@Categorized({ MatchingAndStandardizationCategory.class })
@Deprecated
public class EmailStandardizerTransformer implements Transformer {

	public static final NamedPattern<EmailPart> EMAIL_PATTERN = new NamedPattern<EmailPart>("USERNAME@DOMAIN",
			EmailPart.class);

	public static enum EmailPart implements HasGroupLiteral {
		USERNAME("([a-zA-Z0-9\\._%+-]+)"), DOMAIN("([a-zA-Z0-9\\._%+-]+\\.[a-zA-Z0-9\\._%+-]{2,4})");

		private String groupLiteral;

		private EmailPart(String groupLiteral) {
			this.groupLiteral = groupLiteral;
		}

		@Override
		public String getGroupLiteral() {
			return groupLiteral;
		}
	}

	@Inject
	@Configured
	InputColumn<String> inputColumn;

	@Override
	public OutputColumns getOutputColumns() {
		return new OutputColumns(String.class, "Username", "Domain");
	}

	@Override
	public String[] transform(InputRow inputRow) {
		String value = inputRow.getValue(inputColumn);
		return transform(value);
	}

	public String[] transform(String value) {
		String username = null;
		String domain = null;

		if (value != null) {
			NamedPatternMatch<EmailPart> match = EMAIL_PATTERN.match(value);
			if (match != null) {
				username = match.get(EmailPart.USERNAME);
				domain = match.get(EmailPart.DOMAIN);
			}
		}
		return new String[] { username, domain };
	}

	public void setInputColumn(InputColumn<String> inputColumn) {
		this.inputColumn = inputColumn;
	}
}
