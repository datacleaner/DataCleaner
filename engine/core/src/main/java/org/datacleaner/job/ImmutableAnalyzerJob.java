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
import java.util.Map;

import org.datacleaner.descriptors.AnalyzerDescriptor;
import org.datacleaner.util.ReadObjectBuilder;

public final class ImmutableAnalyzerJob extends ImmutableComponentJob implements AnalyzerJob {

    private static final long serialVersionUID = 1L;
    
    public ImmutableAnalyzerJob(String name, AnalyzerDescriptor<?> descriptor,
            ComponentConfiguration componentConfiguration, ComponentRequirement componentRequirement,
            Map<String, String> metadataProperties, OutputDataStreamJob[] outputDataStreamJobs) {
        super(name, descriptor, componentConfiguration, componentRequirement, metadataProperties, outputDataStreamJobs);
    }

    public ImmutableAnalyzerJob(String name, AnalyzerDescriptor<?> descriptor,
            ComponentConfiguration componentConfiguration, ComponentRequirement requirement,
            Map<String, String> metadataProperties) {
        super(name, descriptor, componentConfiguration, requirement, metadataProperties);
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        ReadObjectBuilder.create(this, ImmutableAnalyzerJob.class).readObject(stream);
    }

    @Override
    public AnalyzerDescriptor<?> getDescriptor() {
        return (AnalyzerDescriptor<?>) super.getDescriptor();
    }

    @Override
    public String toString() {
        return "ImmutableAnalyzerJob[name=" + getName() + ",analyzer=" + getDescriptor().getDisplayName() + "]";
    }
}
