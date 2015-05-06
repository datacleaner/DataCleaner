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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.metamodel.util.BaseObject;
import org.datacleaner.api.InputColumn;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.util.CollectionUtils2;
import org.datacleaner.util.ReadObjectBuilder.Moved;

import com.google.common.collect.ImmutableMap;

public class ImmutableComponentJob extends BaseObject implements ComponentJob {

    private static final long serialVersionUID = 1L;

    @Moved
    private final String _name;

    @Moved
    private final ComponentDescriptor<?> _descriptor;

    @Moved
    private final ComponentConfiguration _beanConfiguration;

    @Moved
    private final ComponentRequirement _componentRequirement;

    @Moved
    private final Map<String, String> _metadataProperties;

    private final OutputDataStreamJob[] _outputDataStreamJobs;

    public ImmutableComponentJob(String name, ComponentDescriptor<?> descriptor,
            ComponentConfiguration componentConfiguration, ComponentRequirement componentRequirement,
            Map<String, String> metadataProperties, OutputDataStreamJob[] outputDataStreamJobs) {
        _name = name;
        _descriptor = descriptor;
        _beanConfiguration = componentConfiguration;
        _componentRequirement = componentRequirement;
        _outputDataStreamJobs = outputDataStreamJobs;

        if (metadataProperties == null) {
            _metadataProperties = Collections.emptyMap();
        } else {
            _metadataProperties = ImmutableMap.copyOf(metadataProperties);
        }
    }

    public ImmutableComponentJob(String name, ComponentDescriptor<?> descriptor,
            ComponentConfiguration componentConfiguration, ComponentRequirement componentRequirement,
            Map<String, String> metadataProperties) {
        this(name, descriptor, componentConfiguration, componentRequirement, metadataProperties, null);
    }

    @Override
    public final Map<String, String> getMetadataProperties() {
        return _metadataProperties;
    }

    @Override
    public final ComponentRequirement getComponentRequirement() {
        return _componentRequirement;
    }

    @Override
    public final String getName() {
        return _name;
    }

    @Override
    public ComponentDescriptor<?> getDescriptor() {
        return _descriptor;
    }

    @Override
    public final ComponentConfiguration getConfiguration() {
        return _beanConfiguration;
    }

    @Override
    public final InputColumn<?>[] getInput() {
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
        identifiers.add(_outputDataStreamJobs);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[name=" + getName() + ",type=" + getDescriptor().getDisplayName() + "]";
    }

    @Override
    public OutputDataStreamJob[] getOutputDataStreamJobs() {
        if (_outputDataStreamJobs == null) {
            return new OutputDataStreamJob[0];
        }
        return Arrays.copyOf(_outputDataStreamJobs, _outputDataStreamJobs.length);
    }
}
