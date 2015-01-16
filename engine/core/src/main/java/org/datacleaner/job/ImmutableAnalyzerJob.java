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
package org.datacleaner.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.datacleaner.api.InputColumn;
import org.datacleaner.descriptors.AnalyzerComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.util.CollectionUtils2;
import org.apache.metamodel.util.BaseObject;

import com.google.common.collect.ImmutableMap;

public final class ImmutableAnalyzerJob extends BaseObject implements AnalyzerJob {

    private static final long serialVersionUID = 1L;

    private final String _name;
    private final AnalyzerComponentDescriptor<?> _descriptor;
    private final ComponentConfiguration _beanConfiguration;
    private final ComponentRequirement _componentRequirement;
    private final Map<String, String> _metadataProperties;

    public ImmutableAnalyzerJob(String name, AnalyzerComponentDescriptor<?> descriptor, ComponentConfiguration beanConfiguration,
            ComponentRequirement requirement, Map<String, String> metadataProperties) {
        _name = name;
        _descriptor = descriptor;
        _beanConfiguration = beanConfiguration;
        _componentRequirement = requirement;
        
        if (metadataProperties == null) {
            _metadataProperties = Collections.emptyMap();
        } else {
            _metadataProperties = ImmutableMap.copyOf(metadataProperties);
        }
    }
    
    @Override
    public Map<String, String> getMetadataProperties() {
        return _metadataProperties;
    }

    @Override
    public ComponentRequirement getComponentRequirement() {
        return _componentRequirement;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public AnalyzerComponentDescriptor<?> getDescriptor() {
        return _descriptor;
    }

    @Override
    public ComponentConfiguration getConfiguration() {
        return _beanConfiguration;
    }

    @Override
    public InputColumn<?>[] getInput() {
        List<InputColumn<?>> result = new ArrayList<InputColumn<?>>();
        Set<ConfiguredPropertyDescriptor> propertiesForInput = _descriptor.getConfiguredPropertiesForInput();
        for (ConfiguredPropertyDescriptor propertyDescriptor : propertiesForInput) {
            Object property = _beanConfiguration.getProperty(propertyDescriptor);
            InputColumn<?>[] inputs = CollectionUtils2.arrayOf(InputColumn.class, property);
            if (inputs != null) {
                for (InputColumn<?> inputColumn : inputs) {
                    result.add(inputColumn);
                }
            }
        }
        return result.toArray(new InputColumn<?>[result.size()]);
    }

    @Override
    protected void decorateIdentity(List<Object> identifiers) {
        identifiers.add(_name);
        identifiers.add(_beanConfiguration);
        identifiers.add(_descriptor);
        identifiers.add(_componentRequirement);
    }

    @Override
    public String toString() {
        return "ImmutableAnalyzerJob[name=" + _name + ",analyzer=" + _descriptor.getDisplayName() + "]";
    }
}
