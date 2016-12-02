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
package org.datacleaner.windows;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.reference.ReferenceData;
import org.datacleaner.reference.StringPattern;
import org.datacleaner.reference.SynonymCatalog;
import org.datacleaner.user.ReferenceDataChangeListener;

/**
 * It contains all the listeners for ReferenceData type used in
 * {@link AnalysisJobBuilderWindowImpl}
 */
public class ReferenceDataAnalysisJobWindowImplListeners {

    class WindowChangeSynonymCatalogListener implements ReferenceDataChangeListener<SynonymCatalog> {

        @Override
        public void onAdd(final SynonymCatalog referenceData) {
            // DO NOTHING (because we do not want to automatically select the
            // reference data to be used in the job)
        }

        @Override
        public void onChange(final SynonymCatalog oldReferenceData, final SynonymCatalog newReferenceData) {
            changeReferenceDataValuesInComponents(oldReferenceData, newReferenceData, SynonymCatalog.class);
        }

        @Override
        public void onRemove(final SynonymCatalog referenceData) {
            removeReferenceDataValuesInComponents(referenceData, SynonymCatalog.class);
        }

    }

    class WindowChangeDictionaryListener implements ReferenceDataChangeListener<Dictionary> {

        @Override
        public void onAdd(final Dictionary referenceData) {
            // DO NOTHING (because we do not want to automatically select the
            // reference data to be used in the job)
        }

        @Override
        public void onChange(final Dictionary oldReferenceData, final Dictionary newReferenceData) {
            changeReferenceDataValuesInComponents(oldReferenceData, newReferenceData, Dictionary.class);
        }

        @Override
        public void onRemove(final Dictionary referenceData) {
            removeReferenceDataValuesInComponents(referenceData, Dictionary.class);

        }
    }

    class WindowChangeStringPatternListener implements ReferenceDataChangeListener<StringPattern> {

        @Override
        public void onAdd(final StringPattern referenceData) {
            // DO NOTHING (because we do not want to automatically select the
            // reference data to be used in the job)
        }

        @Override
        public void onChange(final StringPattern oldReferenceData, final StringPattern newReferenceData) {
            changeReferenceDataValuesInComponents(oldReferenceData, newReferenceData, StringPattern.class);
        }

        @Override
        public void onRemove(final StringPattern referenceData) {
            removeReferenceDataValuesInComponents(referenceData, StringPattern.class);
        }

    }

    private final AnalysisJobBuilder _analysisJobBuilder;

    ReferenceDataAnalysisJobWindowImplListeners(final AnalysisJobBuilder analysisJobBuilder) {
        _analysisJobBuilder = analysisJobBuilder;
    }

    /**
     * Method used to change the reference data(String Patterns, Dictionaries,
     * Synonyms) components' values in components
     *
     * @param oldReferenceData
     * @param newReferenceData
     * @param referenceDataClass
     */
    private void changeReferenceDataValuesInComponents(final ReferenceData oldReferenceData,
            final ReferenceData newReferenceData, final Class<?> referenceDataClass) {
        final Collection<ComponentBuilder> componentBuilders = _analysisJobBuilder.getComponentBuilders();
        for (final ComponentBuilder componentBuilder : componentBuilders) {
            final Map<ConfiguredPropertyDescriptor, Object> configuredProperties =
                    componentBuilder.getConfiguredProperties();
            for (final Map.Entry<ConfiguredPropertyDescriptor, Object> entry : configuredProperties.entrySet()) {
                final ConfiguredPropertyDescriptor propertyDescriptor = entry.getKey();
                if (referenceDataClass.isAssignableFrom(propertyDescriptor.getBaseType())) {
                    final Object valueObject = entry.getValue();
                    // In some cases the configured property is an array
                    if (valueObject.getClass().isArray()) {
                        final Object[] values = (Object[]) valueObject;
                        for (int i = 0; i < values.length; i++) {
                            if (oldReferenceData.equals(values[i])) {
                                // change the old value of the pattern in the
                                // array with the new value
                                values[i] = newReferenceData;
                            }
                        }
                    } else {
                        if (oldReferenceData.equals(valueObject)) {
                            componentBuilder.setConfiguredProperty(propertyDescriptor, newReferenceData);
                        }
                    }
                }
            }
        }
    }

    /**
     * Method used to remove the reference data(String Patterns, Dictionaries,
     * Synonyms) components' values in components
     *
     * @param referenceData
     * @param referenceDataClass
     */
    private void removeReferenceDataValuesInComponents(final ReferenceData referenceData,
            final Class<?> referenceDataClass) {
        final Collection<ComponentBuilder> componentBuilders = _analysisJobBuilder.getComponentBuilders();
        for (final ComponentBuilder componentBuilder : componentBuilders) {
            final Map<ConfiguredPropertyDescriptor, Object> configuredProperties =
                    componentBuilder.getConfiguredProperties();
            for (final Map.Entry<ConfiguredPropertyDescriptor, Object> entry : configuredProperties.entrySet()) {
                final ConfiguredPropertyDescriptor propertyDescriptor = entry.getKey();
                if (referenceDataClass.isAssignableFrom(propertyDescriptor.getBaseType())) {
                    final Object valueObject = entry.getValue();
                    // In some cases the configured property is an array
                    if (valueObject.getClass().isArray()) {
                        final Object[] values = (Object[]) valueObject;
                        final Class<?> componentType = valueObject.getClass().getComponentType();
                        // create the new array
                        final Object[] newArray = (Object[]) Array.newInstance(componentType, values.length - 1);
                        int j = 0;
                        for (int i = 0; i < values.length; i++) {
                            if (!referenceData.equals(values[i])) {
                                // keep only the values different than the one select to be removed
                                newArray[j] = values[i];
                                j++;
                            }
                        }
                        componentBuilder.setConfiguredProperty(propertyDescriptor, newArray);
                    } else {
                        if (referenceData.equals(valueObject)) {
                            componentBuilder.setConfiguredProperty(propertyDescriptor, null);
                        }
                    }
                }
            }
        }
    }
}
