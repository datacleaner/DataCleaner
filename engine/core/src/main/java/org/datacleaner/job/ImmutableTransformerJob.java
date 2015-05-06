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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.descriptors.TransformerDescriptor;
import org.datacleaner.util.ReadObjectBuilder;

public final class ImmutableTransformerJob extends ImmutableComponentJob implements TransformerJob {

    private static final long serialVersionUID = 1L;

    private final List<MutableInputColumn<?>> _output;

    public ImmutableTransformerJob(String name, TransformerDescriptor<?> descriptor,
            ComponentConfiguration componentConfiguration, Collection<MutableInputColumn<?>> output,
            ComponentRequirement componentRequirement, Map<String, String> metadataProperties,
            OutputDataStreamJob[] outputDataStreamJobs) {
        super(name, descriptor, componentConfiguration, componentRequirement, metadataProperties, outputDataStreamJobs);
        _output = Collections.unmodifiableList(new ArrayList<MutableInputColumn<?>>(output));
    }

    /**
     * 
     * @param name
     * @param descriptor
     * @param componentConfiguration
     * @param output
     * @param requirement
     * @param metadataProperties
     * 
     * @deprecated use
     *             {@link #ImmutableTransformerJob(String, TransformerDescriptor, ComponentConfiguration, Collection, ComponentRequirement, Map, OutputDataStreamJob[])}
     *             instead
     */
    @Deprecated
    public ImmutableTransformerJob(String name, TransformerDescriptor<?> descriptor,
            ComponentConfiguration componentConfiguration, Collection<MutableInputColumn<?>> output,
            ComponentRequirement requirement, Map<String, String> metadataProperties) {
        this(name, descriptor, componentConfiguration, output, requirement, metadataProperties, null);
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        ReadObjectBuilder.create(this, ImmutableTransformerJob.class).readObject(stream);
    }

    @Override
    public TransformerDescriptor<?> getDescriptor() {
        return (TransformerDescriptor<?>) super.getDescriptor();
    }

    @Override
    public MutableInputColumn<?>[] getOutput() {
        return _output.toArray(new MutableInputColumn<?>[_output.size()]);
    }

    @Override
    public String toString() {
        return "ImmutableTransformerJob[name=" + getName() + ",transformer=" + getDescriptor().getDisplayName() + "]";
    }
}
