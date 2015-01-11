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
package org.datacleaner.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.datacleaner.api.Configured;
import org.datacleaner.reference.SimpleSynonym;
import org.datacleaner.reference.Synonym;
import org.datacleaner.reference.SynonymCatalog;
import org.datacleaner.util.StringUtils;
import org.junit.Ignore;

@Ignore
public class SampleCustomSynonymCatalog implements SynonymCatalog {

	private static final long serialVersionUID = 1L;

	@Configured
	String name;

	@Configured
	String[][] values;

	@Configured
	String description;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Collection<Synonym> getSynonyms() {
		List<Synonym> result = new ArrayList<Synonym>();
		for (String[] strings : values) {
			result.add(new SimpleSynonym(strings[0], strings));
		}
		return result;
	}

	@Override
	public String getMasterTerm(String term) {
		if (StringUtils.isNullOrEmpty(term)) {
			return null;
		}
		for (Synonym synonym : getSynonyms()) {
			if (synonym.getSynonyms().containsValue(term)) {
				return synonym.getMasterTerm();
			}
		}
		return null;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}
}
