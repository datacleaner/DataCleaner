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
package org.datacleaner.widgets.properties;

import java.io.File;
import java.util.regex.Pattern;

import org.datacleaner.beans.api.Analyzer;
import org.datacleaner.beans.api.AnalyzerBean;
import org.datacleaner.beans.api.Configured;
import org.datacleaner.beans.filter.ValidationCategory;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.UpdateableDatastore;
import org.datacleaner.data.InputColumn;
import org.datacleaner.data.InputRow;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.reference.StringPattern;
import org.datacleaner.reference.SynonymCatalog;
import org.datacleaner.result.NumberResult;
import org.junit.Ignore;

@Ignore
@AnalyzerBean("Many properties!!!")
public class ManyPropertiesAnalyzer implements Analyzer<NumberResult> {

	@Configured
	int intProperty;

	@Configured
	int[] intArrayProperty;

	@Configured
	Number numberProperty;

	@Configured
	Number[] numberArrayProperty;
	
	@Configured
	double doubleProperty;

	@Configured
	double[] doubleArrayProperty;

	@Configured
	boolean boolProperty;

	@Configured
	boolean[] boolArrayProperty;

	@Configured
	String stringProperty;

	@Configured
	String[] stringArrayProperty;

	@Configured
	char charProperty;

	@Configured
	char[] charArrayProperty;

	@Configured
	ValidationCategory enumProperty;

	@Configured
	ValidationCategory[] enumArrayProperty;

	@Configured
	File fileProperty;

	@Configured
	File[] fileArrayProperty;

	@Configured
	Pattern patternProperty;

	@Configured
	Pattern[] patternArrayProperty;

	@Configured
	InputColumn<String> inputColumnProperty;

	@Configured
	InputColumn<String>[] inputColumnArrayProperty;

	@Configured
	StringPattern stringPatternProperty;

	@Configured
	StringPattern[] stringPatternArrayProperty;

	@Configured
	Dictionary dictionaryProperty;

	@Configured
	Dictionary[] dictionaryArrayProperty;

	@Configured
	SynonymCatalog synonymCatalogProperty;

	@Configured
	SynonymCatalog[] synonymCatalogArrayProperty;
	
	@Configured
	Datastore datastoreProperty;
	
	@Configured
	UpdateableDatastore updateableDatastoreProperty;

	@Override
	public NumberResult getResult() {
		return new NumberResult(42);
	}

	@Override
	public void run(InputRow row, int distinctCount) {
		// do nothing
	}

}
