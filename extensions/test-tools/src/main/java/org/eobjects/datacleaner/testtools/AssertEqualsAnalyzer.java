/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.testtools;

import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.metamodel.util.EqualsBuilder;

@AnalyzerBean("Assert equals")
@Categorized(TestToolsCategory.class)
public class AssertEqualsAnalyzer extends TestToolAnalyzer {

	@Configured
	InputColumn<?> column1;

	@Configured
	InputColumn<?> column2;

	@Override
	protected boolean isValid(InputRow row) {
		Object value1 = row.getValue(column1);
		Object value2 = row.getValue(column2);
		return EqualsBuilder.equals(value1, value2);
	}

	@Override
	protected InputColumn<?>[] getColumnsOfInterest() {
		return new InputColumn[] { column1, column2 };
	}
}
