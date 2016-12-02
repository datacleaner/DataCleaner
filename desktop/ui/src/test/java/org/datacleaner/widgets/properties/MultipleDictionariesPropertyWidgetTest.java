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

import javax.inject.Provider;

import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DomConfigurationWriter;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.lifecycle.LifeCycleHelper;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.reference.SimpleDictionary;
import org.datacleaner.user.MutableReferenceDataCatalog;
import org.datacleaner.user.UserPreferencesImpl;
import org.datacleaner.widgets.DCCheckBox;
import org.datacleaner.windows.ReferenceDataDialog;

import junit.framework.TestCase;

public class MultipleDictionariesPropertyWidgetTest extends TestCase {

    private DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl();
    private AnalysisJobBuilder ajb = new AnalysisJobBuilder(configuration);
    private AnalyzerComponentBuilder<ManyPropertiesAnalyzer> analyzerJobBuilder =
            ajb.addAnalyzer(ManyPropertiesAnalyzer.class);
    private ConfiguredPropertyDescriptor property =
            analyzerJobBuilder.getDescriptor().getConfiguredProperty("Dictionary array property");
    private LifeCycleHelper lifeCycleHelper = new LifeCycleHelper(configuration, null, true);
    private MutableReferenceDataCatalog referenceDataCatalog =
            new MutableReferenceDataCatalog(configuration.getReferenceDataCatalog(), new DomConfigurationWriter(),
                    new UserPreferencesImpl(null), lifeCycleHelper);
    private Provider<ReferenceDataDialog> referenceDataDialogProvider = null;

    public void testInitialSelection() throws Exception {
        assertEquals(Dictionary.class, property.getBaseType());

        referenceDataCatalog.addDictionary(new SimpleDictionary("numbers", "1", "2", "3"));
        referenceDataCatalog.addDictionary(new SimpleDictionary("names", "Jane", "Joe"));

        final MultipleDictionariesPropertyWidget widget =
                new MultipleDictionariesPropertyWidget(analyzerJobBuilder, property, referenceDataCatalog,
                        referenceDataDialogProvider);
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

        final MultipleDictionariesPropertyWidget widget =
                new MultipleDictionariesPropertyWidget(analyzerJobBuilder, property, referenceDataCatalog,
                        referenceDataDialogProvider);

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

    private String getAvailableCheckboxValues(final MultipleDictionariesPropertyWidget widget) {
        final StringBuilder sb = new StringBuilder();
        final Component[] components = widget.getWidget().getComponents();
        for (final Component component : components) {
            if (component instanceof DCCheckBox) {
                @SuppressWarnings("unchecked") final DCCheckBox<Dictionary> checkBox =
                        (DCCheckBox<Dictionary>) component;
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
