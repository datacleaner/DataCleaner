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

import java.awt.GraphicsEnvironment;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.datacleaner.actions.OpenAnalysisJobActionListener;
import org.datacleaner.api.InputColumn;
import org.datacleaner.beans.CompletenessAnalyzer;
import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.guice.DCModule;
import org.datacleaner.guice.DCModuleImpl;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerJobBuilder;
import org.datacleaner.windows.AnalysisJobBuilderWindow;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class MultipleMappedEnumsPropertyWidgetTest extends TestCase {

    public void testRestoreEnumValuesFromFile() throws Exception {
        final DCModule dcModule = new DCModuleImpl();
        final FileObject file = VFS.getManager().resolveFile("src/test/resources/mapped_columns_job.analysis.xml");
        final Injector injector1 = Guice.createInjector(dcModule);
        final AnalyzerBeansConfiguration configuration = injector1.getInstance(AnalyzerBeansConfiguration.class);

        final Injector injector2 = OpenAnalysisJobActionListener.open(file, configuration, injector1);

        final List<AnalyzerJobBuilder<?>> analyzers;
        if (GraphicsEnvironment.isHeadless()) {
            analyzers = injector2.getInstance(AnalysisJobBuilder.class).getAnalyzerJobBuilders();
        } else {
            final AnalysisJobBuilderWindow window = injector2.getInstance(AnalysisJobBuilderWindow.class);
            analyzers = window.getAnalysisJobBuilder().getAnalyzerJobBuilders();
        }

        assertEquals(2, analyzers.size());

        final AnalyzerJobBuilder<?> completenessAnalyzer = analyzers.get(0);
        assertEquals("Completeness analyzer", completenessAnalyzer.getDescriptor().getDisplayName());

        final Set<ConfiguredPropertyDescriptor> enumProperties = completenessAnalyzer.getDescriptor()
                .getConfiguredPropertiesByType(CompletenessAnalyzer.Condition[].class, false);
        assertEquals(1, enumProperties.size());

        final Set<ConfiguredPropertyDescriptor> inputProperties = completenessAnalyzer.getDescriptor()
                .getConfiguredPropertiesForInput(false);
        assertEquals(1, inputProperties.size());

        final ConfiguredPropertyDescriptor enumProperty = enumProperties.iterator().next();
        final Enum<?>[] enumValue = (Enum<?>[]) completenessAnalyzer.getConfiguredProperty(enumProperty);
        assertEquals("{NOT_NULL,NOT_BLANK_OR_NULL}", ArrayUtils.toString(enumValue));

        final ConfiguredPropertyDescriptor inputProperty = inputProperties.iterator().next();
        final InputColumn<?>[] inputValue = (InputColumn<?>[]) completenessAnalyzer
                .getConfiguredProperty(inputProperty);

        final MultipleMappedEnumsPropertyWidget<Enum<?>> inputWidget = new MultipleMappedEnumsPropertyWidget<Enum<?>>(
                completenessAnalyzer, inputProperty, enumProperty);
        final PropertyWidget<Enum<?>[]> enumWidget = inputWidget.getMappedEnumsPropertyWidget();
        enumWidget.initialize(enumValue);
        inputWidget.initialize(inputValue);
        inputWidget.onValueTouched(inputValue);
        enumWidget.onValueTouched(enumValue);

        assertEquals("{NOT_NULL,NOT_BLANK_OR_NULL}", ArrayUtils.toString(enumWidget.getValue()));
    }
}
