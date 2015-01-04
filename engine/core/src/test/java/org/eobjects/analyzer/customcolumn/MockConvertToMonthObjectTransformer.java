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
package org.eobjects.analyzer.customcolumn;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

/**
 * Month transformer to convert a String Month value to Month Object.
 * 
 */
@TransformerBean("Mock Month Transformer")
public class MockConvertToMonthObjectTransformer implements Transformer<Month> {

	@Inject
	@Configured("String month input")
	InputColumn<String> monthString;

	@Override
	public OutputColumns getOutputColumns() {
		return new OutputColumns(new String[]{monthString.getName() + " (as Month Object)"}, new Class[]{Month.class});
	}

	@Override
	public Month[] transform(InputRow inputRow) {
		String value = inputRow.getValue(monthString);
		Month monthObject = createMonthObject(value);
		return new Month[]{monthObject};
	}

	private Month createMonthObject(String value) {
		Month returnObject = null;
		if(value.equalsIgnoreCase("january")){
			returnObject = new Month("January","JAN",1);
		} else if(value.equalsIgnoreCase("february")){
			returnObject = new Month("February","FEB",2);
		} else if(value.equalsIgnoreCase("march")){
			returnObject = new Month("March","MAR",3);
		} else if(value.equalsIgnoreCase("april")){
			returnObject = new Month("April","APR",4);
		} else if(value.equalsIgnoreCase("may")){
			returnObject = new Month("May","MAY",5);
		} else if(value.equalsIgnoreCase("june")){
			returnObject = new Month("June","JUN",6);
		} else if(value.equalsIgnoreCase("july")){
			returnObject = new Month("July","JUL",7);
		} else if(value.equalsIgnoreCase("august")){
			returnObject = new Month("August","AUG",8);
		} else if(value.equalsIgnoreCase("september")){
			returnObject = new Month("September","SEP",9);
		} else if(value.equalsIgnoreCase("october")){
			returnObject = new Month("October","OCT",10);
		} else if(value.equalsIgnoreCase("november")){
			returnObject = new Month("November","NOV",11);
		} else if(value.equalsIgnoreCase("december")){
			returnObject = new Month("December","DEC",12);
		} 
		return returnObject;
	}

}
