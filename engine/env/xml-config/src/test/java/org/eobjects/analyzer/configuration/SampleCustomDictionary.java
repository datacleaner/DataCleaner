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
package org.eobjects.analyzer.configuration;

import java.util.ArrayList;
import java.util.List;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.ReferenceValues;
import org.eobjects.analyzer.reference.SimpleStringReferenceValues;
import org.junit.Ignore;

@Ignore
public class SampleCustomDictionary implements Dictionary {

	private static final long serialVersionUID = 1L;

	@Configured
	String name;

	@Configured
	int values;

	@Configured
	Datastore datastore;
	
	@Configured
	String description;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean containsValue(String value) {
		return getValues().containsValue(value);
	}

	@Override
	public ReferenceValues<String> getValues() {
		List<String> values = new ArrayList<String>();
		for (int i = 0; i < this.values; i++) {
			values.add("value" + i);
		}
		SimpleStringReferenceValues refValues = new SimpleStringReferenceValues(values, true);
		return refValues;
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
