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

import javax.inject.Inject;

import org.apache.metamodel.util.HasName;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.JsonSchemaConfiguredPropertyDescriptorImpl;
import org.datacleaner.descriptors.EnumerationValue;
import org.datacleaner.job.builder.ComponentBuilder;

/**
 * @Since 9/15/15
 */
public class MultipleRemoteEnumPropertyWidget extends AbstractMultipleCheckboxesPropertyWidget<EnumerationValue> {

    @Inject
    @SuppressWarnings("unchecked")
    public MultipleRemoteEnumPropertyWidget(ComponentBuilder componentBuilder,
                                            ConfiguredPropertyDescriptor propertyDescriptor) {
        super(componentBuilder, propertyDescriptor, (Class<EnumerationValue>) propertyDescriptor.getBaseType());
    }

    @Override
    protected EnumerationValue[] getAvailableValues() {
        return ((JsonSchemaConfiguredPropertyDescriptorImpl)getPropertyDescriptor()).getEnumValues();
    }

    @Override
    protected String getName(EnumerationValue item) {
        if (item instanceof HasName) {
            return ((HasName)item).getName();
        }
        return item.toString();
    }

    @Override
    protected String getNotAvailableText() {
        return "not available";
    }

}
