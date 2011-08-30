/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.widgets.properties;

import java.io.File;
import java.util.regex.Pattern;

import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.RowProcessingAnalyzer;
import org.eobjects.analyzer.beans.filter.ValidationCategory;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.StringPattern;
import org.eobjects.analyzer.reference.SynonymCatalog;
import org.eobjects.analyzer.result.NumberResult;
import org.junit.Ignore;

@Ignore
@AnalyzerBean("Many properties!!!")
public class ManyPropertiesAnalyzer implements RowProcessingAnalyzer<NumberResult> {

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

	@Override
	public NumberResult getResult() {
		return new NumberResult(42);
	}

	@Override
	public void run(InputRow row, int distinctCount) {
		// do nothing
	}

}
