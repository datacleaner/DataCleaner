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
package org.eobjects.analyzer.beans.transform;

import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.beans.categories.ScriptingCategory;
import org.eobjects.analyzer.data.ConstantInputColumn;
import org.eobjects.analyzer.data.ELInputColumn;
import org.eobjects.analyzer.data.ExpressionBasedInputColumn;
import org.eobjects.analyzer.data.InputRow;

@TransformerBean("Expression language (EL) transformer")
@Description("Generates a column based on an EL expression")
@Categorized({ ScriptingCategory.class })
public class ELTransformer implements Transformer<String> {

	@Configured
	String _expression;

	private ExpressionBasedInputColumn<String> _column;

	@Override
	public OutputColumns getOutputColumns() {
		return new OutputColumns(1);
	}

	@Initialize
	public void init() {
		// we simply reuse the functionality available in the ELInputColumn
		if (_expression.indexOf("#{") == -1) {
			_column = new ConstantInputColumn(_expression);
		} else {
			_column = new ELInputColumn(_expression);
		}
	}

	@Override
	public String[] transform(InputRow inputRow) {
		String result = _column.evaluate(inputRow);
		return new String[] { result };
	}

}
