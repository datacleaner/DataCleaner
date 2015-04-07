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

import java.awt.Component;
import java.util.Arrays;

import junit.framework.TestCase;

import org.datacleaner.beans.filter.ValidationCategory;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.widgets.DCCheckBox;

public class MultipleEnumPropertyWidgetTest extends TestCase {

	public void testSelection() throws Exception {

	    DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl();
		try (AnalysisJobBuilder ajb = new AnalysisJobBuilder(configuration)) {
		    AnalyzerComponentBuilder<ManyPropertiesAnalyzer> analyzerJobBuilder = ajb.addAnalyzer(ManyPropertiesAnalyzer.class);
		    ConfiguredPropertyDescriptor property = analyzerJobBuilder.getDescriptor().getConfiguredProperty(
		            "Enum array property");
		    
		    assertEquals(ValidationCategory.class, property.getBaseType());
		    
		    MultipleEnumPropertyWidget widget = new MultipleEnumPropertyWidget(analyzerJobBuilder, property);
		    widget.initialize(new ValidationCategory[] { ValidationCategory.VALID, ValidationCategory.INVALID });
		    
		    assertEquals("VALID,INVALID", getAvailableCheckboxValues(widget));
		    assertEquals("[VALID, INVALID]", Arrays.toString(widget.getValue()));
		    
		    widget.onValueTouched(new ValidationCategory[0]);
		    assertEquals("VALID,INVALID", getAvailableCheckboxValues(widget));
		    assertEquals("[]", Arrays.toString(widget.getValue()));
		    
		    widget.onValueTouched(new ValidationCategory[] { ValidationCategory.VALID });
		    assertEquals("VALID,INVALID", getAvailableCheckboxValues(widget));
		    assertEquals("[VALID]", Arrays.toString(widget.getValue()));
		    
		    widget.onValueTouched(new ValidationCategory[] { ValidationCategory.INVALID });
		    assertEquals("VALID,INVALID", getAvailableCheckboxValues(widget));
		    assertEquals("[INVALID]", Arrays.toString(widget.getValue()));
		}
	}

	private String getAvailableCheckboxValues(MultipleEnumPropertyWidget widget) {
		StringBuilder sb = new StringBuilder();
		Component[] components = widget.getWidget().getComponents();
		for (Component component : components) {
			if (component instanceof DCCheckBox) {
				@SuppressWarnings("unchecked")
				DCCheckBox<ValidationCategory> checkBox = (DCCheckBox<ValidationCategory>) component;
				String name = checkBox.getValue().toString();
				if (sb.length() > 0) {
					sb.append(',');
				}
				sb.append(name);
			}
		}
		return sb.toString();
	}
}
