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

import java.awt.Component;
import java.util.Arrays;

import javax.inject.Provider;

import junit.framework.TestCase;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.configuration.InjectionManagerImpl;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.analyzer.lifecycle.LifeCycleHelper;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.SimpleDictionary;
import org.eobjects.datacleaner.user.MutableReferenceDataCatalog;
import org.eobjects.datacleaner.user.UserPreferencesImpl;
import org.eobjects.datacleaner.widgets.DCCheckBox;
import org.eobjects.datacleaner.windows.ReferenceDataDialog;

public class MultipleDictionariesPropertyWidgetTest extends TestCase {

	private AnalyzerBeansConfiguration configuration = new AnalyzerBeansConfigurationImpl();
	private AnalysisJobBuilder ajb = new AnalysisJobBuilder(configuration);
	private AnalyzerJobBuilder<ManyPropertiesAnalyzer> analyzerJobBuilder = ajb.addAnalyzer(ManyPropertiesAnalyzer.class);
	private ConfiguredPropertyDescriptor property = analyzerJobBuilder.getDescriptor().getConfiguredProperty(
			"Dictionary array property");
	private LifeCycleHelper lifeCycleHelper = new LifeCycleHelper(new InjectionManagerImpl(
			configuration.getDatastoreCatalog(), null, null), null);
	private MutableReferenceDataCatalog referenceDataCatalog = new MutableReferenceDataCatalog(
			configuration.getReferenceDataCatalog(), new UserPreferencesImpl(null), lifeCycleHelper);
	private Provider<ReferenceDataDialog> referenceDataDialogProvider = null;

	public void testInitialSelection() throws Exception {
		assertEquals(Dictionary.class, property.getBaseType());

		referenceDataCatalog.addDictionary(new SimpleDictionary("numbers", "1", "2", "3"));
		referenceDataCatalog.addDictionary(new SimpleDictionary("names", "Jane", "Joe"));

		MultipleDictionariesPropertyWidget widget = new MultipleDictionariesPropertyWidget(analyzerJobBuilder, property,
				referenceDataCatalog, referenceDataDialogProvider);
		widget.initialize(new Dictionary[] { referenceDataCatalog.getDictionary("numbers") });
		widget.onPanelAdd();

		assertEquals("SimpleDictionary[name=numbers],SimpleDictionary[name=names]", getAvailableCheckboxValues(widget));
		assertEquals("[SimpleDictionary[name=numbers]]", Arrays.toString(widget.getValue()));

		referenceDataCatalog.removeDictionary(referenceDataCatalog.getDictionary("numbers"));

		assertEquals("SimpleDictionary[name=names]", getAvailableCheckboxValues(widget));
		assertEquals("[]", Arrays.toString(widget.getValue()));

		referenceDataCatalog.removeDictionary(referenceDataCatalog.getDictionary("names"));

		assertEquals("- no dictionaries available -", getAvailableCheckboxValues(widget));
		assertEquals("[]", Arrays.toString(widget.getValue()));
	}

	public void testNoInitialSelection() throws Exception {
		assertEquals(Dictionary.class, property.getBaseType());

		MultipleDictionariesPropertyWidget widget = new MultipleDictionariesPropertyWidget(analyzerJobBuilder, property,
				referenceDataCatalog, referenceDataDialogProvider);

		widget.initialize(null);
		widget.onPanelAdd();

		assertEquals("- no dictionaries available -", getAvailableCheckboxValues(widget));
		assertEquals("[]", Arrays.toString(widget.getValue()));

		referenceDataCatalog.addDictionary(new SimpleDictionary("numbers", "1", "2", "3"));
		referenceDataCatalog.addDictionary(new SimpleDictionary("names", "Jane", "Joe"));

		assertEquals("SimpleDictionary[name=numbers],SimpleDictionary[name=names]", getAvailableCheckboxValues(widget));
		assertEquals("[]", Arrays.toString(widget.getValue()));

		widget.onValueTouched(new Dictionary[] { referenceDataCatalog.getDictionary("names") });

		assertEquals("SimpleDictionary[name=numbers],SimpleDictionary[name=names]", getAvailableCheckboxValues(widget));
		assertEquals("[SimpleDictionary[name=names]]", Arrays.toString(widget.getValue()));
	}

	private String getAvailableCheckboxValues(MultipleDictionariesPropertyWidget widget) {
		StringBuilder sb = new StringBuilder();
		Component[] components = widget.getWidget().getComponents();
		for (Component component : components) {
			if (component instanceof DCCheckBox) {
				@SuppressWarnings("unchecked")
				DCCheckBox<Dictionary> checkBox = (DCCheckBox<Dictionary>) component;
				final String name;
				if (checkBox.getValue() == null) {
					name = checkBox.getText();
				} else {
					name = checkBox.getValue().toString();
				}
				if (sb.length() > 0) {
					sb.append(',');
				}
				sb.append(name);
			}
		}
		return sb.toString();
	}
}
